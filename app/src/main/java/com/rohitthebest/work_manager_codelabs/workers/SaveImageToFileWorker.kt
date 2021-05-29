package com.rohitthebest.work_manager_codelabs.workers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.rohitthebest.work_manager_codelabs.KEY_IMAGE_URI
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "SaveImageToFileWorker"

/**
 * Saves the image to a permanent file
 */
class SaveImageToFileWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    private val title = "Blurred Image"
    private val dateFormatter = SimpleDateFormat(
        "yyyy.MM.dd 'at' HH:mm:ss z",
        Locale.getDefault()
    )


    @SuppressLint("RestrictedApi")
    override fun doWork(): Result {

        // Makes a notification when the work starts and slows down the work so that
        // it's easier to see each WorkRequest start, even on emulated devices
        makeStatusNotification("Saving image", applicationContext)
        sleep()

        val resolver = applicationContext.contentResolver

        return try {

            val resourceUri = inputData.getString(KEY_IMAGE_URI)

            val bitmap = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )


            val imageCollection = if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, title)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.WIDTH, bitmap.width)
                put(MediaStore.Images.Media.HEIGHT, bitmap.height)
            }

            var output : Data? = null

            resolver.insert(imageCollection, contentValues)?.also {
                resolver.openOutputStream(it).use { outputStream ->

                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }else {

                        output = workDataOf(KEY_IMAGE_URI to it.toString())

                        makeStatusNotification("Image saved", applicationContext)

                        Log.i(TAG,"image output uri $it")

                        Result.Success(output!!)
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")

            if(output != null) {
                Result.Success(output!!)
            }else {
                Result.failure()
            }
        } catch (e: Exception) {

            e.printStackTrace()

            Timber.e(e)
            Result.failure()
        }
    }


}