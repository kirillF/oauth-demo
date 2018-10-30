package com.github.kirillf.oauth

import java.io.InputStream

fun InputStream.asString() : String {
    return bufferedReader(Charsets.UTF_8).use { it.readText() }
}