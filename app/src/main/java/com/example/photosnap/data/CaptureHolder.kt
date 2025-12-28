package com.example.photosnap.data



object CaptureHolder {
    var imageBytes: ByteArray? = null
    var metadata: String = ""
    var signature: String = ""

    fun clear() {
        imageBytes = null
        metadata = ""
        signature = ""
    }
}