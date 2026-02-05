package com.example.photosnap.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class SensorHelper(context: Context): SensorEventListener{

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager;

    private var currentLightLux = 0.0f
    private var currentGyro = "0.0,0.0,0.0"

    fun startListening(){
        val light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        light?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopListening(){
        sensorManager.unregisterListener(this)
    }

    fun getFullMetaDataJson(locationJson: String): String {

        val timestamp = getIsoTimestamp()
        val device = "${Build.MANUFACTURER} ${Build.MODEL}"

        return """
            {
              "timestamp": "$timestamp",
              "lightLux": "$currentLightLux",
              "gyro": "$currentGyro",
              "device": "$device",
              "extra_location_data": $locationJson
            }
        """.trimIndent()
    }

    fun getEnrichedMetadata(lat: String, long: String): String {
        val timestamp = getIsoTimestamp()
        val device = "${Build.MANUFACTURER} ${Build.MODEL}"

        return """
            {
              "lat": "$lat",
              "long": "$long",
              "timestamp": "$timestamp",
              "lightLux": "$currentLightLux",
              "gyro": "$currentGyro",
              "device": "$device"
            }
        """.trimIndent()
    }


    private fun getIsoTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }


    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> currentLightLux = event.values[0]
            Sensor.TYPE_GYROSCOPE -> {
                val x = String.format(Locale.US, "%.2f", event.values[0])
                val y = String.format(Locale.US, "%.2f", event.values[1])
                val z = String.format(Locale.US, "%.2f", event.values[2])
                currentGyro = "$x,$y,$z"
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

}