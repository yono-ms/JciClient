package com.example.jciclient

import android.webkit.MimeTypeMap
import java.io.File

enum class ViewerType(
    private val mimeTypes: Set<String>
) {
    IMAGE(
        setOf("image/jpeg")
    ),
    VIDEO(
        setOf("video/mp4")
    ),
    UNKNOWN(
        setOf()
    );

    companion object {
        fun fromPath(path: String): ViewerType {
            val ext = File(path).extension
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let { mimeType ->
                when {
                    IMAGE.mimeTypes.contains(mimeType) -> IMAGE
                    VIDEO.mimeTypes.contains(mimeType) -> VIDEO
                    else -> UNKNOWN
                }
            } ?: UNKNOWN
        }
    }
}