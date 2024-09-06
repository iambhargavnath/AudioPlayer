package com.iambhargavnath.audioplayer

object TimeUtils {
    fun formatTime(millis: Long?): String {
        if (millis == null) return "00:00"
        val minutes = (millis / 60000).toInt()
        val seconds = (millis % 60000 / 1000).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }
}
