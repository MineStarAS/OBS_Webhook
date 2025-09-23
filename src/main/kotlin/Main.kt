package kr.kro.minestar

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.Path

var webSocketPort = 4455
var ktorPort = 3000
var obsPassword = ""
var obsPath = "C:\\Program Files\\obs-studio\\bin\\64bit\\obs64.exe"

fun main() {
    val configData = Config.loadConfig()
    webSocketPort = (configData["WEB_SOCKET_SERVER_PORT"] as? Int) ?: 4455
    ktorPort = (configData["KTOR_SERVER_PORT"] as? Int) ?: 3000
    obsPassword = (configData["PASSWORD"] as? String) ?: ""
    obsPath = (configData["OBS_PATH"] as? String) ?: "C:\\Program Files\\obs-studio\\bin\\64bit\\obs64.exe"

    if (!AutoExitProcess.isObsRunning()) {
        val path = Path(obsPath)
        val command = arrayOf(
            "cmd", "/c",
            "start \"\" /D \"${path.parent}\" ${path.fileName}"
        )
        Runtime.getRuntime().exec(command)
    }

    AutoExitProcess.run()

    embeddedServer(Netty, port = ktorPort) {
        install(ContentNegotiation) {
            json()
        }
        install(CallLogging)

        routing {
            post("/start-recording") {
                call.respondText("Trying to start recording...")
                launch { sendObsCommand("StartRecord") }
            }

            post("/stop-recording") {
                call.respondText("Trying to stop recording...")
                launch { sendObsCommand("StopRecord") }
            }
        }
    }.start(wait = true)
}

suspend fun sendObsCommand(command: String) {

    val client = HttpClient(Java) {
        install(WebSockets)
    }

    try {
        client.webSocket("ws://localhost:$webSocketPort") {
            // 1. Hello 수신 (challenge + salt 포함)
            val hello = incoming.receive()
            val helloText = if (hello is Frame.Text) hello.readText() else return@webSocket
            println("OBS Hello: $helloText")

            val json = Json.parseToJsonElement(helloText).jsonObject
            val authObj = json["d"]?.jsonObject?.get("authentication")?.jsonObject
            val challenge = authObj?.get("challenge")?.jsonPrimitive?.content ?: return@webSocket
            val salt = authObj.get("salt")?.jsonPrimitive?.content ?: return@webSocket

            // 2. 인증 토큰 생성
            val authToken = generateAuthToken(obsPassword, salt, challenge)

            // 3. Identify 메시지 전송
            val identifyJson = """
                {
                  "op": 1,
                  "d": {
                    "rpcVersion": 1,
                    "authentication": "$authToken"
                  }
                }
            """.trimIndent()
            send(Frame.Text(identifyJson))

            // 4. Identified 응답 수신
            val identified = incoming.receive()
            if (identified is Frame.Text) {
                println("OBS Identified: ${identified.readText()}")
            }

            // 5. OBS 명령 전송
            val commandJson = buildJsonObject {
                put("op", 6)
                putJsonObject("d") {
                    put("requestType", command)
                    put("requestId", "req-${System.currentTimeMillis()}")
                }
            }
            send(Frame.Text(commandJson.toString()))
            PrintLog.info(PrintLog.LogColor.BLUE, "OBS Completion of command transmission: $command")
        }
    } catch (e: Exception) {
        PrintLog.info(PrintLog.LogColor.BLUE, "OBS WebSocket error: ${e.message}")
    } finally {
        client.close()
    }
}


fun generateAuthToken(password: String, salt: String, challenge: String): String {
    val base64 = Base64.getEncoder()

    // Step 1: Create secret = Base64(SHA256(password + salt))
    val secretHash = MessageDigest.getInstance("SHA-256")
        .digest((password + salt).toByteArray(Charsets.UTF_8))
    val secret = base64.encodeToString(secretHash)

    // Step 2: Create auth = Base64(SHA256(secret + challenge))
    val authHash = MessageDigest.getInstance("SHA-256")
        .digest((secret + challenge).toByteArray(Charsets.UTF_8))
    return base64.encodeToString(authHash)
}
