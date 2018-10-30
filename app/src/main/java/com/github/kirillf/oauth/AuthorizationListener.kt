package com.github.kirillf.oauth

/**
 * Created by kirillf on 10/25/16.
 */
interface AuthorizationListener {
    fun onAuthStarted()

    fun onComplete(token: String)

    fun onError(error: String)
}
