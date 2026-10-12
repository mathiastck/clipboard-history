package com.mattkanninen.cliphistory.di

import android.content.Context
import androidx.room.Room
import com.mattkanninen.cliphistory.data.local.ClipDao
import com.mattkanninen.cliphistory.data.local.ClipDatabase
import com.mattkanninen.cliphistory.data.repository.ClipRepository
import com.mattkanninen.cliphistory.data.repository.ClipRepositoryImpl
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
    fun provideClipDatabase(@ApplicationContext context: Context): ClipDatabase {
        return Room.databaseBuilder(
            context,
            ClipDatabase::class.java,
            ClipDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideClipDao(database: ClipDatabase): ClipDao {
        return database.clipDao()
    }

    @Provides
    @Singleton
    fun provideClipRepository(clipDao: ClipDao): ClipRepository {
        return ClipRepositoryImpl(clipDao)
    }
}
