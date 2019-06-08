@file:Suppress("UNREACHABLE_CODE")

package dev.anhcraft.enc.utils

object UnitUtil {
    @JvmStatic
    fun tick2ms(ticks: Double): Double {
        return ticks * 50
    }

    @JvmStatic
    fun ms2tick(ms: Double): Double {
        return ms / 50
    }

    @JvmStatic
    fun tick2s(ticks: Double): Double {
        return ticks / 20
    }

    @JvmStatic
    fun s2tick(s: Double): Double {
        return s * 20
    }
}
