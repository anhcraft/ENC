package dev.anhcraft.enc.utils

import javax.script.ScriptEngineManager
import javax.script.ScriptException

object ScriptUtil {
    private val jsEngine = ScriptEngineManager().getEngineByName("JavaScript")

    @JvmStatic
    fun eval(jsCode: String): Double {
        try {
            val x = jsEngine.eval(jsCode)
            return x as Double
        } catch (e: ScriptException) {
            e.printStackTrace()
        }
        return 0.0
    }
}
