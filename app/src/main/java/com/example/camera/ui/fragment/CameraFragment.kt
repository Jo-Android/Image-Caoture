package com.example.camera.ui.fragment

import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.core.view.isVisible
import com.example.camera.R
import com.example.camera.databinding.FragmentCameraBinding
import com.example.camera.databinding.LayoutButtonBinding
import com.example.camera.databinding.LayoutExposureBinding
import com.example.camera.ui.custom.dialog.getDisplaySize
import com.example.camera.ui.manager.camera.CameraManager
import com.example.camera.ui.manager.fragment.RequestPermissionFragment
import com.example.camera.ui.model.DimensionSize
import com.example.camera.ui.viewModel.CameraViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class CameraFragment : RequestPermissionFragment<FragmentCameraBinding>(
    FragmentCameraBinding::inflate
) {
    private lateinit var screenSize: DimensionSize
    private val viewModel: CameraViewModel by viewModel()
    private var cameraManager: CameraManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screenSize = getDisplaySize(requireActivity())
    }

    override fun initLayout() {
        with(binding.cameraOptions) {
            EVRoot.setup()
            previousLL.root.isVisible = false
        }
        initButton()
        setClickListener()
    }

    private fun LayoutExposureBinding.setup() {
        slider.addOnChangeListener { _, value, _ ->
            cameraManager?.changeExposure(value)
        }
        messageId.text = getString(R.string.exposure)
    }

    override fun onResume() {
        super.onResume()
        cameraManager?.changeExposure(binding.cameraOptions.EVRoot.slider.value)
    }

    private fun initButton() {
        with(binding.cameraOptions) {
            cameraPosition.setup(R.drawable.camera_switch, R.string.back_camera)
            flash.setup(R.drawable.flash_auto, R.string.flash_auto)
        }
    }


    private fun setClickListener() {
        with(binding.cameraOptions) {
            imageCaptureButton.setOnClickListener {
                getCameraManager().takePhoto(requireContext())
            }
        }
        setupCameraMenuOptionsClickListener()
    }

    private fun getCameraManager(): CameraManager {
        return if (cameraManager == null) {
            cameraManager = CameraManager(viewModel, binding) { name, location ->
                Log.d("CameraFragment","Image Captured name $name location $location")
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(
                        requireContext(),
                        "Image Captured Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            cameraManager!!
        } else
            cameraManager!!
    }

    private fun setupCameraMenuOptionsClickListener() {
        with(binding.cameraOptions) {
            flash.root.setOnClickListener {
                viewModel.FLASH_MODE = when (viewModel.FLASH_MODE) {
                    ImageCapture.FLASH_MODE_ON -> {
                        flash.button.setImageResource(R.drawable.flash_off_white_24dp)
                        flash.description.text = getString(R.string.flash_off)
                        ImageCapture.FLASH_MODE_OFF
                    }

                    ImageCapture.FLASH_MODE_AUTO -> {
                        flash.button.setImageResource(R.drawable.ic_flash)
                        flash.description.text = getString(R.string.flash_on)
                        ImageCapture.FLASH_MODE_ON
                    }

                    else -> {
                        flash.button.setImageResource(R.drawable.flash_auto)
                        flash.description.text = getString(R.string.flash_auto)
                        ImageCapture.FLASH_MODE_AUTO
                    }
                }
                startCamera()
            }

            cameraPosition.root.setOnClickListener {
                viewModel.cameraSelector =
                    if (viewModel.cameraSelector == CameraSelector.LENS_FACING_FRONT) {
                        flash.root.visibility = VISIBLE
                        cameraPosition.description.text = getString(R.string.back_camera)
                        CameraSelector.LENS_FACING_BACK
                    } else {
                        flash.root.visibility = INVISIBLE
                        cameraPosition.description.text = getString(R.string.front_camera)
                        CameraSelector.LENS_FACING_FRONT
                    }
                startCamera()
            }
        }
    }

    private fun startCamera() {
        Log.d("TAG", "Screen Size ${screenSize.width} x ${screenSize.height}")
        getCameraManager().bindCameraUseCases(
            requireContext(), viewLifecycleOwner,
            Size(screenSize.width, screenSize.height)
        )
    }


    private fun LayoutButtonBinding.setup(imageRes: Int, message: Int) {
        button.setImageResource(imageRes)
        description.text = getString(message)
    }

    override fun onStorageGranted() {}

    override fun onCameraGranted() {
        getCameraManager().setupView(
            requireContext(),
            this,
            Size(screenSize.width, screenSize.height)
        )
    }

    override fun onBackPressed() {
        handelBackPressed()
    }

    override fun getPermission() {
        requestCameraPermission()
    }
}