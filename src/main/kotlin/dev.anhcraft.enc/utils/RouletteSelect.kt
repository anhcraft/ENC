package dev.anhcraft.enc.utils

import dev.anhcraft.jvmkit.utils.RandomUtil

/**
 * @see [https://en.wikipedia.org/wiki/Fitness_proportionate_selection](https://en.wikipedia.org/wiki/Fitness_proportionate_selection)
 */
object RouletteSelect {
    @JvmStatic
    fun chooseFromList(list: List<Double>): Int {
        val weightsum = list.stream().mapToDouble{it.toDouble()}.sum()
        var value = RandomUtil.randomDouble(0.0, weightsum)
        for (i in list.indices) {
            if (value > 0) value -= list[i]
            else return i
        }
        return list.size - 1
    }
}
