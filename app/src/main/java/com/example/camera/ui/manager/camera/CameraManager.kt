package com.example.camera.ui.manager.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.util.Log
import android.util.Size
import android.view.ScaleGestureDetector
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import com.example.camera.R
import com.example.camera.databinding.FragmentCameraBinding
import com.example.camera.ui.manager.camera.BitmapHandler.rotateImage
import com.example.camera.ui.manager.camera.BitmapHandler.saveToGallery
import com.example.camera.ui.viewModel.CameraViewModel
import com.google.android.material.slider.Slider
import org.koin.core.component.KoinComponent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt


class CameraManager(
    private val viewModel: CameraViewModel,
    private val binding: FragmentCameraBinding,
    private val onImageSave: (name: String, location: String) -> Unit
) : KoinComponent {

    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var imageCaptureBuilder: ImageCapture.Builder? = null
    private var preview: Preview? = null
    private var previewBuilder: Preview.Builder? = null
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null

    fun setupView(
        context: Context,
        owner: LifecycleOwner,
        resolution: Size
    ) {
        if (::cameraExecutor.isInitialized)
            cameraExecutor.shutdown()
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.viewFinder.post {

            // Keep track of the display in which this view is attached
//           val  displayId = binding.viewFinder.display.displayId

            // Bind all camera use cases
            bindCameraUseCases(context, owner, resolution)
        }
    }

    fun takePhoto(
        context: Context
    ) {
        imageCapture?.let { imageCapture ->

            // Create output file to hold the image

            val name = generateName()
            //Set Image Location
            val photoFile = File(context.dataDir, name)

            // Setup image capture metadata
            val metadata = ImageCapture.Metadata().apply {

                // Mirror image when using the front camera
                isReversedHorizontal =
                    viewModel.cameraSelector == CameraSelector.LENS_FACING_FRONT
            }

            // Create output options object which contains file + metadata
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .setMetadata(metadata)
                .build()

            // Setup image capture listener which is triggered after photo has been taken
            cameraExecutor.let {
                imageCapture.takePicture(
                    outputOptions,
                    it,
                    object : ImageCapture.OnImageSavedCallback {

                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                            Log.e("TAG", "Photo capture succeeded: $savedUri")


                            /*
                            // If the folder selected is an external media directory, this is
                            // unnecessary but otherwise other apps will not be able to access our
                            // images unless we scan them using [MediaScannerConnection]
                            val mimeType = MimeTypeMap.getSingleton()
                                .getMimeTypeFromExtension(savedUri.toFile().extension)
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(savedUri.toString()),
                                arrayOf(mimeType)
                            ) { _, uri ->
                                Log.d("TAG", "Image capture scanned into media store: $uri")
                            }*/
                            generateCapturedPhoto(
                                savedUri.path ?: "",
                                context,
                                name
                            )
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("TAG", "Photo capture failed: ${exception.message}")
                        }
                    })
            }
        }
    }

    private fun generateName(): String {
        return SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + ".jpeg"
    }

    fun bindCameraUseCases(context: Context, owner: LifecycleOwner, screenAspectRatio: Size) {

        // Get screen metrics used to setup camera for full screen resolution
        /*val metrics = DisplayMetrics().also { binding.viewFinder.display.getRealMetrics(it) }
        Log.e("TAG", "Screen metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        Log.e("TAG", "Preview aspect ratio: $screenAspectRatio")*/

        val rotation = binding.viewFinder.display.rotation

        // Bind the CameraProvider to the LifeCycleOwner
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(viewModel.cameraSelector).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            // CameraProvider
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            previewBuilder = Preview.Builder()
                // We request aspect ratio but no resolution
                .setTargetResolution(screenAspectRatio)
                // Set initial target rotation
                .setTargetRotation(rotation)

            preview = previewBuilder?.build()

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(binding.viewFinder.surfaceProvider)

            // ImageCapture
            imageCaptureBuilder = ImageCapture.Builder()
                // Set Flash Mode
                .setFlashMode(viewModel.FLASH_MODE)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                // We request aspect ratio but no resolution to match preview config, but letting
                // CameraX optimize for whatever specific resolution best fits our use cases
                .setTargetResolution(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)

            imageCapture = imageCaptureBuilder?.build()

            // ImageAnalysis
            imageAnalyzer = ImageAnalysis.Builder()
                // We request aspect ratio but no resolution
                .setTargetResolution(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                .setTargetRotation(rotation)
                .build()
            // The analyzer can then be assigned to the instance
            /*.also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    // Values returned from our analyzer are passed to the attached listener
                    // We log image analysis results here - you should do something useful
                    // instead!
                })
            }*/

            // Must unbind the use-cases before rebinding them
            cameraProvider.unbindAll()

            try {

                //enableExtensionFeature(cameraSelector)

                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                camera =
                    cameraProvider.bindToLifecycle(owner, cameraSelector, preview, imageCapture)
                        .also {
                            enableFocusZoomFeature(context)
                            binding.cameraOptions.EVRoot.slider.setup(it)
                        }
            } catch (exception: Exception) {
                Log.e("TAG", "Use case binding failed: ${exception.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun enableFocusZoomFeature(context: Context) {
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
                val delta = detector.scaleFactor
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(context, listener)

        binding.viewFinder.setOnTouchListener { _, event ->
            if (event.pointerCount == 1) { // Enable Focus
                val factory = binding.viewFinder.meteringPointFactory
                val point = factory.createPoint(event.x, event.y)
                val action = FocusMeteringAction.Builder(point).build()
                camera?.cameraControl?.startFocusAndMetering(action)
            } else // Enable Zoom
                scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    private fun Slider.setup(camera: Camera) {
        camera.cameraInfo.exposureState.let {
            if (isExposureSupported(camera)) {
                binding.cameraOptions.EVRoot.root.isVisible = true
                value = it.exposureCompensationIndex.toFloat()
                valueFrom = it.exposureCompensationRange.lower.toFloat()
                valueTo = it.exposureCompensationRange.upper.toFloat()
                setLabelFormatter { value: Float ->
                    "%.2f".format((value * it.exposureCompensationStep.toFloat()))
                }
            } else {
                binding.cameraOptions.EVRoot.root.isVisible = false
                Log.e("Exposure State", "This Device don't Support Changing Exposure State")
            }
        }
    }

    private fun isExposureSupported(camera: Camera): Boolean {
        return camera.cameraInfo.exposureState.isExposureCompensationSupported
    }

    private fun generateCapturedPhoto(
        location: String?,
        context: Context,
        name: String,
    ) {
        if (location != null)
            saveImage(name, location)
        else {
            context.getString(R.string.error_catching_camera_photo).apply {
                Log.e("TAG", this)
                Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImage(
        name: String,
        location: String
    ) {
        val msg = "Photo capture succeeded: $location"
        Log.d("TAG", msg)
        BitmapFactory.decodeFile(location).rotateImage(location).saveToGallery(location) {
            onImageSave.invoke(name, location)
        }
    }

    fun changeExposure(value: Float) {
        camera.apply {
            if (this != null)
                cameraControl.setExposureCompensationIndex(value.roundToInt())
            else
                Log.e("Camera Manager", "Couldn't Change Exposure Value. Camera is Null")
        }
    }

    companion object {
        const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

}