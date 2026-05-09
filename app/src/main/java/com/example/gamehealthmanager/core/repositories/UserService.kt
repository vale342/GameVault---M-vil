package com.example.gamehealthmanager.core.repositories

import com.example.gamehealthmanager.core.ResponseService
import com.example.gamehealthmanager.onboarding.personal.model.UserProfile

interface UserService {
    suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit>
}