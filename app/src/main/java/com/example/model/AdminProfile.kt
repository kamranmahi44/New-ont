package com.example.model

data class AdminProfile(
    val login: String = "octocat",
    val name: String = "GitHub Network Admin",
    val avatarUrl: String = "https://avatars.githubusercontent.com/u/583231?v=4",
    val bio: String = "Local Network Infrastructure SuperAdmin",
    val publicRepos: Int = 12,
    val followers: Int = 42,
    val role: String = "Network SuperAdmin",
    val token: String = "gho_admin_token_sample",
    val isAuthenticated: Boolean = true,
    val authMethod: String = "GitHub OAuth 2.0"
)
