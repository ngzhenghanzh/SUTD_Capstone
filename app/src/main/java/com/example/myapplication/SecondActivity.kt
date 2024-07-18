package com.example.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class SecondActivity : ComponentActivity() {

    private var buttonAppearRequestedTime: Long = 0
    private var buttonActualAppearTime: Long = 0
    private var buttonClickTime: Long = 0
    private var buttonDisappearTime: Long = 0

    private lateinit var showDot: View
    private lateinit var visibilityTimeTextView: TextView
    private lateinit var invisibilityTimeTextView: TextView
    private lateinit var reactionTimeTextView: TextView

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (buttonAppearRequestedTime > 0) {
                buttonAppearRequestedTime = System.nanoTime()
                runOnUiThread{showDot.visibility = View.VISIBLE }
                buttonActualAppearTime = System.nanoTime()
                val latencyTime = (buttonActualAppearTime - buttonAppearRequestedTime) / 1_000_000.0
                Log.d("SecondActivity", "Rendering Delay: $latencyTime ms")
                visibilityTimeTextView.text = "Rendering delay (output latency): ${latencyTime} ms"
                buttonAppearRequestedTime = 0 // Reset after measurement
            }
            // Schedule the next frame callback
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    @OptIn(ExperimentalTime::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        showDot = findViewById(R.id.red_dot)
        showDot.visibility = View.INVISIBLE
        val stopButton = findViewById<Button>(R.id.stopButton)
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.text = getString(R.string.back)
        stopButton.text = getString(R.string.stop)

        visibilityTimeTextView = findViewById(R.id.visibilityTimeTextView)
        visibilityTimeTextView.text = getString(R.string.display_latency)
        invisibilityTimeTextView = findViewById(R.id.invisibilityTimeTextView)
        invisibilityTimeTextView.text = getString(R.string.touch_handling)
        reactionTimeTextView = findViewById(R.id.reactionTimeTextView)
        reactionTimeTextView.text = getString(R.string.reaction_time)

        // Coroutine to manage the stop button visibility
        lifecycleScope.launch {
            try {
                delay(2.seconds)
                buttonAppearRequestedTime = System.nanoTime()
                Choreographer.getInstance().postFrameCallback(frameCallback)

                // Wait until stop button is clicked
                stopButton.setOnClickListener {
                    buttonClickTime = System.nanoTime()
                    Log.d("SecondActivity", "Button Click Time: $buttonClickTime ms")
                    val reactionDuration = (buttonClickTime - buttonActualAppearTime) / 1_000_000.0
                    Log.d("SecondActivity", "Reaction Duration: $reactionDuration ms")
                    reactionTimeTextView.text = "Reaction Time: ${reactionDuration}  ms"

                    runOnUiThread {
                        showDot.visibility = View.GONE
                        buttonDisappearTime = System.nanoTime()

                        val disappearDuration = (buttonDisappearTime - buttonClickTime) / 1_000_000.0
                        Log.d("SecondActivity", "Touch Event Handling: $disappearDuration ns")
                        invisibilityTimeTextView.text = "Touch event handling (input latency): ${disappearDuration} ms"
                    }
                }
            } catch (e: Exception) {
                Log.e("SecondActivity", "Error in coroutine", e)
            }
        }

        backButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the frame callback to avoid memory leaks
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}