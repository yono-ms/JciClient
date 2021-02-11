package com.example.jciclient

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BridgeService : Service() {

    enum class Key {
        REMOTE_ID,
        PATH,
        PORT,
    }

    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private lateinit var bridgeWebServer: BridgeWebServer

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.info("onStartCommand startId=$startId")
        scope.launch {
            kotlin.runCatching {
                val (remoteId, path, port) = intent?.extras?.let { bundle ->
                    Triple(
                        bundle.getInt(Key.REMOTE_ID.name),
                        bundle.getString(Key.PATH.name) ?: throw Throwable("no path."),
                        bundle.getInt(Key.PORT.name)
                    )
                } ?: throw Throwable("no extras.")
                notifyStart(startId)
                bridgeWebServer = BridgeWebServer(port, remoteId, path)
                bridgeWebServer.start()
            }.onFailure {
                logger.error("onStartCommand", it)
                notifyStop()
            }
        }
        return START_NOT_STICKY
    }

    private fun notifyStop() {
        logger.info("notifyStop")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            logger.info("SDK >= 26")
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            logger.info("SDK < 26")
            stopForeground(true)
        }
    }

    private fun notifyStart(startId: Int) {
        logger.info("notifyStart")
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
            startForeground(startId, notification)
        } else {
            logger.info("SDK < 26")
            @Suppress("DEPRECATION") val notification = Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_content_title))
                .setContentText(getText(R.string.notification_content_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
            startForeground(startId, notification)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        logger.info("onBind")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        logger.info("onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.info("onDestroy")
        kotlin.runCatching {
            if (bridgeWebServer.isAlive) {
                logger.info("bridgeWebServer is alive.")
                bridgeWebServer.closeAllConnections()
                bridgeWebServer.stop()
                notifyStop()
            }
        }.onFailure {
            logger.error("onDestroy", it)
        }
    }
}