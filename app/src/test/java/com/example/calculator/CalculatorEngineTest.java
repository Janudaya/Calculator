package com.example.calculator;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for CalculatorEngine.
 * Verifies arithmetic operations, precedence, division-by-zero, decimal handling,
 * and rapid/edge-case expressions.
 */
public class CalculatorEngineTest {

    @Test
    public void testBasicAddition() {
        assertEquals("7", CalculatorEngine.evaluate("3 + 4"));
        assertEquals("100", CalculatorEngine.evaluate("45 + 55"));
    }

    @Test
    public void testBasicSubtraction() {
        assertEquals("2", CalculatorEngine.evaluate("5 − 3"));
        assertEquals("-8", CalculatorEngine.evaluate("2 − 10"));
    }

    @Test
    public void testBasicMultiplication() {
        assertEquals("24", CalculatorEngine.evaluate("6 × 4"));
        assertEquals("0", CalculatorEngine.evaluate("0 × 100"));
    }

    @Test
    public void testBasicDivision() {
        assertEquals("5", CalculatorEngine.evaluate("20 ÷ 4"));
        assertEquals("2.5", CalculatorEngine.evaluate("5 ÷ 2"));
    }

    @Test
    public void testDivisionByZeroShowsError() {
        assertEquals("Error", CalculatorEngine.evaluate("10 ÷ 0"));
        assertEquals("Error", CalculatorEngine.evaluate("5 / 0"));
        assertEquals("Error", CalculatorEngine.evaluate("0 ÷ 0"));
        assertEquals("Error", CalculatorEngine.evaluate("100 + 4 ÷ 0"));
    }

    @Test
    public void testOperatorPrecedence() {
        // Multiplication and division before addition and subtraction
        assertEquals("14", CalculatorEngine.evaluate("2 + 3 × 4"));
        assertEquals("8", CalculatorEngine.evaluate("10 − 4 ÷ 2"));
        assertEquals("17", CalculatorEngine.evaluate("5 + 6 × 2"));
        assertEquals("23", CalculatorEngine.evaluate("3 + 4 × 5"));
        assertEquals("11", CalculatorEngine.evaluate("20 ÷ 2 + 1"));
    }

    @Test
    public void testDecimalArithmetic() {
        // Precision check: 0.1 + 0.2 should cleanly equal 0.3 without 0.30000000000000004
        assertEquals("0.3", CalculatorEngine.evaluate("0.1 + 0.2"));
        assertEquals("7.5", CalculatorEngine.evaluate("3.2 + 4.3"));
        assertEquals("0.75", CalculatorEngine.evaluate("1.5 ÷ 2"));
    }

    @Test
    public void testNegativeNumbers() {
        assertEquals("2", CalculatorEngine.evaluate("-5 + 7"));
        assertEquals("-15", CalculatorEngine.evaluate("5 × -3"));
        assertEquals("-8", CalculatorEngine.evaluate("-4 + -4"));
    }

    @Test
    public void testTrailingOperatorDefensiveHandling() {
        // Tapping equals on "5 +" should evaluate to "5" without crashing
        assertEquals("5", CalculatorEngine.evaluate("5 +"));
        assertEquals("12", CalculatorEngine.evaluate("12 ×"));
    }

    @Test
    public void testEmptyOrWhitespaceExpression() {
        assertEquals("0", CalculatorEngine.evaluate(""));
        assertEquals("0", CalculatorEngine.evaluate("   "));
        assertEquals("0", CalculatorEngine.evaluate(null));
    }

    @Test
    public void testChainedOperations() {
        assertEquals("20", CalculatorEngine.evaluate("2 + 3 + 5 + 10"));
        assertEquals("10", CalculatorEngine.evaluate("100 ÷ 2 ÷ 5"));
    }

    @Test
    public void testModuloPercentage() {
        assertEquals("1", CalculatorEngine.evaluate("10 % 3"));
    }
}
