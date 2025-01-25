package com.example.project400.visuals

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import java.nio.ByteBuffer


import java.io.ByteArrayOutputStream



class YuvToRgbConverter(context: Context) {

    @Synchronized
    fun yuvToRgb(image: Image): Bitmap {
        if (image.format != ImageFormat.YUV_420_888) {
            throw IllegalArgumentException("Unsupported image format: ${image.format}")
        }

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val yBuffer = yPlane.buffer
        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer

        val yRowStride = yPlane.rowStride
        val uvRowStride = uPlane.rowStride
        val uvPixelStride = uPlane.pixelStride

        val width = image.width
        val height = image.height
        val nv21 = ByteArray(width * height * 3 / 2)

        // Copy Y plane
        for (row in 0 until height) {
            yBuffer.position(row * yRowStride)
            yBuffer.get(nv21, row * width, width)
        }

        // Copy UV planes
        val uvHeight = height / 2
        for (row in 0 until uvHeight) {
            for (col in 0 until width / 2) {
                val uvOffset = row * uvRowStride + col * uvPixelStride
                nv21[width * height + row * width + col * 2] = vBuffer[uvOffset]
                nv21[width * height + row * width + col * 2 + 1] = uBuffer[uvOffset]
            }
        }

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val outStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, outStream)
        val jpegByteArray = outStream.toByteArray()

        return BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size)
    }
}