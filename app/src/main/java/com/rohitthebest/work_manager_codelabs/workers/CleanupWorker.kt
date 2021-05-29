package com.rohitthebest.work_manager_codelabs.workers

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.rohitthebest.work_manager_codelabs.OUTPUT_PATH
import timber.log.Timber
import java.io.File


/**
 * Cleans up temporary files generated during blurring process
 */
class CleanupWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {

        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Cleaning up old temporary files", applicationContext)
        sleep()

        return try {

            val outputDirectory = File(applicationContext.filesDir, OUTPUT_PATH)

            if (outputDirectory.exists()) {

                val entries = outputDirectory.listFiles()

                if (entries != null) {

                    for (entry in entries) {

                        val name = entry.name

                        if (name.isNotEmpty() && name.endsWith(".png")) {

                            val deleted = entry.delete()
                            Timber.i("Deleted $name - $deleted")

                        }
                    }
                }
            }

            Result.Success()
        } catch (e: Exception) {

            Timber.e(e)
            Result.Failure()
        }

    }
}