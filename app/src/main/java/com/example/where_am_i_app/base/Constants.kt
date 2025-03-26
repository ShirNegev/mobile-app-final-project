package com.example.where_am_i_app.base

import com.example.where_am_i_app.User

typealias UsersCallback = (List<User>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object Collections {
        const val USERS = "users"
    }
}