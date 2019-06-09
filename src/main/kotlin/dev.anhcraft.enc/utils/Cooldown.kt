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

    fun elapsedTicks(): Double {
        return UnitUtil.ms2tick((System.currentTimeMillis() - lastTime).toDouble())
    }

    fun isTimeout(ticks: Double): Boolean {
        return elapsedTicks() > ticks
    }

    fun timeLeft(ticks: Double): Double {
        return ticks - elapsedTicks()
    }
}
