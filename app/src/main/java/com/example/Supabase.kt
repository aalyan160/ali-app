package com.example

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.gotrue.MemorySessionManager

val supabase by lazy {
    createSupabaseClient(
        supabaseUrl = "https://wzhnmhfekvuepbczszie.supabase.co",
        supabaseKey = "sb_publishable_Icl0bEL_xisOx6qGorkymQ_Fxc64sPT"
    ) {
        install(Auth) {
            try {
                // Explicitly use SettingsSessionManager to leverage multiplatform-settings-no-arg
                sessionManager = io.github.jan.supabase.gotrue.SettingsSessionManager()
            } catch (e: Exception) {
                // Fallback for Robolectric tests where StartupProvider doesn't initialize it
                sessionManager = io.github.jan.supabase.gotrue.MemorySessionManager()
            }
            try {
                codeVerifierCache = io.github.jan.supabase.gotrue.SettingsCodeVerifierCache()
            } catch (e: Exception) {
                codeVerifierCache = io.github.jan.supabase.gotrue.MemoryCodeVerifierCache()
            }
        }
        install(Postgrest) {
            serializer = io.github.jan.supabase.serializer.KotlinXSerializer(
                kotlinx.serialization.json.Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }
}
