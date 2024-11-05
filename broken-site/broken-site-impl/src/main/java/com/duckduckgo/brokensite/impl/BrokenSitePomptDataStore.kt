/*
 * Copyright (c) 2024 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.brokensite.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.brokensite.impl.SharedPreferencesDuckPlayerDataStore.Keys.COOL_DOWN_DAYS
import com.duckduckgo.brokensite.impl.SharedPreferencesDuckPlayerDataStore.Keys.DISMISS_STREAK
import com.duckduckgo.brokensite.impl.SharedPreferencesDuckPlayerDataStore.Keys.DISMISS_STREAK_RESET_DAYS
import com.duckduckgo.brokensite.impl.SharedPreferencesDuckPlayerDataStore.Keys.MAX_DISMISS_STREAK
import com.duckduckgo.brokensite.impl.SharedPreferencesDuckPlayerDataStore.Keys.NEXT_SHOWN_DATE
import com.duckduckgo.brokensite.impl.di.BrokenSitePrompt
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface BrokenSitePomptDataStore {
    suspend fun setMaxDismissStreak(maxDismissStreak: Int)
    suspend fun getMaxDismissStreak(): Int

    suspend fun setDismissStreakResetDays(days: Int)
    suspend fun getDismissStreakResetDays(): Int

    suspend fun setCoolDownDays(days: Int)
    suspend fun getCoolDownDays(): Int
    suspend fun setDismissStreak(streak: Int)
    suspend fun getDismissStreak(): Int
    suspend fun setNextShownDate(nextShownDate: LocalDate?)
    suspend fun getNextShownDate(): LocalDate?
}

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class SharedPreferencesDuckPlayerDataStore @Inject constructor(
    @BrokenSitePrompt private val store: DataStore<Preferences>,
    @AppCoroutineScope private val appCoroutineScope: CoroutineScope,
) : BrokenSitePomptDataStore {

    private object Keys {
        val MAX_DISMISS_STREAK = intPreferencesKey(name = "MAX_DISMISS_STREAK")
        val DISMISS_STREAK_RESET_DAYS = intPreferencesKey(name = "DISMISS_STREAK_RESET_DAYS")
        val COOL_DOWN_DAYS = intPreferencesKey(name = "COOL_DOWN_DAYS")
        val DISMISS_STREAK = intPreferencesKey(name = "DISMISS_STREAK")
        val NEXT_SHOWN_DATE = stringPreferencesKey(name = "NEXT_SHOWN_DATE")
    }

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val maxDismissStreak: Flow<Int> = store.data
        .map { prefs ->
            prefs[MAX_DISMISS_STREAK] ?: 3
        }
        .distinctUntilChanged()

    private val dismissStreakResetDays: Flow<Int> = store.data
        .map { prefs ->
            prefs[DISMISS_STREAK_RESET_DAYS] ?: 30
        }
        .distinctUntilChanged()

    private val coolDownDays: Flow<Int> = store.data
        .map { prefs ->
            prefs[COOL_DOWN_DAYS] ?: 7
        }
        .distinctUntilChanged()

    private val dismissStreak: Flow<Int> = store.data
        .map { prefs ->
            prefs[DISMISS_STREAK] ?: 7
        }
        .distinctUntilChanged()

    private val nextShownDate: Flow<String?> = store.data
        .map { prefs ->
            prefs[NEXT_SHOWN_DATE]
        }
        .distinctUntilChanged()

    override suspend fun setMaxDismissStreak(maxDismissStreak: Int) {
        store.edit { prefs -> prefs[MAX_DISMISS_STREAK] = maxDismissStreak }
    }

    override suspend fun getMaxDismissStreak(): Int = maxDismissStreak.first()

    override suspend fun setDismissStreakResetDays(days: Int) {
        store.edit { prefs -> prefs[DISMISS_STREAK_RESET_DAYS] = days }
    }

    override suspend fun getDismissStreakResetDays(): Int = dismissStreakResetDays.first()

    override suspend fun setCoolDownDays(days: Int) {
        store.edit { prefs -> prefs[COOL_DOWN_DAYS] = days }
    }

    override suspend fun getCoolDownDays(): Int = coolDownDays.first()

    override suspend fun setDismissStreak(streak: Int) {
        store.edit { prefs -> prefs[DISMISS_STREAK] = streak }
    }

    override suspend fun setNextShownDate(nextShownDate: LocalDate?) {
        store.edit { prefs ->

            nextShownDate?.let {
                prefs[NEXT_SHOWN_DATE] = formatter.format(nextShownDate)
            } ?: run {
                prefs.remove(NEXT_SHOWN_DATE)
            }
        }
    }

    override suspend fun getDismissStreak(): Int {
        return dismissStreak.first()
    }

    override suspend fun getNextShownDate(): LocalDate? {
        return nextShownDate.first()?.let { LocalDate.parse(it, formatter) }
    }
}