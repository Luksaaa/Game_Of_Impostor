package com.example.impostergame

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseManager {
    private const val DATABASE_URL = "https://gameofimpostor-default-rtdb.europe-west1.firebasedatabase.app/"
    
    private val database: FirebaseDatabase = Firebase.database(DATABASE_URL)
    val roomsRef: DatabaseReference = database.getReference("rooms")

    fun generateRoom(username: String, onComplete: (String) -> Unit) {
        createNewRoom(username, onComplete)
    }

    private fun createNewRoom(username: String, onComplete: (String) -> Unit) {
        val code = generateRandomCode()
        
        roomsRef.child(code).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                createNewRoom(username, onComplete) // Ponovi ako postoji
            } else {
                val roomData: Map<String, Any> = mapOf(
                    "admin" to username,
                    "status" to "waiting",
                    "players" to mapOf(username to mapOf("name" to username, "isReady" to false)),
                    "messages" to mapOf(roomsRef.push().key!! to "$username je napravio sobu")
                )
                roomsRef.child(code).setValue(roomData).addOnSuccessListener {
                    onComplete(code)
                }
            }
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
