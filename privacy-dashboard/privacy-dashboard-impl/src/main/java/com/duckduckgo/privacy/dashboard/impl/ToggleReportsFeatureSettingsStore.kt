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

package com.duckduckgo.privacy.dashboard.impl

import com.duckduckgo.app.di.AppCoroutineScope
import com.duckduckgo.common.utils.DispatcherProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.feature.toggles.api.FeatureSettings
import com.duckduckgo.feature.toggles.api.RemoteFeatureStoreNamed
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesBinding(AppScope::class)
@RemoteFeatureStoreNamed(ToggleReportsFeature::class)
class ToggleReportsFeatureSettingsStore @Inject constructor(
    private val toggleReportsDataStore: ToggleReportsDataStore,
    @AppCoroutineScope private val coroutineScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
) : FeatureSettings.Store {

    private val jsonAdapter by lazy { buildJsonAdapter() }

    override fun store(jsonString: String) {
        coroutineScope.launch(dispatcherProvider.io()) {
            try {
                jsonAdapter.fromJson(jsonString)?.let {
                    toggleReportsDataStore.storeDismissLogicEnabled(it.dismissLogicEnabled)
                    toggleReportsDataStore.storeDismissInterval(it.dismissInterval)
                    toggleReportsDataStore.storePromptLimitLogicEnabled(it.promptLimitLogicEnabled)
                    toggleReportsDataStore.storePromptInterval(it.promptInterval)
                    toggleReportsDataStore.storeMaxPromptCount(it.maxPromptCount)
                }
            } catch (e: Exception) {
                Timber.d("Katetest->Failed to store ToggleReports settings", e)
            }
        }
    }

    private fun buildJsonAdapter(): JsonAdapter<ToggleReportsSetting> {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        return moshi.adapter(ToggleReportsSetting::class.java)
    }
}

@JsonClass(generateAdapter = true)
data class ToggleReportsSetting(
    @field:Json(name = "dismissLogicEnabled")
    val dismissLogicEnabled: Boolean,
    @field:Json(name = "dismissInterval")
    val dismissInterval: Int,
    @field:Json(name = "promptLimitLogicEnabled")
    val promptLimitLogicEnabled: Boolean,
    @field:Json(name = "promptInterval")
    val promptInterval: Int,
    @field:Json(name = "maxPromptCount")
    val maxPromptCount: Int,
)