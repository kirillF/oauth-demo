package com.github.kirillf.oauth.okhttp

import okhttp3.Interceptor
import okhttp3.Response

class LoggingInterceptor(private val logger: (String) -> Unit) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val t1 = System.nanoTime()
        logger("Sending request ${request.url()} ${request.headers()}")

        val response = chain.proceed(request)

        val t2 = System.nanoTime()
        logger("Received response for ${response.request().url()} in ${(t2 - t1) / 1e6} ${response.headers()}")

        return response
    }
}