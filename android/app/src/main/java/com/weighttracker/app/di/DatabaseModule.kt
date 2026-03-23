package com.weighttracker.app.di

import android.content.Context
import androidx.room.Room
import com.weighttracker.app.data.local.dao.WeightRecordDao
import com.weighttracker.app.data.local.database.AppDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideWeightRecordDao(database: AppDatabase): WeightRecordDao {
        return database.weightRecordDao()
    }
}
