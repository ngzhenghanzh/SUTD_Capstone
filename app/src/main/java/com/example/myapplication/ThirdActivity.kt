package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.utils.DataCloudUpload
import com.example.myapplication.utils.DataUploadWorker
import com.example.myapplication.utils.NetworkMonitor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class ThirdActivity : ComponentActivity() {
    private var buttonAppearRequestedTime: Long = 0
    private var buttonActualAppearTime: Long = 0
    private var buttonClickTime: Long = 0
    private var buttonDisappearTime: Long = 0

    private lateinit var showDot: View
    private lateinit var visibilityTimeTextView: TextView
    private lateinit var invisibilityTimeTextView: TextView
    private lateinit var reactionTimeTextView: TextView

    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var networkStatusTextView: TextView

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (buttonAppearRequestedTime > 0) {
                buttonAppearRequestedTime = System.nanoTime()
                runOnUiThread { showDot.visibility = View.VISIBLE }
                buttonActualAppearTime = System.nanoTime()
                val latencyTime = (buttonActualAppearTime - buttonAppearRequestedTime) / 1_000_000.0
                Log.d("ThirdActivity", "Rendering Delay: $latencyTime ms")
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
                    Log.d("ThirdActivity", "Button Click Time: $buttonClickTime ms")
                    val reactionDuration = (buttonClickTime - buttonActualAppearTime) / 1_000_000.0
                    Log.d("ThirdActivity", "Reaction Duration: $reactionDuration ms")
                    reactionTimeTextView.text = "Reaction Time: ${reactionDuration} ms"

                    runOnUiThread {
                        showDot.visibility = View.GONE
                        buttonDisappearTime = System.nanoTime()

                        val disappearDuration =
                            (buttonDisappearTime - buttonClickTime) / 1_000_000.0
                        Log.d("ThirdActivity", "Touch Event Handling: $disappearDuration ns")
                        invisibilityTimeTextView.text =
                            "Touch event handling (input latency): ${disappearDuration} ms"
                    }

                    handleDataUploadOrStorage(
                        reactionTimeTextView.text.toString(),
                        visibilityTimeTextView.text.toString(),
                        invisibilityTimeTextView.text.toString()
                    )
                }

            } catch (e: Exception) {
                Log.e("ThirdActivity", "Error in coroutine", e)
            }
        }

        backButton.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        networkMonitor = NetworkMonitor(this)
        networkStatusTextView = findViewById(R.id.networkStatusTextView)
        // Observe the network status
        networkMonitor.observe(this, Observer { isConnected ->
            if (isConnected) {
                networkStatusTextView.text = "Network status: Connected"
                uploadPendingData()
            } else {
                networkStatusTextView.text = "Network status: Disconnected"
            }
        })
    }

    private fun handleDataUploadOrStorage(
        reactionTime: String,
        renderingDelay: String,
        inputLatency: String
    ) {
        if (networkMonitor.value == true) {
            DataCloudUpload.uploadDataToCloud(reactionTime, renderingDelay, inputLatency)
        } else {
            storeDataLocally(reactionTime, renderingDelay, inputLatency)
        }
    }

    private fun storeDataLocally(
        reactionTime: String,
        renderingDelay: String,
        inputLatency: String
    ) {
        val sharedPreferences = getSharedPreferences("local_data", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("reactionTime", reactionTime.toString())
        editor.putString("renderingDelay", renderingDelay)
        editor.putString("inputLatency", inputLatency)
        editor.apply()

        Log.d(
            "ThirdActivity",
            "Storing data locally: Reaction Time = $reactionTime, Rendering Delay = $renderingDelay, Input Latency = $inputLatency"
        )
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "triggered")
        if (networkMonitor.value == true) {
            uploadPendingData()
        }
    }

    private fun uploadPendingData() {
        val sharedPreferences = getSharedPreferences("local_data", MODE_PRIVATE)
        val reactionTime = sharedPreferences.getString("reactionTime", null)
        val renderingDelay = sharedPreferences.getString("renderingDelay", null)
        val inputLatency = sharedPreferences.getString("inputLatency", null)

        if (reactionTime != null && renderingDelay != null && inputLatency != null) {
            DataCloudUpload.uploadDataToCloud(reactionTime, renderingDelay, inputLatency)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("onPause", "triggered")
        scheduleUploadWorker()
    }

    private fun scheduleUploadWorker() {
        val uploadWorkRequest = OneTimeWorkRequestBuilder<DataUploadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueue(uploadWorkRequest)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the frame callback to avoid memory leaks
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}