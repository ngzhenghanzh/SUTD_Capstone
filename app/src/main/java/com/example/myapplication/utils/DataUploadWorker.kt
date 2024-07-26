package com.example.myapplication.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DataUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sharedPreferences =
            applicationContext.getSharedPreferences("local_data", Context.MODE_PRIVATE)
        val reactionTime = sharedPreferences.getString("reactionTime", null)
        val renderingDelay = sharedPreferences.getString("renderingDelay", null)
        val inputLatency = sharedPreferences.getString("inputLatency", null)

        if (reactionTime != null && renderingDelay != null && inputLatency != null) {
            // Simulate uploading data to cloud
            try {

                DataCloudUpload.uploadDataToCloud(
                    reactionTime,
                    renderingDelay,
                    inputLatency
                )
                // Actual upload logic would go here (e.g., using Retrofit, Firebase, etc.)

                // Clear the stored data
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                return Result.success()
            } catch (e: Exception) {
                Log.e("DataUploadWorker", "Error uploading data", e)
                return Result.retry()
            }
        } else {
            return Result.success()
        }
    }
}


