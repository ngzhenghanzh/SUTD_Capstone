package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import com.example.myapplication.utils.NetworkMonitor

class SecondActivity : ComponentActivity() {
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var networkStatusTextView: TextView

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

        networkMonitor = NetworkMonitor(this)
        networkStatusTextView = findViewById(R.id.networkStatusTextView)
        // Observe the network status
        networkMonitor.observe(this, Observer { isConnected ->
            if (isConnected) {
                networkStatusTextView.text = "Network status: Connected"
            } else {
                networkStatusTextView.text = "Network status: Disconnected"
            }
        })
    }
}