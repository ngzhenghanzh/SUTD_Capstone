package com.example.myapplication.utils

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.database

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
        val database =
            Firebase.database("https://htx-cognitive-app-default-rtdb.asia-southeast1.firebasedatabase.app")
        // Use push() to create a unique key for each new reaction time entry
        val reactionTimeRef = database.getReference("reaction_times").push()
        val reactionTimeData = mapOf(
            "reaction_time" to reactionTime,
            "timestamp" to System.currentTimeMillis()
        )

        // Save the reaction time data under the new key
        reactionTimeRef.setValue(reactionTimeData)
    }
}