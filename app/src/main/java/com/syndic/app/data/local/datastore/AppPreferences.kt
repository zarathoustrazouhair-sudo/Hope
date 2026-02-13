package com.syndic.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val IS_SETUP_DONE = booleanPreferencesKey("is_setup_done")

    val isSetupDone: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_SETUP_DONE] ?: false
        }

    suspend fun setSetupDone(done: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SETUP_DONE] = done
        }
    }
}
