package com.app.privacyscreendisplay.onboarding.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Security
import com.app.privacyscreendisplay.onboarding.data.datasource.OnboardingLocalDataSource
import com.app.privacyscreendisplay.onboarding.domain.model.OnboardingFeatureItem
import com.app.privacyscreendisplay.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Concrete implementation of [OnboardingRepository] bridging local data persistence with domain expectations.
 *
 * @property localDataSource Data source wrapping DataStore operations.
 */
class OnboardingRepositoryImpl(
    private val localDataSource: OnboardingLocalDataSource
) : OnboardingRepository {

    override fun isOnboardingCompleted(): Flow<Boolean> {
        return localDataSource.isOnboardingCompletedFlow
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        localDataSource.setOnboardingCompleted(completed)
    }

    override fun getOnboardingFeatures(): List<OnboardingFeatureItem> {
        return listOf(
            OnboardingFeatureItem(
                id = "feature_ai_detection",
                title = "AI detects extra faces",
                description = "Uses on-device AI. Your data stays private.",
                icon = Icons.Rounded.Face
            ),
            OnboardingFeatureItem(
                id = "feature_instant_protection",
                title = "Instant screen protection",
                description = "Blurs your screen so others can't see.",
                icon = Icons.Rounded.Security
            ),
            OnboardingFeatureItem(
                id = "feature_cross_app",
                title = "Works across apps",
                description = "Protects banking, chats, notes and more.",
                icon = Icons.Rounded.Apps
            )
        )
    }
}
