package com.example.jciclient

import android.webkit.MimeTypeMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

enum class ViewerType(
    private val mimeTypes: Set<String>
) {
    EXTERNAL(
        setOf("video/mkv")
    ),
    IMAGE(
        setOf("image/")
    ),
    VIDEO(
        setOf("video/")
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
                values().forEach { viewerType ->
                    viewerType.mimeTypes.forEach {
                        if (mimeType.startsWith(it)) {
                            return viewerType
                        }
                    }
                }
                return UNKNOWN
            } ?: UNKNOWN
        }
    }
}