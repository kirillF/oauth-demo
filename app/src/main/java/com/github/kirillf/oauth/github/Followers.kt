package com.github.kirillf.oauth.github

import com.google.gson.GsonBuilder

class Followers {
    private var users: Array<User>? = null

    fun asJSON(): String {
        val gson = GsonBuilder()
                .setPrettyPrinting()
                .create()
        return gson.toJson(users)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for (user in users!!) {
            sb.append("name: ")
            sb.append(user.login)
            sb.append("\n")
            sb.append("type: ")
            sb.append(user.type)
            sb.append("\n")
            sb.append("admin: ")
            sb.append(user.siteAdmin)
            sb.append("\n")
        }
        return sb.toString()
    }

    companion object {

        fun createFromJSON(json: String): Followers {
            val gson = GsonBuilder().create()
            val followers = Followers()
            followers.users = gson.fromJson(json, Array<User>::class.java) as Array<User>

            return followers
        }
    }
}
