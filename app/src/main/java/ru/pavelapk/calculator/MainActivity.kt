package ru.pavelapk.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MainActivity : AppCompatActivity() {

    val STATE_FIRST = 0
    val STATE_OPERATION = 1
    val STATE_SECOND = 2

    lateinit var tv_numbers: TextView
    lateinit var decimalFormat: DecimalFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_numbers = findViewById(R.id.tv_numbers)

        val otherSymbols = DecimalFormatSymbols(Locale.getDefault())
        otherSymbols.decimalSeparator = '.'
        decimalFormat = DecimalFormat("#.######", otherSymbols)
    }


    private var firstNumber = 0.0
    private var operation: Char = 0.toChar()
    private var secondNumber = 0.0
    private var currentNumber = ""
    private var isComma = false
    private var state = STATE_FIRST

    fun onDigitClick(v: View) {
        v as Button
        if (currentNumber.length < 11) {
            when (v.text) {
                "," -> {
                    if (!isComma) {
                        currentNumber += "."
                        isComma = true
                    }
                }
                else -> {
                    if (!(currentNumber == "0" && v.text == "0")) {
                        currentNumber += v.text
                    }
                }
            }
            if (state == STATE_OPERATION) {
                state = STATE_SECOND
            }
        }
        tv_numbers.text = currentNumber
    }

    private fun clear() {
        currentNumber = ""
        isComma = false
    }

    private fun allClear() {
        clear()
        firstNumber = 0.0
        secondNumber = 0.0
        state = STATE_FIRST
        tv_numbers.text = currentNumber
    }

    private fun chooseOperation(o: CharSequence) = when (o) {
        "+" -> '+'
        "-" -> '-'
        "X" -> '*'
        "÷" -> '/'
        else -> 0.toChar()
    }

    fun calculate() {
        secondNumber = currentNumber.toDouble()
        firstNumber = when (operation) {
            '+' -> firstNumber + secondNumber
            '-' -> firstNumber - secondNumber
            '*' -> firstNumber * secondNumber
            '/' -> firstNumber / secondNumber
            else -> 0.0
        }
    }

    private fun calculateWithPercent() {
        secondNumber = currentNumber.toDouble() / 100.0
        firstNumber = when (operation) {
            '+' -> firstNumber * (1 + secondNumber)
            '-' -> firstNumber * (1 - secondNumber)
            '*' -> firstNumber * secondNumber
            '/' -> firstNumber / secondNumber
            else -> 0.0
        }
    }

    private fun setTextNumWithOper() {
        tv_numbers.text =
            decimalFormat.format(firstNumber) + operation
    }

    fun onOperationClick(v: View) {
        v as Button
        when (v.text) {
            "AC" -> allClear()
            "+", "-", "X", "÷" -> {
                when (state) {
                    STATE_FIRST -> {
                        operation = chooseOperation(v.text)
                        if (currentNumber.isNotEmpty()) {
                            firstNumber = currentNumber.toDouble()
                            clear()
                            setTextNumWithOper()
                            state = STATE_OPERATION
                        }
                    }
                    STATE_OPERATION -> {
                        operation = chooseOperation(v.text)
                        setTextNumWithOper()
                    }
                    STATE_SECOND -> {
                        if (currentNumber.isNotEmpty()) {
                            calculate()
                            clear()
                            operation = chooseOperation(v.text)
                            setTextNumWithOper()
                            state = STATE_OPERATION
                        }
                    }
                }
            }
            "=" -> {
                if (currentNumber.isNotEmpty() && state == STATE_SECOND) {
                    calculate()
                    currentNumber = decimalFormat.format(firstNumber)
                    if (currentNumber.length > 11) {
                        clear()
                        tv_numbers.text = getString(R.string.error)
                    } else {
                        tv_numbers.text = currentNumber
                        state = STATE_FIRST
                    }
                }
            }
            "+/-" -> {
                if (currentNumber.isNotEmpty()) {
                    currentNumber = decimalFormat.format(-currentNumber.toDouble())
                    tv_numbers.text = currentNumber
                }
            }
            "%" -> {
                if (currentNumber.isNotEmpty() && state == STATE_SECOND) {
                    calculateWithPercent()
                    currentNumber = decimalFormat.format(firstNumber)
                    if (currentNumber.length > 11) {
                        clear()
                        tv_numbers.text = getString(R.string.error)
                    } else {
                        tv_numbers.text = currentNumber
                        state = STATE_FIRST
                    }
                }
            }
            else -> Toast.makeText(this, v.text, Toast.LENGTH_SHORT).show()
        }

    }

}

