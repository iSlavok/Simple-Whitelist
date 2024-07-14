package online.slavok.events

import com.mojang.authlib.GameProfile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import online.slavok.SimpleWhitelist
import online.slavok.mixin.ServerLoginNetworkHandlerAccessor
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.ParseException
import java.util.*

class LoginEvent {
    private val client: HttpClient = HttpClient.newBuilder().build()

    fun register() {
        ServerLoginConnectionEvents.QUERY_START.register(
            ServerLoginConnectionEvents.QueryStart { handler, _, _, _ ->
                val accessor = handler as ServerLoginNetworkHandlerAccessor
                val profile: GameProfile = accessor.getProfile()
                val playerUUID = profile.id
                val playerName = profile.name
                if (SimpleWhitelist.configManager.config.whitelist && !SimpleWhitelist.databaseManager.checkPlayer(playerName)) {
                    handler.disconnect(Text.literal("You are not on the whitelist!").formatted(Formatting.RED))
                    return@QueryStart
                }
                val offlineUUID = UUID.nameUUIDFromBytes("OfflinePlayer:${profile.name}".toByteArray(StandardCharsets.UTF_8))
                movePlayerData(playerUUID, playerName, offlineUUID)
            }
        )
    }

    private fun movePlayerData(playerUUID: UUID, playerName: String, offlineUUID: UUID) {
        try {
            if (playerUUID == offlineUUID) {
                var onlineUUID: UUID? = null
                try {
                    onlineUUID = getOnlineUUID(playerName)
                } catch (ignore: Exception) {
                }
                if (onlineUUID == null) {
                    return
                }
                replaceData(onlineUUID.toString(), offlineUUID.toString())
            } else {
                replaceData(offlineUUID.toString(), playerUUID.toString())
            }
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }
    }

    @Throws(IOException::class, InterruptedException::class, ParseException::class)
    private fun getOnlineUUID(nickname: String): UUID {
        val response: HttpResponse<String> = client.send(
            HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://api.mojang.com/users/profiles/minecraft/$nickname"))
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        val json = Json { ignoreUnknownKeys = true }
        val jsonObject = json.decodeFromString(JsonObject.serializer(), response.body())
        val rawUuid = jsonObject["id"]?.toString()
        val one = rawUuid?.substring(0, 8)
        val two = rawUuid?.substring(8, 12)
        val three = rawUuid?.substring(12, 16)
        val four = rawUuid?.substring(16, 20)
        val five = rawUuid?.substring(20)
        return UUID.fromString("$one-$two-$three-$four-$five")
    }

    @Throws(IOException::class)
    private fun replaceData(from: String, to: String) {
        val advancementsPath = SimpleWhitelist.advancementsPath
        val advancements = advancementsPath.resolve("$from.json")
        val newAdvancements = advancementsPath.resolve("$to.json")
        if (advancements.toFile().exists()) {
            Files.move(advancements, newAdvancements, StandardCopyOption.REPLACE_EXISTING)
        }
        val playerDataPath = SimpleWhitelist.playerDataPath
        val playerData = playerDataPath.resolve("$from.dat")
        val newPlayerData = playerDataPath.resolve("$to.dat")
        val playerDataOld = playerDataPath.resolve("$from.dat_old")
        val newPlayerDataOld = playerDataPath.resolve("$to.dat_old")
        if (playerData.toFile().exists()) {
            Files.move(playerData, newPlayerData, StandardCopyOption.REPLACE_EXISTING)
        }
        if (playerDataOld.toFile().exists()) {
            Files.move(playerDataOld, newPlayerDataOld, StandardCopyOption.REPLACE_EXISTING)
        }
        val statsPath = SimpleWhitelist.statsPath
        val stats = statsPath.resolve("$from.json")
        val newStats = statsPath.resolve("$to.json")
        if (stats.toFile().exists()) {
            Files.move(stats, newStats, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}