package com.hzm.musicplayer

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MusicService : Service() {
    private val binder = LocalBinder()
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        createMediaPlayer()
        createNotificationChannel()
    }

    private fun createMediaPlayer() {
        mediaPlayer = MediaPlayer()
        try {
            val audioUri = Uri.parse("android.resource://$packageName/${R.raw.music}")
            mediaPlayer.setDataSource(applicationContext, audioUri)
            mediaPlayer.prepare()
            mediaPlayer.isLooping = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Set an OnCompletionListener to show a notification when the song is finished
        mediaPlayer.setOnCompletionListener {
            showSongFinishedNotification()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        when (action) {
            ACTION_PLAY -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startPlayback()
            }
            ACTION_PAUSE -> {
                pausePlayback()
            }
            ACTION_STOP -> {
                stopPlayback()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun startPlayback() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    fun pausePlayback() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    fun stopPlayback() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.reset() // Reset the MediaPlayer
        stopForeground(true)
        stopSelf()
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun getDuration(): Int {
        return mediaPlayer.duration
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Playing Music")
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun showSongFinishedNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 0
        )

        val songFinishedNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText("Song Finished")
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID_SONG_FINISHED, songFinishedNotification)
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    companion object {
        const val ACTION_PLAY = "com.example.musicplayer.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.musicplayer.ACTION_PAUSE"
        const val ACTION_STOP = "com.example.musicplayer.ACTION_STOP"
        const val CHANNEL_ID = "MusicPlayerChannel"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_ID_SONG_FINISHED = 2
    }
}