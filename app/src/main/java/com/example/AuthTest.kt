package com.example

import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.SessionStatus

class AuthTest {
    suspend fun testAuth() {
        supabase.auth.signUpWith(Email) {
            email = "test@example.com"
            password = "password"
        }
        supabase.auth.signInWith(Email) {
            email = "test@example.com"
            password = "password"
        }
    }
}
