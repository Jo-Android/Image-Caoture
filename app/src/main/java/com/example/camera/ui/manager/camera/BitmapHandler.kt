package com.example.camera.ui.manager.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.FileOutputStream


object BitmapHandler {

    fun Bitmap.rotateImage(output: String): Bitmap {
        val exif = ExifInterface(output)
        val transformation = decodeExifOrientation(
            exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_ROTATE_90
            )
        )
        return Bitmap.createBitmap(
            BitmapFactory.decodeFile(output),
            0,
            0,
            width,
            height,
            transformation,
            true
        )
    }

    private fun decodeExifOrientation(orientation: Int): Matrix {
        val matrix = Matrix()

        // Apply transformation corresponding to declared EXIF orientation
        when (orientation) {
            ExifInterface.ORIENTATION_NORMAL -> Unit
            ExifInterface.ORIENTATION_UNDEFINED -> Unit
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270F)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1F, 1F)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1F, -1F)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postScale(-1F, 1F)
                matrix.postRotate(270F)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postScale(-1F, 1F)
                matrix.postRotate(90F)
            }

            // Error out if the EXIF orientation is invalid
            else -> throw IllegalArgumentException("Invalid orientation: $orientation")
        }

        // Return the resulting matrix
        return matrix
    }

    fun Bitmap.saveToGallery(
        path: String,
        onImageSaved: () -> Unit
    ): Bitmap {

        try {
            val fileOutputStream =
                FileOutputStream(path)
            this.compress(Bitmap.CompressFormat.JPEG, 50, fileOutputStream)
            fileOutputStream.close()
            onImageSaved.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this@saveToGallery
    }
}