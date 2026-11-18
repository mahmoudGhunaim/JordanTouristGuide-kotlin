package edu.zu.jordantouristguide.data.model

/** The signed-in user. Guests have no [User]. */
data class User(
    val id: String,
    val name: String,
    val email: String,
)
