package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class SecondActivity : ComponentActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        welcomeTextView.text = getString(R.string.welcome)

        val startClick = findViewById<Button>(R.id.startButton)
        startClick.text = getString(R.string.start)
        startClick.setOnClickListener {
            intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }

    }
}