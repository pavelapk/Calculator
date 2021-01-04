package ru.pavelapk.calculator

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.animation.addListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val logic = Calculator()


    private fun Group.setAllOnClickListener(listener: View.OnClickListener?) {
        referencedIds.forEach { id ->
            rootView.findViewById<View>(id).setOnClickListener(listener)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        groupDigits.setAllOnClickListener { v ->
            printOnScreen(logic.onDigitClick((v as Button).text))
        }

        groupOperations.setAllOnClickListener { v ->
            printOnScreen(logic.onOperationClick((v as Button).text))
        }
    }


    private fun printOnScreen(num: String?) {
        if (num == null) {
            explode()
        } else {

            if (num.length > Calculator.MAX_LENGTH) {
                tvNumbers.text = getString(R.string.trunc_num, num.take(Calculator.MAX_LENGTH - 2))
            } else {
                tvNumbers.text = num
            }

            if (logic.getState() == State.STATE_OPERATION) {
                tvNumbers.append(logic.getOperationStr())
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
            logic.allClear()
            tvNumbers.text = ""
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

