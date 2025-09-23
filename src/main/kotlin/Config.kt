package kr.kro.minestar

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File

object Config {
    val configFile = File("config.yml")

    private val options = DumperOptions().apply {
        defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        isPrettyFlow = true
        indent = 2
    }

    fun loadConfig(): Map<String, Any> {
        if (!configFile.exists()) generateDefaultConfig()
        return Yaml(options).load(configFile.inputStream()) as Map<String, Any>
    }

    private fun generateDefaultConfig() {
        val map = mapOf(
            "PASSWORD" to "",
            "WEB_SOCKET_SERVER_PORT" to 4455,
            "KTOR_SERVER_PORT" to 3000,
            "OBS_PATH" to "C:\\Program Files\\obs-studio\\bin\\64bit\\obs64.exe",
        )
        Yaml(options).dump(map, configFile.writer())
    }
}
