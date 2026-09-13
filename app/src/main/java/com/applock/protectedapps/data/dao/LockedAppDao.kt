package com.applock.protectedapps.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.applock.protectedapps.data.entity.LockedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {

    @Query("SELECT * FROM locked_apps ORDER BY appName ASC")
    fun getAllLockedAppsFlow(): Flow<List<LockedAppEntity>>

    @Query("SELECT packageName FROM locked_apps WHERE isLocked = 1")
    suspend fun getLockedPackageNames(): List<String>

    @Query("SELECT packageName FROM locked_apps WHERE isLocked = 1")
    fun getLockedPackageNamesFlow(): Flow<List<String>>

    @Query("SELECT isLocked FROM locked_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun isAppLocked(packageName: String): Boolean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: LockedAppEntity)

    @Query("UPDATE locked_apps SET isLocked = :isLocked WHERE packageName = :packageName")
    suspend fun setAppLockedState(packageName: String, isLocked: Boolean)

    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)
}
