package com.example.impostergame

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

val croatianWords = listOf(
    "Jabuka", "Kruška", "Automobil", "Zrakoplov", "Hrvatska", "Zadar", "Zagreb", "More",
    "Sunce", "Knjiga", "Računalo", "Mobitel", "Škola", "Lopta", "Rijeka", "Planina",
    "Šuma", "Kava", "Čaj", "Ručak", "Televizor", "Gitara", "Plaža", "Sladoled", "Otok",
    "Bicikl", "Prozor", "Vrata", "Krevet", "Stol", "Stolica", "Tanjur", "Čaša", "Sat"
)
