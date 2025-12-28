package com.example.photosnap.room



import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [EvidenceEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun evidenceDao(): EvidenceDao
}