package com.premium.vault

import java.io.BufferedReader
import java.io.InputStreamReader

object Commands {
    fun execute(cmd: String): String {
        val sb = StringBuilder()
        try {
            val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val r = BufferedReader(InputStreamReader(p.inputStream))
            var line: String?
            while (r.readLine().also { line = it } != null) { sb.append(line).append("\n") }
            p.waitFor()
        } catch (e: Exception) { return "Error: ${e.message}" }
        return sb.toString().trim().ifEmpty { "[No Output]" }
    }
}
