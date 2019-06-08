package dev.anhcraft.enc.utils

class Cooldown {
    private var lastTime: Long = 0

    constructor() {
        this.lastTime = System.currentTimeMillis()
    }

    constructor(lastTime: Long) {
        this.lastTime = lastTime
    }

    fun reset(): Cooldown {
        lastTime = System.currentTimeMillis()
        return this
    }

    fun elapsedTime(): Double {
        return (System.currentTimeMillis() - lastTime) / 1000.0
    }

    fun isTimeout(seconds: Double): Boolean {
        return elapsedTime() > seconds
    }

    fun timeLeft(seconds: Double): Double {
        return seconds - elapsedTime()
    }
}
