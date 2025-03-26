package com.example.where_am_i_app

data class User(
    val id: String,
    val name: String,
    val profileImageUrl: String = ""
) {
    fun toMap(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "name" to name,
            "profileImageUrl" to profileImageUrl
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>, userId: String): User {
            return User(
                id = userId,
                name = map["name"] as? String ?: "",
                profileImageUrl = map["profileImageUrl"] as? String ?: ""
            )
        }
    }
}