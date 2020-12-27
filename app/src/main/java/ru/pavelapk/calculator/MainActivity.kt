package ru.pavelapk.calculator

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.animation.addListener
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val STATE_FIRST = 0
        const val STATE_OPERATION = 1
        const val STATE_SECOND = 2
    }

    private lateinit var tvNumbers: TextView
    private lateinit var ivExplosion: ImageView
    private lateinit var decimalFormat: DecimalFormat

    private fun Group.setAllOnClickListener(listener: View.OnClickListener?) {
        referencedIds.forEach { id ->
            rootView.findViewById<View>(id).setOnClickListener(listener)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Group>(R.id.groupDigits).setAllOnClickListener { v ->
            onDigitClick(v as Button)
        }

        findViewById<Group>(R.id.groupOperations).setAllOnClickListener { v ->
            onOperationClick(v as Button)
        }

        tvNumbers = findViewById(R.id.tv_numbers)
        ivExplosion = findViewById(R.id.iv_explosion)

        val otherSymbols = DecimalFormatSymbols(Locale.getDefault())
        otherSymbols.decimalSeparator = '.'
        decimalFormat = DecimalFormat("#.######", otherSymbols)
    }


    private var firstNumber = 0.0
    private var operation = 0.toChar()
    private var secondNumber = 0.0
    private var currentNumber = ""
    private var isComma = false
    private var state = STATE_FIRST

    private fun onDigitClick(v: Button) {
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
        tvNumbers.text = currentNumber
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
        tvNumbers.text = currentNumber
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

    private fun printNumWithOperation() {
        tvNumbers.text = getString(R.string.concat, decimalFormat.format(firstNumber), operation)
    }

    private fun printNum() {
        currentNumber = decimalFormat.format(firstNumber)
        if (currentNumber.length > 10) {
            clear()
            tvNumbers.text = getString(R.string.error)
        } else {
            tvNumbers.text = currentNumber
        }
    }

    private fun onOperationClick(v: Button) {
        when (v.text) {
            "AC" -> allClear()
            "+", "-", "x", "÷" -> {
                when (state) {
                    STATE_FIRST -> {
                        operation = chooseOperation(v.text)
                        if (currentNumber.isNotEmpty()) {
                            firstNumber = currentNumber.toDouble()
                            clear()
                            printNumWithOperation()
                            state = STATE_OPERATION
                        }
                    }
                    STATE_OPERATION -> {
                        operation = chooseOperation(v.text)
                        printNumWithOperation()
                    }
                    STATE_SECOND -> {
                        if (currentNumber.isNotEmpty()) {
                            calculate()
                            clear()
                            operation = chooseOperation(v.text)
                            printNumWithOperation()
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
                    tvNumbers.text = currentNumber
                }
            }
            "%" -> {
                if (currentNumber.isNotEmpty() && state == STATE_SECOND) {
                    calculateWithPercent()
                    printNum()
                    state = STATE_FIRST
                }
            }
        }

    }


    /// EXPLOSION
    private fun explode() {
        val mp = MediaPlayer.create(this, R.raw.explode_sound)
        mp.start()

        val centerX = resources.displayMetrics.widthPixels / 2f - ivExplosion.width / 2f
        val centerY = resources.displayMetrics.heightPixels / 2f - ivExplosion.height / 2f

        val animX = ObjectAnimator.ofFloat(ivExplosion, "x", centerX)
        val animY = ObjectAnimator.ofFloat(ivExplosion, "y", centerY)
        val animScaleX = ObjectAnimator.ofFloat(ivExplosion, "scaleX", 12f)
        val animScaleY = ObjectAnimator.ofFloat(ivExplosion, "scaleY", 12f)
        val animAlphaOn = ObjectAnimator.ofFloat(ivExplosion, "alpha", 1f)
        val animAlphaOff = ObjectAnimator.ofFloat(ivExplosion, "alpha", 0f)

        animAlphaOn.addListener(onEnd = {
            allClear()
        })
        animAlphaOff.addListener(onEnd = {
            ivExplosion.translationX = 0f
            ivExplosion.translationY = 0f
            ivExplosion.scaleX = 0f
            ivExplosion.scaleY = 0f
        })

        AnimatorSet().apply {
            duration = 700
            playTogether(animX, animY, animScaleX, animScaleY, animAlphaOn)
            play(animAlphaOff).after(animAlphaOn)
            start()
        }

    }

}

