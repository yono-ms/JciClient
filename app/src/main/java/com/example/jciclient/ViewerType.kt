package com.example.jciclient

import android.webkit.MimeTypeMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

enum class ViewerType(
    private val mimeTypes: Set<String>
) {
    IMAGE(
        setOf("image/jpeg", "image/png")
    ),
    VIDEO(
        setOf("video/mpg")
    ),
    EXTERNAL(
        setOf("video/mp4")
    ),
    UNKNOWN(
        setOf()
    );

    companion object {

        private val logger: Logger by lazy { LoggerFactory.getLogger("ViewerType") }

        fun fromPath(path: String): ViewerType {
            val ext = File(path).extension
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)?.let { mimeType ->
                logger.info("mimeType=$mimeType")
                when {
                    IMAGE.mimeTypes.contains(mimeType) -> IMAGE
                    VIDEO.mimeTypes.contains(mimeType) -> VIDEO
                    EXTERNAL.mimeTypes.contains(mimeType) -> EXTERNAL
                    else -> UNKNOWN
                }
            } ?: UNKNOWN
        }
    }
}