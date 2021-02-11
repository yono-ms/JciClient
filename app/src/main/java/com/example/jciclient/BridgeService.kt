package com.example.jciclient

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BridgeService : Service() {

    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass.simpleName) }

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.info("onStartCommand startId=$startId")
        notifyStart(startId)
        scope.launch {
            delay(10000)
            notifyStop()
        }
        return START_NOT_STICKY
    }

    private fun notifyStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            logger.info("SDK >= 26")
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            logger.info("SDK < 26")
            stopForeground(true)
        }
    }

    private fun notifyStart(startId: Int) {
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
                .setTicker(getText(R.string.notification_content_ticker))
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
    }
}