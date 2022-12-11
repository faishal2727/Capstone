package com.example.capstone.ui.onboarding

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.capstone.databinding.ActivityOnboardingBinding
import com.example.capstone.model.login_model.LoginResultModel
import com.example.capstone.preference.PreferenceLogin
import com.example.capstone.ui.splash.SplashActivity
import com.example.capstone.util.Constanta


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@SuppressLint("CustomOnboarding")
class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var preferenceLogin: PreferenceLogin
    private lateinit var loginResultModel: LoginResultModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        supportActionBar?.hide()
        preferenceLogin = PreferenceLogin(this)
        loginResultModel = preferenceLogin.getUser()
        isLogin()

        binding.button.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
        }
    }

    private fun navigate(intent: Intent) {
        val splashTimer: Long = Constanta.ONBOARDING_SCREEN_TIMER
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, splashTimer)
    }

    private fun isLogin() {
        if (loginResultModel.name != null && loginResultModel.token != null && loginResultModel.id != null) {
            val intent = Intent(this, SplashActivity::class.java)
            navigate(intent)
        }
    }
}