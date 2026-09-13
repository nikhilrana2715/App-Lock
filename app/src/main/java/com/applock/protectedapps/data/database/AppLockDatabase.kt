package com.applock.protectedapps.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.applock.protectedapps.data.dao.LockedAppDao
import com.applock.protectedapps.data.entity.LockedAppEntity

@Database(entities = [LockedAppEntity::class], version = 1, exportSchema = false)
abstract class AppLockDatabase : RoomDatabase() {
    abstract fun lockedAppDao(): LockedAppDao
}
