package com.example.capstone.ui.splash

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
import com.example.capstone.R
import com.example.capstone.model.login_model.LoginResultModel
import com.example.capstone.preference.PreferenceLogin
import com.example.capstone.ui.auth.login.LoginActivity
import com.example.capstone.ui.main.MainActivity
import com.example.capstone.util.Constanta


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@SuppressLint("CustomSplashScreen")

class SplashActivity : AppCompatActivity() {
    private lateinit var preferenceLogin: PreferenceLogin
    private lateinit var loginResultModel: LoginResultModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        supportActionBar?.hide()
        preferenceLogin = PreferenceLogin(this)

        loginResultModel = preferenceLogin.getUser()
        isLogin()

    }

    private fun navigate(intent: Intent) {
        val splashTimer: Long = Constanta.SPLASH_SCREEN_TIMER
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intent)
            finish()
        }, splashTimer)
    }

    private fun isLogin() {
        if (loginResultModel.name != null && loginResultModel.token != null && loginResultModel.id != null) {
            val intent = Intent(this, MainActivity::class.java)
            navigate(intent)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            navigate(intent)
        }
    }
}