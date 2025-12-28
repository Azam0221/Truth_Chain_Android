package com.example.photosnap.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface EvidenceDao {

    @Insert
    suspend fun insertEvidence(evidence: EvidenceEntity): Long

    @Update
    suspend fun updateEvidence(evidence: EvidenceEntity)


    @Query("SELECT * FROM pending_evidence WHERE isSynced = 1 ORDER BY timestamp DESC")
    fun getVerifiedEvidence(): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM pending_evidence WHERE isSynced = 0 ORDER BY timestamp DESC")
    fun getPendingEvidence(): Flow<List<EvidenceEntity>>


    @Query("SELECT * FROM pending_evidence WHERE isSynced = 0")
    suspend fun getAllPendingList(): List<EvidenceEntity>
    
 
    @Delete
    suspend fun deleteEvidence(evidence: EvidenceEntity)
}