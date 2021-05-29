package com.rohitthebest.work_manager_codelabs

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import com.rohitthebest.work_manager_codelabs.databinding.ActivityBlurBinding
import timber.log.Timber

private const val TAG = "BlurActivity"

class BlurActivity : AppCompatActivity() {

    private lateinit var viewModel: BlurViewModel
    private lateinit var binding: ActivityBlurBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlurBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Get the ViewModel
        viewModel = ViewModelProvider(this).get(BlurViewModel::class.java)

        val imageUriExtra = intent.getStringExtra(KEY_IMAGE_URI)
        viewModel.setImageUri(imageUriExtra)
        viewModel.imageUri?.let { imageUri ->
            Glide.with(this).load(imageUri).into(binding.imageView)
        }

        binding.goButton.setOnClickListener {

            viewModel.applyBlur(blurLevel)
        }

        viewModel.outputWorkInfos.observe(this, outputObserver())
        viewModel.progressWorkInfos.observe(this, progressObserver())

        binding.seeFileButton.setOnClickListener {

            viewModel.outputUri?.let { currentUri ->

                val actionView = Intent(Intent.ACTION_VIEW, currentUri)

                actionView.resolveActivity(packageManager)?.run {
                    startActivity(actionView)
                }
            }
        }

        binding.cancelButton.setOnClickListener {

            viewModel.cancelWork()
        }

    }

    private fun progressObserver(): Observer<List<WorkInfo>> {

        return Observer { listOfWorkInfo ->

            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            listOfWorkInfo.forEach {workInfo ->

                if(WorkInfo.State.RUNNING == workInfo.state) {

                    val progress = workInfo.progress.getInt(PROGRESS, 0)
                    binding.progressBar.progress = progress
                }
            }


        }
    }

    private fun outputObserver(): Observer<List<WorkInfo>> {

        return Observer { listOfWorkInfos ->

            // Note that these next few lines grab a single WorkInfo if it exists
            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.

            // If there is no matching work info, do nothing
            if (listOfWorkInfos.isNullOrEmpty()) {

                return@Observer
            }

            // We only care about the one output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfos[0]

            if (workInfo.state.isFinished) {

                showWorkFinished()

                // Normally this processing, which is not directly related to drawing views on
                // screen would be in the ViewModel. For simplicity we are keeping it here.
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)

                Timber.i("workInfosObserver: $outputImageUri")

                // If there is an output file show "See File" button
                if (!outputImageUri.isNullOrEmpty()) {

                    Timber.i("output image uri is not null")
                    viewModel.setOutputUri(outputImageUri)
                    binding.seeFileButton.visibility = View.VISIBLE
                }

            } else {

                showWorkInProgress()
            }

        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
            progressBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }


    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
            progressBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            goButton.visibility = View.VISIBLE
            progressBar.progress = 0
        }
    }

    private val blurLevel: Int
        get() =
            when (binding.radioBlurGroup.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }


}