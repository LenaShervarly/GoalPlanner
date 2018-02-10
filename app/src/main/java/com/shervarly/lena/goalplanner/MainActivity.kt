package com.shervarly.lena.goalplanner

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.view.animation.AnimationUtils


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.title = resources.getString(R.string.time_frame_selection)
        }
    }

    fun checkPage(view: View) {
        val animRotate = AnimationUtils.loadAnimation(this, R.anim.abc_shrink_fade_out_from_bottom)
        val hexagon: Button = findViewById(view.id)
        val buttonText = findViewById<Button>(view.id).text
        val intent = Intent(this, CategoryActivity::class.java)

        hexagon.startAnimation(animRotate)
        when (buttonText) {
            TimeFrame._12Weeks.value -> intent.putExtra(TIME_FRAME, TimeFrame._12Weeks.value)
            TimeFrame._3Years.value -> intent.putExtra(TIME_FRAME, TimeFrame._3Years.value)
            TimeFrame._10Years.value -> intent.putExtra(TIME_FRAME, TimeFrame._10Years.value)
            TimeFrame._1Week.value -> intent.putExtra(TIME_FRAME, TimeFrame._1Week.value)
            else -> ""
        }
        startActivity(intent)
    }
}
