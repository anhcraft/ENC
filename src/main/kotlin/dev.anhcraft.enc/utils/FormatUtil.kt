package dev.anhcraft.enc.utils

object FormatUtil {
    const val SECTION_SIGN = '\u00A7'

    @JvmStatic
    fun reverseColorCode(str: String): String {
        return str.replace(SECTION_SIGN, '&')
    }
}
