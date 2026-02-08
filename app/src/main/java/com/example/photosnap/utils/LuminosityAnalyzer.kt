package com.example.photosnap.utils


import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class LuminosityAnalyzer(private val listener: (Float) -> Unit) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L

    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()

        // BATTERY SAVER: Only calculate once per second (1000ms)
        if (currentTimestamp - lastAnalyzedTimestamp >= 1000) {

            val buffer = image.planes[0].buffer
            val data = toByteArray(buffer)

            // Convert pixels to brightness (0-255)
            val pixels = data.map { it.toInt() and 0xFF }

            // Calculate Average Brightness
            val average = if (pixels.isNotEmpty()) pixels.average() else 0.0

            // Convert to approximate Lux (x5 multiplier for indoor scaling)
            val multiplier = average / 10.0
            val estimatedLux = average * multiplier

            listener(estimatedLux.toFloat())

            lastAnalyzedTimestamp = currentTimestamp
        }

        image.close()
    }

    private fun toByteArray(buffer: ByteBuffer): ByteArray {
        buffer.rewind()
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        return data
    }
}