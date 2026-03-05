package com.example.impostergame

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseManager {
    private val database = Firebase.database("https://your-project-id.firebaseio.com/") // REPLACE WITH YOUR URL if needed, or use default
    val roomsRef = database.getReference("rooms")

    fun generateRoom(username: String, onComplete: (String) -> Unit) {
        val code = generateRandomCode()
        val roomData = mapOf(
            "admin" to username,
            "status" to "waiting",
            "players" to mapOf(username to true),
            "messages" to listOf("$username je napravio sobu")
        )
        roomsRef.child(code).setValue(roomData).addOnSuccessListener {
            onComplete(code)
        }
    }

    private fun generateRandomCode(): String {
        val letters = ('A'..'Z').toList()
        val numbers = ('0'..'9').toList()
        val randomLetters = (1..3).map { letters.random() }.joinToString("")
        val randomNumbers = (1..3).map { numbers.random() }.joinToString("")
        return randomLetters + randomNumbers
    }
}
