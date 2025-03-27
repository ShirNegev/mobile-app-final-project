package com.example.where_am_i_app.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.where_am_i_app.base.MyApplication
import com.example.where_am_i_app.model.UserAlertReport

@Database(entities = [UserAlertReport::class], version = 1)
abstract class AppLocalDbRepository: RoomDatabase() {
    abstract fun userAlertReportDao(): UserAlertReportDao
}

object AppLocalDb {

    val database: AppLocalDbRepository by lazy {

        val context = MyApplication.Globals.context ?: throw IllegalStateException("Application context is missing")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "dbFileName.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}