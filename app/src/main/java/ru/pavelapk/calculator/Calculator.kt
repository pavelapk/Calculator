package ru.pavelapk.calculator

import ru.pavelapk.calculator.CalcOperation.*
import ru.pavelapk.calculator.MathOperation.*
import ru.pavelapk.calculator.State.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class Calculator {

    companion object {
        const val MAX_LENGTH = 10
        const val COMMA = ","
        const val DOT = "."
        const val FORMAT_PATTERN = "0" // 0 means number
        const val ZERO = "0"
    }

    private var firstNumber = 0.0
    private var operation: MathOperation? = null
    private var secondNumber = 0.0
    private var currentNumber = ""
    private var isComma = false
    private var state = STATE_FIRST

    private val df = DecimalFormat(
        FORMAT_PATTERN,
        DecimalFormatSymbols.getInstance(Locale.ENGLISH)
    ).apply { maximumFractionDigits = MAX_LENGTH }

    fun getState() = state

    fun getOperationStr() = operation?.screenStr ?: ""

    fun onDigitClick(text: CharSequence): String {
        if (state == STATE_OPERATION) {
            state = STATE_SECOND
            clear()
        }
        if (currentNumber.length < MAX_LENGTH) {
            when (text) {
                COMMA -> {
                    if (!isComma) {
                        currentNumber += DOT
                        isComma = true
                    }
                }
                else -> {
                    if (!(currentNumber == ZERO && text == ZERO)) {
                        currentNumber += text
                    }
                }
            }

        }
        return currentNumber
    }

    private fun clear() {
        currentNumber = ""
        isComma = false
    }

    fun allClear() {
        clear()
        firstNumber = 0.0
        secondNumber = 0.0
        state = STATE_FIRST
    }

    private fun chooseMathOperation(o: CharSequence) =
        MathOperation.values().firstOrNull { it.btnStr == o }

    private fun chooseCalcOperation(o: CharSequence) =
        CalcOperation.values().firstOrNull { it.btnStr == o }


    private fun calculate(): Boolean {
        if (operation == DIV && secondNumber == 0.0) {
            return false
        }
        firstNumber = when (operation) {
            PLUS -> firstNumber + secondNumber
            MINUS -> firstNumber - secondNumber
            MULT -> firstNumber * secondNumber
            DIV -> firstNumber / secondNumber
            else -> 0.0
        }
        currentNumber = df.format(firstNumber)
        return true
    }


    private fun calculateWithPercent(): Boolean {
        if (operation == DIV && secondNumber == 0.0) {
            return false
        }
        secondNumber /= 100
        firstNumber = when (operation) {
            PLUS -> firstNumber * (1 + secondNumber)
            MINUS -> firstNumber * (1 - secondNumber)
            MULT -> firstNumber * secondNumber
            DIV -> firstNumber / secondNumber
            else -> 0.0
        }
        currentNumber = df.format(firstNumber)
        return true
    }

    private fun doMathOperation(mathOperation: MathOperation?): Boolean {
        when (state) {
            STATE_FIRST -> {
                operation = mathOperation
                if (currentNumber.isNotEmpty()) {
                    firstNumber = currentNumber.toDouble()
                    currentNumber = df.format(firstNumber)
                    state = STATE_OPERATION
                }
            }
            STATE_OPERATION -> {
                operation = mathOperation
            }
            STATE_SECOND -> {
                if (currentNumber.isNotEmpty()) {
                    secondNumber = currentNumber.toDouble()
                    if (!calculate()) return false
                    operation = mathOperation
                    state = STATE_OPERATION
                }
            }
        }
        return true
    }

    fun onOperationClick(text: CharSequence): String? {
        val mathOperation = chooseMathOperation(text)
        if (mathOperation != null) {
            if (!doMathOperation(mathOperation)) return null
        } else {
            when (chooseCalcOperation(text)) {
                ALL_CLEAR -> allClear()
                EQUALLY -> {
                    if (currentNumber.isNotEmpty() && state == STATE_SECOND) {
                        secondNumber = currentNumber.toDouble()
                        if (!calculate()) return null
                        state = STATE_FIRST
                    }
                }
                PLUS_MINUS -> {
                    if (currentNumber.isNotEmpty()) {
                        currentNumber = df.format(-currentNumber.toDouble())
                    }
                }
                PERCENT -> {
                    if (currentNumber.isNotEmpty() && state == STATE_SECOND) {
                        secondNumber = currentNumber.toDouble()
                        if (!calculateWithPercent()) return null
                        state = STATE_FIRST
                    }
                }
            }
        }
        return currentNumber
    }


}