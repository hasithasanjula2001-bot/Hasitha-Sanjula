package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        VerifiedLcEntity::class,
        SanctionEntryEntity::class,
        ComplianceRuleEntity::class,
        ExposureLimitEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun verifiedLcDao(): VerifiedLcDao
    abstract fun sanctionEntryDao(): SanctionEntryDao
    abstract fun complianceRuleDao(): ComplianceRuleDao
    abstract fun exposureLimitDao(): ExposureLimitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meridian_lc_vault.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
