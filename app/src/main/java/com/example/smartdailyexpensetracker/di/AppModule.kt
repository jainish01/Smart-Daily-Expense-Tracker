package com.example.smartdailyexpensetracker.di

import android.app.Application
import androidx.room.Room
import com.example.smartdailyexpensetracker.data.local.ExpenseDatabase
import com.example.smartdailyexpensetracker.data.local.ExpenseDao
import com.example.smartdailyexpensetracker.data.repository.ExpenseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): ExpenseDatabase =
        Room.databaseBuilder(
            app,
            ExpenseDatabase::class.java,
            "expense_db"
        ).fallbackToDestructiveMigration().build()

    @Provides
    fun provideExpenseDao(db: ExpenseDatabase): ExpenseDao = db.expenseDao()

    @Provides
    @Singleton
    fun provideExpenseRepository(db: ExpenseDatabase): ExpenseRepository =
        ExpenseRepository(db.expenseDao())

}
