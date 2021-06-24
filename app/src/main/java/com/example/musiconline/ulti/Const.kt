package com.example.musiconline.ulti

import java.util.concurrent.TimeUnit

object Const {
    const val BASE_URL = "https://mp3.zing.vn/xhr/"
    fun durationConverter(duration: Long): String {
        return String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        )
    }
}