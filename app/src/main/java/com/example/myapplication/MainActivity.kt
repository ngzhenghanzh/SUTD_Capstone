package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import com.example.myapplication.utils.DoNotDisturb
import com.example.myapplication.utils.NetworkMonitor

class MainActivity : ComponentActivity() {
    private lateinit var doNotDisturb: DoNotDisturb
    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var networkStatusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        welcomeTextView.text = getString(R.string.welcome)

        doNotDisturb = DoNotDisturb(this)
        val dndClick = findViewById<Button>(R.id.startButton)
        dndClick.text = getString(R.string.DND)
        dndClick.setOnClickListener {
            if (!doNotDisturb.isNotificationPolicyAccessGranted()) {
                doNotDisturb.requestNotificationPolicyAccess()
            } else {
                doNotDisturb.enableDoNotDisturb()
                navigateToSecondActivity()
            }
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

    private fun navigateToSecondActivity() {
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
    }
}
