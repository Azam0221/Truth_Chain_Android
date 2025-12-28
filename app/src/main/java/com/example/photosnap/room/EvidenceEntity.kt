package com.example.photosnap.room

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "pending_evidence")
data class EvidenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String,
    val metaData: String,
    val signature: String,
    val publicKey: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
