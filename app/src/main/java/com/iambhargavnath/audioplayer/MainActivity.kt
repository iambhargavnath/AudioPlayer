package com.iambhargavnath.audioplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val playerViewModel: PlayerViewModel by viewModels()

    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val playPauseButton: Button = findViewById(R.id.play_pause_button)
        val currentTimeTextView: TextView = findViewById(R.id.current_time)
        val durationTextView: TextView = findViewById(R.id.duration)
        val seekBar: SeekBar = findViewById(R.id.seek_bar)

        playerViewModel.currentTime.observe(this) { time ->
            currentTimeTextView.text = time
        }

        playerViewModel.duration.observe(this) { time ->
            durationTextView.text = time
        }

        playerViewModel.isPlaying.observe(this) { isPlaying ->
            playPauseButton.text = if (isPlaying) "Pause" else "Play"
            // Update the notification based on the playback state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    playerViewModel.updateNotification()
                } else {
                    requestNotificationPermission()
                }
            } else {
                playerViewModel.updateNotification()
            }
        }

        playerViewModel.seekBarMax.observe(this) { max ->
            seekBar.max = max
        }

        playerViewModel.seekBarProgress.observe(this) { progress ->
            seekBar.progress = progress
        }

        playPauseButton.setOnClickListener {
            playerViewModel.playPause()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            private var isUserInteraction = false

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    playerViewModel.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserInteraction = true
                if (playerViewModel.isPlaying.value == true) {
                    playerViewModel.playPause() // Pause when user starts interacting
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserInteraction = false
                // Resume playback if it was playing before
                if (playerViewModel.isPlaying.value == false) {
                    playerViewModel.playPause()
                }
            }
        })

        // Initialize permission launcher
        requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                playerViewModel.updateNotification()
            } else {
                // Handle the case where the permission is not granted
            }
        }
    }

    private fun requestNotificationPermission() {
        requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

}