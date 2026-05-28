package com.pinghtdog.amping.data.repository

import android.content.Context
import java.io.File

object TokenManager {
    private const val REFRESH_TOKEN_FILE = "refresh_token.txt"
    private const val ACCESS_TOKEN_FILE = "access_token.txt"

    private var cachedAccessToken: String? = null

    @Synchronized
    fun getRefreshToken(context: Context): String? {
        val file = File(context.filesDir, REFRESH_TOKEN_FILE)
        if (file.exists()) {
            val token = file.readText().trim()
            if (token.isNotEmpty()) return token
        }
        
        // Seeding the user's provided refresh token as the default
        val defaultToken = "b8OzKIfLYlhDUSZkFFKfh48YbxESPKnmxXBQJiIvmGFRBTPYd8eRd24PHcnFXOd1w8kdHftQFVSSCamWx1X4As4kmEWK7VZF1jg4s16hXUOYRkxkGGtqhZLx2iPExcuq8VnllRuAMFsDCwMuaj5eRnUGkwCaRngHKNBqe9F0JwNsb1sCZmvyPIHSxU3Svow6FRVh3gdztNyQ0hUVaZyDzEHz9aCXC2e3YIB3hVFpLgNHTKeFVwjAfhtOaooh6fy"
        saveRefreshToken(context, defaultToken)
        return defaultToken
    }

    @Synchronized
    fun saveRefreshToken(context: Context, token: String) {
        val file = File(context.filesDir, REFRESH_TOKEN_FILE)
        file.writeText(token.trim())
    }

    @Synchronized
    fun getAccessToken(context: Context): String? {
        if (cachedAccessToken != null) return cachedAccessToken
        val file = File(context.filesDir, ACCESS_TOKEN_FILE)
        if (file.exists()) {
            cachedAccessToken = file.readText().trim().ifEmpty { null }
        }
        return cachedAccessToken
    }

    @Synchronized
    fun saveAccessToken(context: Context, token: String) {
        cachedAccessToken = token.trim()
        val file = File(context.filesDir, ACCESS_TOKEN_FILE)
        file.writeText(token.trim())
    }

    @Synchronized
    fun clearTokens(context: Context) {
        cachedAccessToken = null
        File(context.filesDir, REFRESH_TOKEN_FILE).delete()
        File(context.filesDir, ACCESS_TOKEN_FILE).delete()
    }
}
