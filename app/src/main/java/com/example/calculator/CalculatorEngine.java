package com.example.calculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

/**
 * Robust mathematical expression evaluation engine for the Calculator app.
 * Supports standard arithmetic operations (+, −, ×, ÷, %), operator precedence,
 * unary minus, floating point rounding, and division-by-zero detection.
 */
public class CalculatorEngine {

    public static final String ERROR_MESSAGE = "Error";

    /**
     * Evaluates a mathematical expression string and returns the formatted result.
     *
     * @param expression The raw or formatted expression string (e.g., "12 + 5 × 3")
     * @return Formatted result string (e.g., "27") or "Error" if invalid/division by zero.
     */
    public static String evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return "0";
        }

        try {
            // Normalize visual operator symbols to standard characters
            String normalized = normalizeExpression(expression);

            // Tokenize expression
            List<String> tokens = tokenize(normalized);
            if (tokens.isEmpty()) {
                return "0";
            }

            // Remove trailing operator if present (defensive handling for expressions like "5 +")
            if (isOperator(tokens.get(tokens.size() - 1))) {
                tokens.remove(tokens.size() - 1);
            }

            if (tokens.isEmpty()) {
                return "0";
            }

            // Convert to Reverse Polish Notation (Shunting-Yard)
            List<String> rpn = infixToRPN(tokens);

            // Evaluate RPN
            double result = evaluateRPN(rpn);

            if (Double.isInfinite(result) || Double.isNaN(result)) {
                return ERROR_MESSAGE;
            }

            return formatResult(result);
        } catch (ArithmeticException e) {
            return ERROR_MESSAGE;
        } catch (Exception e) {
            return ERROR_MESSAGE;
        }
    }

    /**
     * Normalizes Unicode arithmetic symbols into standard ASCII operators.
     */
    public static String normalizeExpression(String expr) {
        return expr.replace('×', '*')
                   .replace('÷', '/')
                   .replace('−', '-')
                   .replace(" ", "")
                   .trim();
    }

    /**
     * Tokenizes an expression into numbers and operators, handling unary minus.
     */
    public static List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        int len = expr.length();
        int i = 0;

        while (i < len) {
            char c = expr.charAt(i);

            // Unary minus handling: at start of expression or immediately after an operator
            if (c == '-' && (tokens.isEmpty() || isOperator(tokens.get(tokens.size() - 1)))) {
                StringBuilder num = new StringBuilder();
                num.append(c);
                i++;
                // Read digits and optional decimal point
                boolean hasDot = false;
                while (i < len && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    if (expr.charAt(i) == '.') {
                        if (hasDot) break;
                        hasDot = true;
                    }
                    num.append(expr.charAt(i));
                    i++;
                }
                if (num.length() == 1) {
                    // Just a lone minus operator
                    tokens.add("-");
                } else {
                    tokens.add(num.toString());
                }
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                StringBuilder num = new StringBuilder();
                boolean hasDot = false;
                while (i < len && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    if (expr.charAt(i) == '.') {
                        if (hasDot) break;
                        hasDot = true;
                    }
                    num.append(expr.charAt(i));
                    i++;
                }
                tokens.add(num.toString());
                continue;
            }

            if (isOperatorChar(c)) {
                tokens.add(String.valueOf(c));
                i++;
                continue;
            }

            // Skip any other unexpected character
            i++;
        }

        return tokens;
    }

    private static boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
    }

    private static boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") ||
               token.equals("/") || token.equals("%");
    }

    private static int getPrecedence(String op) {
        switch (op) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
            case "%":
                return 2;
            default:
                return 0;
        }
    }

    /**
     * Converts infix tokens to Reverse Polish Notation (RPN) using Dijkstra's Shunting-Yard algorithm.
     */
    private static List<String> infixToRPN(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> operatorStack = new Stack<>();

        for (String token : tokens) {
            if (isOperator(token)) {
                while (!operatorStack.isEmpty() && isOperator(operatorStack.peek()) &&
                        getPrecedence(operatorStack.peek()) >= getPrecedence(token)) {
                    output.add(operatorStack.pop());
                }
                operatorStack.push(token);
            } else {
                output.add(token);
            }
        }

        while (!operatorStack.isEmpty()) {
            output.add(operatorStack.pop());
        }

        return output;
    }

    /**
     * Evaluates a list of tokens in Reverse Polish Notation.
     */
    private static double evaluateRPN(List<String> rpn) {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Malformed expression");
                }
                double b = stack.pop();
                double a = stack.pop();
                double res;

                switch (token) {
                    case "+":
                        res = a + b;
                        break;
                    case "-":
                        res = a - b;
                        break;
                    case "*":
                        res = a * b;
                        break;
                    case "/":
                        if (Math.abs(b) < 1e-12) {
                            throw new ArithmeticException("Division by zero");
                        }
                        res = a / b;
                        break;
                    case "%":
                        // Modulo or percentage calculation
                        res = a % b;
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown operator: " + token);
                }
                stack.push(res);
            } else {
                try {
                    stack.push(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + token);
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid expression evaluation state");
        }

        return stack.pop();
    }

    /**
     * Formats the numeric calculation result into a clean, human-readable string.
     * Removes unnecessary trailing zeros (e.g. 5.0 -> 5) and avoids floating point noise (0.1+0.2 -> 0.3).
     */
    public static String formatResult(double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            return ERROR_MESSAGE;
        }

        // Avoid -0 display
        if (Math.abs(value) < 1e-12) {
            return "0";
        }

        // Clean integer representation
        if (value == (long) value && Math.abs(value) < 1e12) {
            return String.format(Locale.US, "%d", (long) value);
        }

        try {
            BigDecimal bd = new BigDecimal(Double.toString(value), MathContext.DECIMAL64);
            bd = bd.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
            
            // Format large or extremely small numbers
            if (bd.abs().compareTo(new BigDecimal("1e12")) >= 0 ||
               (bd.abs().compareTo(new BigDecimal("1e-6")) < 0 && bd.compareTo(BigDecimal.ZERO) != 0)) {
                return String.format(Locale.US, "%e", value);
            }
            return bd.toPlainString();
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
