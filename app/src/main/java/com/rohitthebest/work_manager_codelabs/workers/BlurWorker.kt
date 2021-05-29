package com.rohitthebest.work_manager_codelabs.workers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.rohitthebest.work_manager_codelabs.KEY_IMAGE_URI
import com.rohitthebest.work_manager_codelabs.PROGRESS
import timber.log.Timber

class BlurWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {

        val appContext = applicationContext

        makeStatusNotification("Blurring Image", appContext)

        val resourceUri = inputData.getString(KEY_IMAGE_URI)

        //sleep()

        (0..100 step 10).forEach {

            setProgressAsync(workDataOf(PROGRESS to it))
            sleep()
        }

        return try {

            if(TextUtils.isEmpty(resourceUri)){

                Timber.e("Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver

            val picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )

            val output = blurBitmap(picture, appContext)

            val outputUri = writeBitmapToFile(appContext, output)

            makeStatusNotification("Output is $outputUri", appContext)

            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString())

            Result.Success(outputData)
        }catch (throwable : Throwable) {

            Timber.e(throwable, "Error applying blur")
            Result.failure()
        }
    }


}