package com.alvaroquintana.usecases.question.internal

internal object RandomUtils {
    fun randomWithExclusion(start: Int, end: Int, vararg exclude: Int): Int {
        var n = (start..end).random()
        var attempts = 0
        while (exclude.contains(n)) {
            n = (start..end).random()
            if (++attempts > 1000) break
        }
        return n
    }
}
