package com.example.myapplication.utils

import android.util.Log

object DataCloudUpload {
        fun uploadDataToCloud(
            reactionTime: String,
            renderingDelay: String,
            inputLatency: String
        ) {
            // Simulate uploading data to cloud
            Log.d(
                "ThirdActivity",
                "Uploading data to cloud: Reaction Time = $reactionTime, Rendering Delay = $renderingDelay, Input Latency = $inputLatency"
            )
            // Actual upload logic would go here (e.g., using Retrofit, Firebase, etc.)
        }
}