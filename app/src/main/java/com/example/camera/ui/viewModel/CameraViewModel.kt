package com.example.camera.ui.viewModel

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel

class CameraViewModel() : ViewModel() {
    var cameraSelector = CameraSelector.LENS_FACING_BACK
    var FLASH_MODE: Int = ImageCapture.FLASH_MODE_AUTO
}