package com.shervarly.lena.goalplanner

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun checkPage(view: View) {
        val buttonText = findViewById<Button>(view.id).text
        val intent = Intent(this, CategoryActivity::class.java)


        when (buttonText) {
            TimeFrame._12Weeks.value -> intent.putExtra(TIME_FRAME, TimeFrame._12Weeks.value)
            TimeFrame._3Years.value -> intent.putExtra(TIME_FRAME, TimeFrame._3Years.value)
            TimeFrame._10Years.value -> intent.putExtra(TIME_FRAME, TimeFrame._10Years.value)
            else -> ""
        }
        startActivity(intent)
    }
}
