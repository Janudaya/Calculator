# Modern Android Calculator Application
> **Tech Stack:** Android Studio • Java • XML Layouts • Material Design Components

A clean, responsive, and robust Android Calculator application built with Java and XML. Features an intuitive two-tier expression/result display, a responsive 4×5 `GridLayout` keypad, operator precedence evaluation via Dijkstra's Shunting-Yard algorithm, floating-point precision formatting, and defensive input sanitization to eliminate crashes from rapid or malformed button presses.

---

## 📋 Feature Checklist Compliance

| Feature Requirement | Status | Implementation Details | Key Files |
| :--- | :---: | :--- | :--- |
| **Display TextView for Input & Result** | ✅ | Dual-display setup with `tvExpression` (shows the active calculation string) and `tvResult` (shows live evaluation, final answer, or error state) with horizontal scroll support. | `activity_main.xml`<br>`MainActivity.java` |
| **Number Buttons (0–9) & Decimal** | ✅ | Dedicated material buttons with tactile ripple feedback and sound; blocks invalid leading zeros (`00` -> `0`). | `activity_main.xml`<br>`MainActivity.java` |
| **Operator Buttons: +, −, ×, ÷, %** | ✅ | Distinctly accented operator keys with visual Unicode symbols (`+`, `−`, `×`, `÷`, `%`). | `activity_main.xml`<br>`themes.xml` |
| **Equals (=) Button** | ✅ | Evaluates the full mathematical expression respecting algebraic operator precedence and formats results cleanly. | `CalculatorEngine.java`<br>`MainActivity.java` |
| **Clear (C) Button** | ✅ | Instantly resets the `StringBuilder`, display buffers, error flags, and calculation states. | `MainActivity.java` |
| **Backspace (⌫) Button** | ✅ | Deletes the last entered character or multi-character formatted operator; long-press activates full clear. | `MainActivity.java` |
| **Division-by-Zero Error Handling** | ✅ | Detects division by zero or infinite/NaN conditions and displays `"Error"` in stylized warning color without crashing. | `CalculatorEngine.java`<br>`MainActivity.java` |
| **Responsive Grid Layout** | ✅ | Button keypad built with `GridLayout` using `layout_rowWeight` and `layout_columnWeight` inside a `ConstraintLayout` container. Scales seamlessly across phone and tablet screens. | `activity_main.xml` |
| **Rapid & Repeated Tap Defense** | ✅ | Operator replacement prevents invalid consecutive operators (`5 + ×` becomes `5 ×`), single-decimal per number rule, boundary-safe string manipulations, and configuration change preservation. | `MainActivity.java`<br>`CalculatorEngine.java` |

---

## 🏗️ Architecture & Project Structure

```text
CalculatorApp/
├── build.gradle                                  # Top-level Gradle build configuration
├── settings.gradle                               # Project modules & repository settings
├── gradle.properties                             # JVM args and AndroidX flags
├── gradlew.bat                                   # Gradle wrapper execution script for Windows
├── gradle/wrapper/
│   └── gradle-wrapper.properties                 # Gradle 8.4 wrapper distribution
└── app/
    ├── build.gradle                              # Application dependencies (Material 1.11, ConstraintLayout, GridLayout)
    ├── proguard-rules.pro                        # Code obfuscation & optimization rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml               # App manifest with portrait lock & soft keyboard prevention
        │   ├── java/com/example/calculator/
        │   │   ├── MainActivity.java             # UI controller, button OnClick routing & StringBuilder management
        │   │   └── CalculatorEngine.java         # Shunting-Yard parser, RPN evaluator & precision formatter
        │   └── res/
        │       ├── layout/
        │       │   └── activity_main.xml         # Dual TextView display + 4x5 responsive GridLayout keypad
        │       ├── values/
        │       │   ├── colors.xml                # Dark slate, accent amber, function gray, and error colors
        │       │   ├── strings.xml               # Translatable labels and accessibility descriptions
        │       │   ├── dimens.xml                # Padding, margins, and typography sizes
        │       │   └── themes.xml                # DayNight material theme & modular button styles
        │       ├── values-night/
        │       │   └── themes.xml                # Dark mode theme overrides
        │       └── drawable/                     # Launcher icons & vector assets
        └── test/
            └── java/com/example/calculator/
                └── CalculatorEngineTest.java     # JUnit test suite for arithmetic, precedence, decimals & errors
```

