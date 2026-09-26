package com.example.network

import com.example.model.AdminProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GitHubAuthService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Configurable OAuth Client ID (can be customized by user or prefilled)
    var clientId: String = "Ov23lizSampleClientID"
    var redirectUri: String = "netadmin://oauth-callback"

    val authorizationUrl: String
        get() = "https://github.com/login/oauth/authorize?client_id=$clientId&redirect_uri=$redirectUri&scope=read:user,user:email"

    suspend fun authenticateWithToken(token: String): Result<AdminProfile> = withContext(Dispatchers.IO) {
        try {
            val cleanToken = token.trim()
            val request = Request.Builder()
                .url("https://api.github.com/user")
                .header("Authorization", "Bearer $cleanToken")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "NetAdmin-Android-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("GitHub API returned HTTP ${response.code}"))
                }
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
                val json = JSONObject(body)

                val profile = AdminProfile(
                    login = json.optString("login", "admin"),
                    name = json.optString("name", json.optString("login")),
                    avatarUrl = json.optString("avatar_url", "https://avatars.githubusercontent.com/u/583231?v=4"),
                    bio = json.optString("bio", "Local Subnet Administrator"),
                    publicRepos = json.optInt("public_repos", 0),
                    followers = json.optInt("followers", 0),
                    role = "Verified GitHub SuperAdmin",
                    token = cleanToken,
                    isAuthenticated = true,
                    authMethod = "GitHub OAuth 2.0"
                )
                Result.success(profile)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDemoOctocatAdmin(): AdminProfile {
        return AdminProfile(
            login = "octocat",
            name = "Monalisa Octocat",
            avatarUrl = "https://avatars.githubusercontent.com/u/583231?v=4",
            bio = "Subnet Network Operations Lead & Core Maintainer",
            publicRepos = 8,
            followers = 1337,
            role = "Verified GitHub SuperAdmin",
            token = "gho_octocat_demo_session",
            isAuthenticated = true,
            authMethod = "GitHub OAuth Demo"
        )
    }
}
