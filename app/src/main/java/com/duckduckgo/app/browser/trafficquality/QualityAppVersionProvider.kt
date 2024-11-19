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

package com.duckduckgo.app.browser.trafficquality

import com.duckduckgo.appbuildconfig.api.AppBuildConfig
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

interface QualityAppVersionProvider {
    fun provide(): String
}

@ContributesBinding(AppScope::class)
class RealQualityAppVersionProvider @Inject constructor(private val appBuildConfig: AppBuildConfig) : QualityAppVersionProvider {
    override fun provide(): String {
        val appBuildDate = appBuildConfig.buildDateTime
        val now = LocalDateTime.now()
        val daysSinceBuild = ChronoUnit.DAYS.between(appBuildDate, now)

        if (daysSinceBuild < DAYS_UNTIL_DEFAULT_VALUE) {
            return APP_VERSION_QUALITY_DEFAULT_VALUE
        }

        if (daysSinceBuild > DAYS_FOR_APP_VERSION_LOGGING) {
            return APP_VERSION_QUALITY_DEFAULT_VALUE
        }

        return appBuildConfig.versionName
    }
}

const val APP_VERSION_QUALITY_DEFAULT_VALUE = "other_versions"
const val DAYS_UNTIL_DEFAULT_VALUE = 6
const val DAYS_FOR_APP_VERSION_LOGGING = DAYS_UNTIL_DEFAULT_VALUE + 10