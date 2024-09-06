package com.iambhargavnath.audioplayer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentTime = MutableLiveData<String>("00:00")
    val currentTime: LiveData<String> get() = _currentTime

    private val _duration = MutableLiveData<String>("00:00")
    val duration: LiveData<String> get() = _duration

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _seekBarMax = MutableLiveData<Int>(0)
    val seekBarMax: LiveData<Int> get() = _seekBarMax

    private val _seekBarProgress = MutableLiveData<Int>(0)
    val seekBarProgress: LiveData<Int> get() = _seekBarProgress

    private val notificationHelper = NotificationHelper(application)

    val player: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        setMediaItem(MediaItem.fromUri("https://file-examples.com/storage/fe44eeb9cb66ab8ce934f14/2017/11/file_example_MP3_1MG.mp3"))
        prepare()
    }

    init {
        observePlayer()
    }

    fun playPause() {
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else {
            player.play()
            _isPlaying.value = true

        }
        updateNotification()
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    private fun observePlayer() {
        viewModelScope.launch(Dispatchers.Main) {
            while (true) {
                delay(1000)
                val position = player.currentPosition
                val duration = player.duration
                _currentTime.value = formatTime(position)
                _duration.value = formatTime(duration)
                _seekBarMax.value = duration.toInt()
                _seekBarProgress.value = position.toInt()
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 60000).toInt()
        val seconds = (millis % 60000 / 1000).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun updateNotification() {
        notificationHelper.showNotification(player.isPlaying)
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        notificationHelper.cancelNotification()
    }
}