package com.example.calculator;

import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * MainActivity handles user interactions with the calculator keypad and updates
 * the expression and result displays.
 *
 * Implements:
 * - Number inputs (0-9) and decimal point (.)
 * - Operators (+, −, ×, ÷, %)
 * - Equals (=) evaluation with division-by-zero handling ("Error")
 * - Clear (C) and Backspace (⌫)
 * - Sign toggle (±)
 * - Safe state management using StringBuilder
 * - Configuration change persistence
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String KEY_EXPRESSION = "key_expression";
    private static final String KEY_RESULT = "key_result";
    private static final String KEY_IS_CALCULATED = "key_is_calculated";
    private static final String KEY_IS_ERROR = "key_is_error";

    private TextView tvExpression;
    private TextView tvResult;
    private HorizontalScrollView scrollExpression;
    private HorizontalScrollView scrollResult;

    private final StringBuilder expressionBuilder = new StringBuilder();
    private String previousResult = "";
    private boolean isCalculated = false;
    private boolean isError = false;

    private int colorPrimaryText;
    private int colorErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupButtonListeners();

        colorPrimaryText = ContextCompat.getColor(this, R.color.text_primary);
        colorErrorText = ContextCompat.getColor(this, R.color.text_error);

        if (savedInstanceState != null) {
            String savedExpr = savedInstanceState.getString(KEY_EXPRESSION, "");
            String savedRes = savedInstanceState.getString(KEY_RESULT, getString(R.string.default_result));
            isCalculated = savedInstanceState.getBoolean(KEY_IS_CALCULATED, false);
            isError = savedInstanceState.getBoolean(KEY_IS_ERROR, false);

            expressionBuilder.setLength(0);
            expressionBuilder.append(savedExpr);
            tvExpression.setText(savedExpr);
            tvResult.setText(savedRes);

            if (isError) {
                tvResult.setTextColor(colorErrorText);
            }
        }
    }

    private void initViews() {
        tvExpression = findViewById(R.id.tvExpression);
        tvResult = findViewById(R.id.tvResult);
        scrollExpression = findViewById(R.id.scrollExpression);
        scrollResult = findViewById(R.id.scrollResult);
    }

    private void setupButtonListeners() {
        int[] buttonIds = {
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDot,
            R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide, R.id.btnPercent,
            R.id.btnClear, R.id.btnBackspace, R.id.btnEquals, R.id.btnPlusMinus
        };

        for (int id : buttonIds) {
            View view = findViewById(id);
            if (view != null) {
                view.setOnClickListener(this);
            }
        }

        // Long click on backspace provides a convenient shortcut to clear all
        View btnBackspace = findViewById(R.id.btnBackspace);
        if (btnBackspace != null) {
            btnBackspace.setOnLongClickListener(v -> {
                clearAll();
                return true;
            });
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btn0) appendNumber("0");
        else if (id == R.id.btn1) appendNumber("1");
        else if (id == R.id.btn2) appendNumber("2");
        else if (id == R.id.btn3) appendNumber("3");
        else if (id == R.id.btn4) appendNumber("4");
        else if (id == R.id.btn5) appendNumber("5");
        else if (id == R.id.btn6) appendNumber("6");
        else if (id == R.id.btn7) appendNumber("7");
        else if (id == R.id.btn8) appendNumber("8");
        else if (id == R.id.btn9) appendNumber("9");
        else if (id == R.id.btnDot) appendDecimal();
        else if (id == R.id.btnAdd) appendOperator(" + ");
        else if (id == R.id.btnSubtract) appendOperator(" − ");
        else if (id == R.id.btnMultiply) appendOperator(" × ");
        else if (id == R.id.btnDivide) appendOperator(" ÷ ");
        else if (id == R.id.btnPercent) appendOperator(" % ");
        else if (id == R.id.btnClear) clearAll();
        else if (id == R.id.btnBackspace) backspace();
        else if (id == R.id.btnEquals) calculateResult();
        else if (id == R.id.btnPlusMinus) togglePlusMinus();
    }

    /**
     * Appends a digit to the expression with defensive checks against multiple leading zeros
     * and automatic reset following an evaluation or error.
     */
    private void appendNumber(String digit) {
        if (isError) {
            clearAll();
        } else if (isCalculated) {
            // Typing a new number after equals starts a fresh calculation
            expressionBuilder.setLength(0);
            isCalculated = false;
        }

        // Prevent multiple leading zeros in an integer segment (e.g. "00" -> "0")
        String currentSegment = getCurrentOperandSegment();
        if ("0".equals(currentSegment)) {
            expressionBuilder.deleteCharAt(expressionBuilder.length() - 1);
        }

        expressionBuilder.append(digit);
        updateDisplay();
    }

    /**
     * Appends a decimal point, ensuring only one decimal point exists per operand.
     * Automatically prefixes '0' if placed at the start of a number.
     */
    private void appendDecimal() {
        if (isError || isCalculated) {
            clearAll();
            expressionBuilder.append("0.");
            updateDisplay();
            return;
        }

        String currentSegment = getCurrentOperandSegment();
        if (currentSegment.contains(".")) {
            // Do not allow multiple decimals in a single number
            return;
        }

        if (currentSegment.isEmpty() || currentSegment.endsWith(" ")) {
            expressionBuilder.append("0.");
        } else {
            expressionBuilder.append(".");
        }

        updateDisplay();
    }

    /**
     * Appends an operator (+, −, ×, ÷, %) with intelligent replacement if another operator
     * was just entered, preventing invalid syntax like "5 + ×".
     */
    private void appendOperator(String op) {
        if (isError) {
            return;
        }

        if (isCalculated) {
            // Continue calculating using previous result
            expressionBuilder.setLength(0);
            expressionBuilder.append(previousResult);
            isCalculated = false;
        }

        String current = expressionBuilder.toString();

        if (current.isEmpty()) {
            // Allow unary minus at the very beginning
            if (op.contains("−")) {
                expressionBuilder.append("−");
                updateDisplay();
            }
            return;
        }

        // If expression already ends with an operator, strip it to safely replace
        if (endsWithOperator()) {
            stripTrailingOperator();
        }

        expressionBuilder.append(op);
        updateDisplay();
    }

    /**
     * Evaluates the current expression using CalculatorEngine and updates the UI.
     * Handles division-by-zero by showing "Error".
     */
    private void calculateResult() {
        if (expressionBuilder.length() == 0 || isError) {
            return;
        }

        String expr = expressionBuilder.toString();
        String result = CalculatorEngine.evaluate(expr);

        if (CalculatorEngine.ERROR_MESSAGE.equals(result)) {
            isError = true;
            isCalculated = false;
            tvExpression.setText(expr + " =");
            tvResult.setText(R.string.error_divide_by_zero);
            tvResult.setTextColor(colorErrorText);
        } else {
            isError = false;
            isCalculated = true;
            previousResult = result;
            tvExpression.setText(expr + " =");
            tvResult.setText(result);
            tvResult.setTextColor(colorPrimaryText);
        }

        scrollDisplaysToEnd();
    }

    /**
     * Resets the entire calculator state.
     */
    private void clearAll() {
        expressionBuilder.setLength(0);
        isCalculated = false;
        isError = false;
        previousResult = "";
        tvExpression.setText("");
        tvResult.setText(R.string.default_result);
        tvResult.setTextColor(colorPrimaryText);
        scrollDisplaysToEnd();
    }

    /**
     * Deletes the last character or operator from the expression.
     */
    private void backspace() {
        if (isError || isCalculated) {
            clearAll();
            return;
        }

        int len = expressionBuilder.length();
        if (len == 0) {
            return;
        }

        if (endsWithOperator()) {
            stripTrailingOperator();
        } else {
            expressionBuilder.deleteCharAt(len - 1);
        }

        updateDisplay();
    }

    /**
     * Safely removes the trailing operator and its surrounding spaces from expressionBuilder.
     */
    private void stripTrailingOperator() {
        while (expressionBuilder.length() > 0) {
            char lastChar = expressionBuilder.charAt(expressionBuilder.length() - 1);
            if (lastChar == ' ' || lastChar == '+' || lastChar == '−' || lastChar == '-' ||
                lastChar == '×' || lastChar == '*' || lastChar == '÷' || lastChar == '/' || lastChar == '%') {
                expressionBuilder.deleteCharAt(expressionBuilder.length() - 1);
            } else {
                break;
            }
        }
    }

    /**
     * Toggles the sign (positive/negative) of the current number segment.
     */
    private void togglePlusMinus() {
        if (isError) return;

        if (isCalculated) {
            expressionBuilder.setLength(0);
            expressionBuilder.append(previousResult);
            isCalculated = false;
        }

        String current = expressionBuilder.toString();
        if (current.isEmpty()) {
            expressionBuilder.append("−");
            updateDisplay();
            return;
        }

        int lastSpace = current.lastIndexOf(" ");
        int startIndex = (lastSpace >= 0) ? lastSpace + 1 : 0;
        String segment = current.substring(startIndex);

        if (segment.isEmpty()) {
            expressionBuilder.append("−");
            updateDisplay();
            return;
        }

        if (segment.startsWith("−") || segment.startsWith("-")) {
            // Remove negative sign
            expressionBuilder.deleteCharAt(startIndex);
        } else {
            // Add negative sign
            expressionBuilder.insert(startIndex, "−");
        }

        updateDisplay();
    }

    private boolean endsWithOperator() {
        String current = expressionBuilder.toString().trim();
        return current.endsWith("+") || current.endsWith("−") ||
               current.endsWith("×") || current.endsWith("÷") || current.endsWith("%");
    }

    private String getCurrentOperandSegment() {
        String current = expressionBuilder.toString();
        int lastSpace = current.lastIndexOf(" ");
        if (lastSpace >= 0 && lastSpace + 1 < current.length()) {
            return current.substring(lastSpace + 1);
        }
        return current;
    }

    /**
     * Updates display views and computes live evaluation when appropriate.
     */
    private void updateDisplay() {
        String current = expressionBuilder.toString();
        tvExpression.setText(current);

        if (current.isEmpty()) {
            tvResult.setText(R.string.default_result);
            tvResult.setTextColor(colorPrimaryText);
        } else if (!endsWithOperator()) {
            // Show live result preview for user convenience
            String liveResult = CalculatorEngine.evaluate(current);
            if (!CalculatorEngine.ERROR_MESSAGE.equals(liveResult)) {
                tvResult.setText(liveResult);
                tvResult.setTextColor(colorPrimaryText);
            }
        }

        scrollDisplaysToEnd();
    }

    private void scrollDisplaysToEnd() {
        tvExpression.post(() -> {
            if (scrollExpression != null) {
                scrollExpression.fullScroll(View.FOCUS_RIGHT);
            }
        });
        tvResult.post(() -> {
            if (scrollResult != null) {
                scrollResult.fullScroll(View.FOCUS_RIGHT);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_EXPRESSION, expressionBuilder.toString());
        outState.putString(KEY_RESULT, tvResult.getText().toString());
        outState.putBoolean(KEY_IS_CALCULATED, isCalculated);
        outState.putBoolean(KEY_IS_ERROR, isError);
    }
}
