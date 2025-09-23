package kr.kro.minestar

import kotlinx.coroutines.*
import kotlin.system.exitProcess

object AutoExitProcess {

    @OptIn(DelicateCoroutinesApi::class)
    fun run() {
        GlobalScope.launch {
            while (true) {
                delay(1000)
                if (!isObsRunning()) {
                    PrintLog.info(PrintLog.LogColor.RED, "OBS termination detection, program termination.")
                    exitProcess(0)
                }
            }
        }
    }

    fun isObsRunning(): Boolean {
        val process = ProcessBuilder("tasklist").start()
        val output = process.inputStream.bufferedReader().readText()
        return output.contains("obs64.exe", ignoreCase = true)
    }
}