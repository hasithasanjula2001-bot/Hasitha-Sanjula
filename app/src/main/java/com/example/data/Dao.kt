package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VerifiedLcDao {
    @Query("SELECT * FROM verified_lcs ORDER BY verifiedAtMillis DESC")
    fun observeAllLcs(): Flow<List<VerifiedLcEntity>>

    @Query("SELECT * FROM verified_lcs WHERE id = :id")
    fun observeLcById(id: Long): Flow<VerifiedLcEntity?>

    @Query("SELECT * FROM verified_lcs ORDER BY verifiedAtMillis DESC")
    suspend fun getAllLcsSnapshot(): List<VerifiedLcEntity>

    @Query("SELECT COUNT(*) FROM verified_lcs")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLc(lc: VerifiedLcEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lcs: List<VerifiedLcEntity>)

    @Query("DELETE FROM verified_lcs WHERE id = :id")
    suspend fun deleteLcById(id: Long)
}

@Dao
interface SanctionEntryDao {
    @Query("SELECT * FROM sanction_entries ORDER BY riskTier ASC, entityName ASC")
    fun observeAllSanctions(): Flow<List<SanctionEntryEntity>>

    @Query("SELECT * FROM sanction_entries")
    suspend fun getAllSnapshot(): List<SanctionEntryEntity>

    @Query("SELECT COUNT(*) FROM sanction_entries")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SanctionEntryEntity>)
}

@Dao
interface ComplianceRuleDao {
    @Query("SELECT * FROM compliance_rules ORDER BY category ASC, ruleCode ASC")
    fun observeAllRules(): Flow<List<ComplianceRuleEntity>>

    @Query("SELECT * FROM compliance_rules")
    suspend fun getAllSnapshot(): List<ComplianceRuleEntity>

    @Query("SELECT COUNT(*) FROM compliance_rules")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<ComplianceRuleEntity>)
}

@Dao
interface ExposureLimitDao {
    @Query("SELECT * FROM exposure_limits ORDER BY scope ASC, targetName ASC")
    fun observeAllLimits(): Flow<List<ExposureLimitEntity>>

    @Query("SELECT * FROM exposure_limits")
    suspend fun getAllSnapshot(): List<ExposureLimitEntity>

    @Query("SELECT COUNT(*) FROM exposure_limits")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimit(limit: ExposureLimitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(limits: List<ExposureLimitEntity>)

    @Update
    suspend fun updateLimit(limit: ExposureLimitEntity)
}
