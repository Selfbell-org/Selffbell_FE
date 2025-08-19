package com.selfbell.escort.di

import android.content.Context
import com.selfbell.escort.ui.LocationTrackingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EscortModule {

    @Provides
    @Singleton
    fun provideLocationTrackingService(
        @ApplicationContext context: Context
    ): LocationTrackingService {
        return LocationTrackingService(context)
    }
}

