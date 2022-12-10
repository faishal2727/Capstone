package com.example.capstone.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.capstone.preference.PreferenceOnboarding
import kotlinx.coroutines.launch

class OnboardingViewModel ( context: Context): ViewModel() {


    private val onBoardingPreferences = PreferenceOnboarding(context)

    fun setBoardingKey(key : Boolean){
        viewModelScope.launch{
            onBoardingPreferences.setToken(key)
        }
    }

    fun getBoardingKey() = onBoardingPreferences.getToken().asLiveData()
}