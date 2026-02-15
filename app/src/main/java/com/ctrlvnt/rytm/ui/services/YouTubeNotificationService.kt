package com.ctrlvnt.rytm.ui.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.ctrlvnt.rytm.R
import com.bumptech.glide.request.target.CustomTarget

class YouTubeNotificationService : Service() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManagerCompat

    companion object {
        const val CHANNEL_ID = "music_channel_id"
        const val NOTIFICATION_ID = 1


        const val ACTION_PLAY = "action_play"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREV = "action_prev"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        notificationManager = NotificationManagerCompat.from(this)

        mediaSession = MediaSessionCompat(this, "YouTubeService")
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() { sendBroadcastToFragment(ACTION_PLAY) }
            override fun onPause() { sendBroadcastToFragment(ACTION_PAUSE) }
            override fun onSkipToNext() { sendBroadcastToFragment(ACTION_NEXT) }
            override fun onSkipToPrevious() { sendBroadcastToFragment(ACTION_PREV) }
        })
        mediaSession.isActive = true
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val action = intent?.action
        if (action != null) {
            when (action) {
                ACTION_NEXT -> sendBroadcastToFragment(ACTION_NEXT)
                ACTION_PREV -> sendBroadcastToFragment(ACTION_PREV)
                ACTION_PLAY -> sendBroadcastToFragment(ACTION_PLAY)
                ACTION_PAUSE -> sendBroadcastToFragment(ACTION_PAUSE)
            }
        }

         if (intent?.hasExtra("TITLE") == true) {
            val title = intent.getStringExtra("TITLE") ?: "Unknown"
            val author = intent.getStringExtra("AUTHOR") ?: "Unknown"
            val url = intent.getStringExtra("THUMBNAIL_URL") ?: "Unknown"
            val isPlaying = intent.getBooleanExtra("IS_PLAYING", false)
             if (url != null && url.isNotEmpty()) {
                 Glide.with(this)
                     .asBitmap()
                     .load(url)
                     .into(object : CustomTarget<Bitmap>() {

                         override fun onResourceReady(
                             resource: Bitmap,
                             transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                         ) {
                             showNotification(title, author, isPlaying, resource)
                         }

                         override fun onLoadCleared(placeholder: Drawable?) {
                             // Non serve fare nulla qui
                         }
                     })
             } else {
                 // Se non c'è URL, mostra notifica senza large icon (o con placeholder)
                 showNotification(title, author, isPlaying, null)
             }
        }

        return START_NOT_STICKY
    }

    private fun showNotification(title: String, author: String, isPlaying: Boolean, bitmap: Bitmap?) {
        val playPauseIcon = if (isPlaying) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24

        // Nota: qui passiamo le stringhe ACTION_*, non i KeyEvent
        val playPauseAction = if (isPlaying) ACTION_PAUSE else ACTION_PLAY

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(author)
            .setSmallIcon(R.drawable.notify_logo)
            .setLargeIcon(bitmap)

            // --- MODIFICA QUI ---
            .addAction(R.drawable.baseline_skip_previous, "Previous", getActionIntent(ACTION_PREV))
            .addAction(playPauseIcon, "Play/Pause", getActionIntent(playPauseAction))
            .addAction(R.drawable.baseline_skip_next, "Next", getActionIntent(ACTION_NEXT))
            // --------------------

            .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.sessionToken)
                .setShowActionsInCompactView(0, 1, 2))
            .setOnlyAlertOnce(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }


    private fun getActionIntent(action: String): PendingIntent {
        val intent = Intent(this, YouTubeNotificationService::class.java)
        intent.action = action

        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun sendBroadcastToFragment(action: String) {
        val intent = Intent("PLAYER_ACTION")
        intent.putExtra("ACTION_TYPE", action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mediaSession.release()
        super.onDestroy()
    }
}