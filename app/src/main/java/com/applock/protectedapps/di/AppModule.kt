package com.applock.protectedapps.di

import android.content.Context
import androidx.room.Room
import com.applock.protectedapps.data.dao.LockedAppDao
import com.applock.protectedapps.data.database.AppLockDatabase
import com.applock.protectedapps.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppLockDatabase(@ApplicationContext context: Context): AppLockDatabase {
        return Room.databaseBuilder(
            context,
            AppLockDatabase::class.java,
            "applock_database.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideLockedAppDao(database: AppLockDatabase): LockedAppDao {
        return database.lockedAppDao()
    }
}
