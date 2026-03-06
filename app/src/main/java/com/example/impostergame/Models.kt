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
    val status: String = "waiting", // waiting, started
    val players: Map<String, PlayerInfo> = emptyMap(),
    val mainWord: String = "",
    val imposterWord: String = "",
    val imposterId: String = "",
    val messages: List<String> = emptyList()
)

data class PlayerInfo(
    val name: String = "",
    val isReady: Boolean = false
)

object WordManager {
    private var words: List<String> = emptyList()

    fun loadWords(context: Context) {
        if (words.isNotEmpty()) return
        try {
            // Otvaramo sirovu datoteku (res/raw/hrvatski_rijecnik.txt)
            val inputStream = context.resources.openRawResource(R.raw.hrvatski_rijecnik)
            words = inputStream.bufferedReader().useLines { lines ->
                lines.filter { line ->
                    line.isNotBlank() && !line.startsWith("Slovo ")
                }.map { it.trim() }.toList()
            }
        } catch (e: Exception) {
            // Fallback ako dođe do greške
            words = listOf("Jabuka", "Kruška", "Automobil", "Zagreb", "More", "Sunce", "Knjiga")
        }
    }

    fun getRandomWord(): String {
        return if (words.isNotEmpty()) words.random() else "Jabuka"
    }

    fun getRandomImposterWord(exclude: String): String {
        val filtered = words.filter { it != exclude }
        return if (filtered.isNotEmpty()) filtered.random() else "Kruška"
    }
}
