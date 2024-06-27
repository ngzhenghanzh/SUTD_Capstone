package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class ThirdActivity : ComponentActivity() {
    private var visibilitySetTime: Long = 0
    private var invisibilitySetTime: Long = 0
    private var dotVisibleTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val backClick = findViewById<Button>(R.id.backButton)
        backClick.text = getString(R.string.back)
        val stopDot = findViewById<Button>(R.id.stopButton)
        stopDot.text = getString(R.string.stop)
        val showDot = findViewById<View>(R.id.red_dot)

        val visibilityTimeTextView = findViewById<TextView>(R.id.visibilityTimeTextView)
        visibilityTimeTextView.text = getString(R.string.display_latency)
        val invisibilityTimeTextView = findViewById<TextView>(R.id.invisibilityTimeTextView)
        invisibilityTimeTextView.text = getString(R.string.touch_handling)
        val reactionTimeTextView = findViewById<TextView>(R.id.reactionTimeTextView)
        reactionTimeTextView.text = getString(R.string.reaction_time)

        backClick.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Handler func used to delay the appearance of red dot, and is set to appear only after 2000ms
        Handler(Looper.getMainLooper()).postDelayed({
            visibilitySetTime = System.nanoTime() // Record the time when visibility is set
            showDot.visibility = View.VISIBLE

            // Measure the time when the view is actually rendered as visible
            showDot.post {
                val drawTime = System.nanoTime() //ui system flag for dot to appear
                val timeTaken = (drawTime - visibilitySetTime) / 1000000 // Convert from nano to milliseconds
                Log.d("VisibilityTiming", "Time taken for red dot to appear: ${timeTaken}ms")
                visibilityTimeTextView.text ="Time to appear: ${timeTaken}ms"

                // Record the time when the red dot becomes visible
                dotVisibleTime = System.nanoTime()
            }
        }, 2000)

        stopDot.setOnClickListener {
            invisibilitySetTime = System.nanoTime() // Record the time when invisibility is set
            showDot.visibility = View.INVISIBLE


            showDot.post {
                val disappearTime = System.nanoTime() //ui system flag for dot to disappear
                val timeTaken = (disappearTime - invisibilitySetTime) / 1000000 // Convert from nano to milliseconds
                Log.d("VisibilityTiming", "Time taken for reddot to disappear: ${timeTaken}ms") // Measure the time when the view is actually gone by finding the latency
                invisibilityTimeTextView.text = "Time to disappear: ${timeTaken}ms"
            }

            // Calculate and display the reaction time
            val reactionTime = (invisibilitySetTime - dotVisibleTime) / 1000000 // Convert to milliseconds
            Log.d("ReactionTiming", "User reaction time: ${reactionTime}ms")
            reactionTimeTextView.text = "Reaction Time: ${reactionTime}ms"
        }
    }
}
