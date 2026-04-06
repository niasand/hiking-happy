package com.happyclaw.hikinghappy.di

import com.happyclaw.hikinghappy.data.repository.ActivityRepository
import com.happyclaw.hikinghappy.data.repository.ActivityRepositoryImpl
import com.happyclaw.hikinghappy.data.repository.UserPreferencesRepositoryImpl
import com.happyclaw.hikinghappy.domain.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindActivityRepository(impl: ActivityRepositoryImpl): ActivityRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
