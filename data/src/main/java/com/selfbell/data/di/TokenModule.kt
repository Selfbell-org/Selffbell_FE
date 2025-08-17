// File: com.selfbell.data.di.TokenModule.kt

package com.selfbell.data.di

import com.selfbell.data.repository.impl.TokenManager
import com.selfbell.data.repository.impl.TokenManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TokenModule {

    @Binds
    @Singleton
    abstract fun bindTokenManager(
        tokenManagerImpl: TokenManagerImpl
    ): TokenManager
}