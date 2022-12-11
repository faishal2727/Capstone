package com.example.capstone.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.capstone.preference.PreferenceOnboarding.Companion.dataStoreName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.onBoarding: DataStore<Preferences> by preferencesDataStore(name = dataStoreName)

class PreferenceOnboarding(private val context: Context) {

    private val boardingKey = booleanPreferencesKey("boardingKey")

    suspend fun setToken(key: Boolean) {
        context.onBoarding.edit {
            it[boardingKey] = key
        }
    }

    fun getToken(): Flow<Boolean> {
        return context.onBoarding.data.map {
            it[boardingKey] ?: false
        }
    }


    companion object {
        const val dataStoreName = "dataStoreName"
    }
}
