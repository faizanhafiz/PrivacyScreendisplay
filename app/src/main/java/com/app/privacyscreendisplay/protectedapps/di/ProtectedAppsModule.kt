package com.app.privacyscreendisplay.protectedapps.di

import android.content.Context
import com.app.privacyscreendisplay.protectedapps.data.datasource.ProtectedAppsLocalDataSource
import com.app.privacyscreendisplay.protectedapps.data.repository.ProtectedAppsRepositoryImpl
import com.app.privacyscreendisplay.protectedapps.domain.repository.ProtectedAppsRepository
import com.app.privacyscreendisplay.protectedapps.domain.usecase.AddProtectedAppUseCase
import com.app.privacyscreendisplay.protectedapps.domain.usecase.GetProtectedAppsUseCase
import com.app.privacyscreendisplay.protectedapps.domain.usecase.RemoveProtectedAppUseCase

object ProtectedAppsModule {

    fun provideProtectedAppsLocalDataSource(context: Context): ProtectedAppsLocalDataSource {
        return ProtectedAppsLocalDataSource(context)
    }

    fun provideProtectedAppsRepository(
        localDataSource: ProtectedAppsLocalDataSource
    ): ProtectedAppsRepository {
        return ProtectedAppsRepositoryImpl(localDataSource)
    }

    fun provideGetProtectedAppsUseCase(
        repository: ProtectedAppsRepository
    ): GetProtectedAppsUseCase {
        return GetProtectedAppsUseCase(repository)
    }

    fun provideAddProtectedAppUseCase(
        repository: ProtectedAppsRepository
    ): AddProtectedAppUseCase {
        return AddProtectedAppUseCase(repository)
    }

    fun provideRemoveProtectedAppUseCase(
        repository: ProtectedAppsRepository
    ): RemoveProtectedAppUseCase {
        return RemoveProtectedAppUseCase(repository)
    }
}
