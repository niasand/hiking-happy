package com.happyclaw.hikinghappy.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.happyclaw.hikinghappy.data.local.HikingDatabase
import com.happyclaw.hikinghappy.data.local.MIGRATION_1_2
import com.happyclaw.hikinghappy.data.local.dao.ActivityRecordDao
import com.happyclaw.hikinghappy.data.local.dao.TrackPointDao
import com.happyclaw.hikinghappy.data.local.dao.TrackSessionDao
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
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideActivityRecordDao(database: HikingDatabase): ActivityRecordDao {
        return database.activityRecordDao()
    }

    @Provides
    fun provideTrackSessionDao(database: HikingDatabase): TrackSessionDao {
        return database.trackSessionDao()
    }

    @Provides
    fun provideTrackPointDao(database: HikingDatabase): TrackPointDao {
        return database.trackPointDao()
    }
}