---

## 🧮 Mathematical Evaluation Engine

The calculator relies on a decoupled, pure-Java evaluation engine (`CalculatorEngine.java`) that performs:

1. **Normalization**: Replaces visual Unicode glyphs (`×`, `÷`, `−`) with standard mathematical operator tokens (`*`, `/`, `-`).
2. **Tokenization with Unary Support**:
   - Parses operands, decimals, and operators.
   - Accurately detects unary negative numbers at the beginning of expressions (e.g. `-5 + 8`) or after binary operators (e.g. `6 × -2`).
3. **Dijkstra's Shunting-Yard Algorithm**:
   - Converts infix notation into Reverse Polish Notation (RPN / Postfix).
   - Enforces strict operator precedence: `×`, `÷`, and `%` (Precedence 2) execute before `+` and `−` (Precedence 1).
4. **RPN Evaluation**:
   - Evaluates postfix tokens using an operand stack.
   - Throws `ArithmeticException("Division by zero")` if divisor magnitude is `< 1e-12`.
5. **Clean Result Formatting**:
   - Whole numbers are displayed as integers without trailing `.0` (e.g. `5` instead of `5.0`).
   - Floating-point noise is eliminated using `BigDecimal` half-up rounding (e.g. `0.1 + 0.2 = 0.3`).
   - Very large ($> 10^{12}$) or infinitesimal numbers format in scientific notation.

---

## 🛡️ Rapid Tap & Fault Tolerance Features

- **No Multi-Operator Collision**: If a user presses `5 +` and subsequently taps `×`, the application dynamically replaces `+` with `×` instead of creating an unparseable `5 + ×` string.
- **Single Decimal Rule**: The active number segment is inspected; additional decimal taps are ignored if a `.` already exists in the current number.
- **Smart Decimal Prefix**: Tapping `.` on an empty display or right after an operator auto-inserts `0.` (e.g. `+ .5` becomes `+ 0.5`).
- **Defensive Trailing Operators**: Tapping `=` on `10 +` safely evaluates `10` without throwing a syntax exception.
- **Division by Zero Protection**: `10 ÷ 0` immediately triggers the `"Error"` state, colors the result in warning red, and disables further operator chaining until reset.
- **Session Continuity**: `onSaveInstanceState` and `onRestoreInstanceState` preserve active expressions, result values, and error flags during device rotation.

---

## 🧪 Unit Test Coverage (`CalculatorEngineTest.java`)

A complete JUnit test suite verifies all critical functionality:
- **`testBasicAddition`**: Tests integer and multi-digit addition (`3 + 4 = 7`, `45 + 55 = 100`).
- **`testBasicSubtraction`**: Tests positive and negative differences (`5 − 3 = 2`, `2 − 10 = -8`).
- **`testBasicMultiplication`**: Tests products and zero multiplication (`6 × 4 = 24`, `0 × 100 = 0`).
- **`testBasicDivision`**: Tests exact and floating point quotients (`20 ÷ 4 = 5`, `5 ÷ 2 = 2.5`).
- **`testDivisionByZeroShowsError`**: Asserts that `10 ÷ 0`, `0 ÷ 0`, and `100 + 4 ÷ 0` return `"Error"`.
- **`testOperatorPrecedence`**: Verifies `2 + 3 × 4 = 14` (not 20) and `10 − 4 ÷ 2 = 8`.
- **`testDecimalArithmetic`**: Asserts `0.1 + 0.2 = 0.3` without IEEE-754 precision artifacts.
- **`testNegativeNumbers`**: Asserts `-5 + 7 = 2` and `5 × -3 = -15`.
- **`testTrailingOperatorDefensiveHandling`**: Asserts `5 +` evaluates to `5`.
- **`testEmptyOrWhitespaceExpression`**: Confirms empty and null inputs safely return `0`.

---

## 🚀 How to Open and Run in Android Studio

1. **Launch Android Studio** (Hedgehog, Iguana, Jellyfish, or newer recommended).
2. Click **File** ➔ **Open...**
3. Navigate to and select the directory:
   ```
   C:\Users\JANUDAYA\CalculatorApp
   ```
4. Allow Android Studio to sync Gradle dependencies.
5. Select an Android Virtual Device (AVD) or connect a physical device (API 24+).
6. Click **Run** (`Shift + F10`) to build and launch the app!
