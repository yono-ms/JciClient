package com.example.jciclient

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.Socket

class BridgeService : Service() {

    enum class Key {
        REMOTE_ID,
        PATH,
    }

    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onBind(intent: Intent): IBinder {
        logger.info("onBind")
        return intent.extras?.let { bundle ->
            val port = Socket().use {
                it.bind(null)
                it.localPort
            }
            BridgeBinder(
                bundle.getInt(Key.REMOTE_ID.name),
                bundle.getString(Key.PATH.name) ?: throw Throwable("no path."),
                port
            )
        } ?: throw Throwable("no extras.")
    }

    override fun onCreate() {
        super.onCreate()
        logger.info("onCreate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            logger.info("SDK >= 26")
            val channelId = getString(R.string.notification_channel_id)

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).also { manager ->
                if (manager.getNotificationChannel(channelId) == null) {
                    NotificationChannel(
                        channelId,
                        getString(R.string.notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = getString(R.string.notification_channel_description)
                    }.also {
                        manager.createNotificationChannel(it)
                    }
                }
            }

            val pendingIntent = Intent(this, BridgeService::class.java).let {
                PendingIntent.getActivity(this, 0, it, 0)
            }
            val notification = Notification.Builder(this, channelId)
                .setContentTitle(getText(R.string.notification_content_title))
                .setContentText(getText(R.string.notification_content_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
            startForeground(1, notification)
        } else {
            logger.info("SDK < 26")
            @Suppress("DEPRECATION") val notification = Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_content_title))
                .setContentText(getText(R.string.notification_content_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
            startForeground(1, notification)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logger.info("onUnbind")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            logger.info("SDK >= 26")
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            logger.info("SDK < 26")
            stopForeground(true)
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.info("onDestroy")
    }

    inner class BridgeBinder(
        private val remoteId: Int,
        private val path: String,
        private val port: Int,
    ) : Binder() {
        val uriString: String
            get() {
                val name = path.split('/').last()
                return "http://localhost:$port/${name}"
            }

        lateinit var onStartWebServer: () -> Unit

        private lateinit var bridgeWebServer: BridgeWebServer

        fun startWebServer() {
            scope.launch {
                kotlin.runCatching {
                    bridgeWebServer = BridgeWebServer(port, remoteId, path)
                    bridgeWebServer.start()
                    onStartWebServer()
                }.onFailure {
                    logger.info("startWebServer", it)
                }
            }
        }

        fun stopWebServer() {
            scope.launch {
                kotlin.runCatching {
                    if (bridgeWebServer.isAlive) {
                        logger.info("bridgeWebServer is alive.")
                        bridgeWebServer.closeAllConnections()
                        bridgeWebServer.stop()
                    }
                }
            }
        }
    }

}