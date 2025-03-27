package com.example.where_am_i_app.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.where_am_i_app.model.UserAlertReport

@Dao
interface UserAlertReportDao {

    @Query("SELECT * FROM UserAlertReport")
    fun getAllUserAlertReports(): LiveData<List<UserAlertReport>>

    @Query("SELECT * FROM UserAlertReport WHERE userId = :userId")
    fun getUserAlertReportByUserIdLiveData(userId: String): LiveData<List<UserAlertReport>>

    @Query("SELECT * FROM UserAlertReport WHERE id = :id")
    fun getUserAlertReportById(id: String): UserAlertReport

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg userAlertReport: UserAlertReport)

    @Delete
    fun delete(userAlertReport: UserAlertReport)
}