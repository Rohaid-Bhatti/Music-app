package com.hzm.musicplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar

class MainActivity : AppCompatActivity() {
    /*lateinit var runnable: Runnable
    private var handler = Handler()
    private lateinit var mediaPlayer: MediaPlayer*/
    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var stopButton: ImageButton
    private lateinit var musicService: MusicService
    private lateinit var seekBar: SeekBar
    private var isBound: Boolean = false
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*val play_btn = findViewById<ImageButton>(R.id.play_btn)
        val seekbar = findViewById<SeekBar>(R.id.seekbar)

        mediaPlayer = MediaPlayer.create(this, R.raw.music)
        seekbar.progress = 0
        seekbar.max = mediaPlayer.duration

        play_btn.setOnClickListener{
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()

                play_btn.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                mediaPlayer.pause()
                play_btn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
        }

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, pos: Int, changed: Boolean) {
                if (changed) {
                    mediaPlayer.seekTo(pos)

                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })

        runnable = Runnable {
            seekbar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
        mediaPlayer.setOnCompletionListener {
            play_btn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            seekbar.progress = 0
        }*/

        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        stopButton = findViewById(R.id.stopButton)
        seekBar = findViewById(R.id.seekbar)

        // Initialize the MusicService
        val serviceIntent = Intent(this, MusicService::class.java)
        bindService(serviceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE)

        playButton.setOnClickListener {
            if (isBound) {
                musicService.startPlayback()
            }
        }

        pauseButton.setOnClickListener {
            if (isBound) {
                musicService.pausePlayback()
            }
        }

        stopButton.setOnClickListener {
            if (isBound) {
                musicService.stopPlayback()
            }
        }

        // SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Seek to the selected position when the user interacts with the SeekBar
                    musicService.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Update SeekBar periodically
        updateSeekBar()
    }

    private val musicServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.LocalBinder
            musicService = binder.getService()
            isBound = true
            setupSeekBar()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Handle disconnection if needed
            isBound = false
        }
    }

    private fun updateSeekBar() {
        // Create a Runnable to update the SeekBar progress
        val updateSeekBar = object : Runnable {
            override fun run() {
                if (isBound) {
                    val currentPosition = musicService.getCurrentPosition()
                    val duration = musicService.getDuration()
                    seekBar.max = duration
                    seekBar.progress = currentPosition
                }
                handler.postDelayed(this, 1000) // Update every 1 second
            }
        }

        handler.post(updateSeekBar)
    }

    private fun setupSeekBar() {
        val duration = musicService.getDuration()
        seekBar.max = duration

        // Create a Runnable to update the SeekBar progress
        val updateSeekBar = object : Runnable {
            override fun run() {
                val currentPosition = musicService.getCurrentPosition()
                seekBar.progress = currentPosition
                handler.postDelayed(this, 1000) // Update every 1 second
            }
        }

        handler.post(updateSeekBar)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(musicServiceConnection)
            isBound = false
        }
    }
}