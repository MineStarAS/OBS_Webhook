package kr.kro.minestar

object PrintLog {

    enum class LogColor(val code: String) {
        RESET("\u001B[0m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m")
    }

    fun info(logColor: LogColor, message: String) = println("${logColor.code}[INFO] $message${LogColor.RESET.code}")

}