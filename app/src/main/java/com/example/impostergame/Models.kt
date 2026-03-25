package com.example.impostergame

import android.content.Context

enum class Screen {
    ENTER_NAME,
    HOME,
    JOIN,
    LOBBY,
    GAME
}

data class Room(
    val admin: String = "",
    val status: String = "waiting", // waiting, started, finished
    val players: Map<String, PlayerInfo> = emptyMap(),
    val mainWord: String = "",
    val imposterWord: String = "",
    val imposterId: String = "",
    val messages: List<String> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val discussionEndTime: Long = 0L,
    val isDiscussionActive: Boolean = false,
    val votedPlayerId: String = "",
    val resultMessage: String = ""
)

data class PlayerInfo(
    val name: String = "",
    val isReady: Boolean = false
)

data class ChatMessage(
    val sender: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object WordManager {
    private var wordPairs: List<Pair<String, String>> = emptyList()

    fun loadWords(context: Context) {
        if (wordPairs.isNotEmpty()) return
        try {
            // Otvaramo sirovu datoteku (res/raw/hrvatski_rijecnik.txt)
            val inputStream = context.resources.openRawResource(R.raw.hrvatski_rijecnik)
            wordPairs = inputStream.bufferedReader().useLines { lines ->
                lines.filter { line ->
                    line.contains("/") && !line.trim().startsWith("(")
                }.mapNotNull { line ->
                    val parts = line.split("/")
                    if (parts.size >= 2) {
                        // Čišćenje razmaka na početku i kraju svake riječi
                        val first = parts[0].trim()
                        val second = parts[1].trim()
                        if (first.isNotBlank() && second.isNotBlank()) {
                            first to second
                        } else null
                    } else null
                }.toList()
            }
        } catch (e: Exception) {
            // Fallback ako dođe do greške
            wordPairs = listOf(
                "Jabuka" to "Kruška",
                "Automobil" to "Motor",
                "Zagreb" to "Split",
                "Sunce" to "Mjesec"
            )
        }
    }

    fun getNextWords(): Pair<String, String> {
        if (wordPairs.isEmpty()) return "Jabuka" to "Kruška"
        val pair = wordPairs.random()
        return if ((0..1).random() == 0) {
            pair.first to pair.second
        } else {
            pair.second to pair.first
        }
    }

    fun getRandomWord(): String = getNextWords().first
    fun getRandomImposterWord(exclude: String): String = "Kruška"
}
