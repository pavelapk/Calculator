package ru.pavelapk.calculator

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MainActivity : AppCompatActivity() {

    private val STATE_FIRST = 0
    private val STATE_OPERATION = 1
    private val STATE_SECOND = 2

    lateinit var tv_numbers: TextView
    lateinit var iv_explosion: ImageView
    lateinit var decimalFormat: DecimalFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_numbers = findViewById(R.id.tv_numbers)
        iv_explosion = findViewById(R.id.iv_explosion)

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
        if (currentNumber.length < 10) {
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
        "x" -> '*'
        "÷" -> '/'
        else -> 0.toChar()
    }

    private fun calculate() {
        secondNumber = currentNumber.toDouble()
        if (operation == '/' && secondNumber == 0.0) {
            explode()
            return
        }

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

    private fun printNumWithOper() {
        tv_numbers.text =
            decimalFormat.format(firstNumber) + operation
    }

    private fun printNum() {
        currentNumber = decimalFormat.format(firstNumber)
        if (currentNumber.length > 10) {
            clear()
            tv_numbers.text = getString(R.string.error)
        } else {
            tv_numbers.text = currentNumber
        }
    }

    fun onOperationClick(v: View) {
        v as Button
        when (v.text) {
            "AC" -> allClear()
            "+", "-", "x", "÷" -> {
                when (state) {
                    STATE_FIRST -> {
                        operation = chooseOperation(v.text)
                        if (currentNumber.isNotEmpty()) {
                            firstNumber = currentNumber.toDouble()
                            clear()
                            printNumWithOper()
                            state = STATE_OPERATION
                        }
                    }
                    STATE_OPERATION -> {
                        operation = chooseOperation(v.text)
                        printNumWithOper()
                    }
                    STATE_SECOND -> {
                        if (currentNumber.isNotEmpty()) {
                            calculate()
                            clear()
                            operation = chooseOperation(v.text)
                            printNumWithOper()
                            state = STATE_OPERATION
                        }
                    }
                }
            }
            "=" -> {
                if (currentNumber.isNotEmpty() && state == STATE_SECOND) {
                    calculate()
                    printNum()
                    state = STATE_FIRST
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
                    printNum()
                    state = STATE_FIRST
                }
            }
            else -> Toast.makeText(this, v.text, Toast.LENGTH_SHORT).show()
        }

    }


    /// EXPLOSION
    private fun explode() {
        val mp = MediaPlayer.create(this, R.raw.explode_sound)
        mp.start()

        val centerX = resources.displayMetrics.widthPixels / 2f - iv_explosion.width / 2f
        val centerY = resources.displayMetrics.heightPixels / 2f - iv_explosion.height / 2f

        val animX = ObjectAnimator.ofFloat(iv_explosion, "x", centerX)
        val animY = ObjectAnimator.ofFloat(iv_explosion, "y", centerY)
        val animScaleX = ObjectAnimator.ofFloat(iv_explosion, "scaleX", 12f)
        val animScaleY = ObjectAnimator.ofFloat(iv_explosion, "scaleY", 12f)
        val animAlphaOn = ObjectAnimator.ofFloat(iv_explosion, "alpha", 1f)
        val animAlphaOff = ObjectAnimator.ofFloat(iv_explosion, "alpha", 0f)

        animAlphaOn.addListener(onEnd = {
            allClear()
        })
        animAlphaOff.addListener(onEnd = {
            iv_explosion.translationX = 0f
            iv_explosion.translationY = 0f
            iv_explosion.scaleX = 0f
            iv_explosion.scaleY = 0f
        })

        AnimatorSet().apply {
            duration = 700
            playTogether(animX, animY, animScaleX, animScaleY, animAlphaOn)
            play(animAlphaOff).after(animAlphaOn)
            start()
        }

    }

}

