package com.applock.protectedapps.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locked_apps")
data class LockedAppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val isLocked: Boolean,
    val addedTimestamp: Long = System.currentTimeMillis()
)
