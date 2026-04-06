package com.happyclaw.hikinghappy.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.happyclaw.hikinghappy.data.local.HikingDatabase
import com.happyclaw.hikinghappy.data.local.dao.ActivityRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HikingDatabase {
        return Room.databaseBuilder(
            context,
            HikingDatabase::class.java,
            "hiking_database"
        )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideActivityRecordDao(database: HikingDatabase): ActivityRecordDao {
        return database.activityRecordDao()
    }
}
