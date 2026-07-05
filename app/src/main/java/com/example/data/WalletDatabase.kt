package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Transaction::class, Voucher::class, AppConfig::class, BettingSite::class, PaymentMethodConfig::class, SupportTicket::class, SupportMessage::class, Offer::class], version = 22, exportSchema = false)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: WalletDatabase? = null

        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `vouchers` (`code` TEXT NOT NULL, `amount` REAL NOT NULL, `creatorId` INTEGER NOT NULL, `isRedeemed` INTEGER NOT NULL, `redeemedById` INTEGER, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`code`))")
                database.execSQL("CREATE TABLE IF NOT EXISTS `app_config` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))")
            }
        }

        private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `betting_sites` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `siteName` TEXT NOT NULL, `username` TEXT NOT NULL, `passwordHash` TEXT NOT NULL, `workId` TEXT NOT NULL, `withdrawAddress` TEXT NOT NULL, `balance` REAL NOT NULL, `isActive` INTEGER NOT NULL, `onlyDeposit` INTEGER NOT NULL, `onlyWithdraw` INTEGER NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `payment_methods` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `number` TEXT NOT NULL, `subMethod` TEXT NOT NULL, `isActive` INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                val addColumnSafe = { columnDef: String ->
                    try {
                        database.execSQL("ALTER TABLE `users` ADD COLUMN $columnDef")
                    } catch (e: Exception) {
                        // Column might already exist
                    }
                }
                addColumnSafe("`referralCode` TEXT NOT NULL DEFAULT ''")
                addColumnSafe("`referredBy` TEXT NOT NULL DEFAULT ''")
                addColumnSafe("`bonusPercent` REAL NOT NULL DEFAULT 2.0")
                addColumnSafe("`mainBalance` REAL NOT NULL DEFAULT 0.0")
                addColumnSafe("`withdrawLimit` REAL NOT NULL DEFAULT 0.0")
                addColumnSafe("`holdBalance` REAL NOT NULL DEFAULT 0.0")
                addColumnSafe("`withdrawableBalance` REAL NOT NULL DEFAULT 0.0")
                addColumnSafe("`referBalance` REAL NOT NULL DEFAULT 0.0")
                addColumnSafe("`bonusBalance` REAL NOT NULL DEFAULT 0.0")
                addColumnSafe("`commissionBalance` REAL NOT NULL DEFAULT 0.0")
                addColumnSafe("`gasFeeBalance` REAL NOT NULL DEFAULT 0.0")
                addColumnSafe("`role` TEXT NOT NULL DEFAULT 'User'")
                addColumnSafe("`lastBettingSite` TEXT NOT NULL DEFAULT '1xbet'")
                addColumnSafe("`lastBettingUserId` TEXT NOT NULL DEFAULT ''")
                addColumnSafe("`isBlocked` INTEGER NOT NULL DEFAULT 0")
                addColumnSafe("`isHold` INTEGER NOT NULL DEFAULT 0")
                addColumnSafe("`ignoreWithdrawLimit` INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                val addColumnSafe = { table: String, columnDef: String ->
                    try {
                        database.execSQL("ALTER TABLE `$table` ADD COLUMN $columnDef")
                    } catch (e: Exception) {
                        // Column might already exist
                    }
                }
                addColumnSafe("vouchers", "`balanceType` TEXT NOT NULL DEFAULT 'Main Balance'")
                addColumnSafe("vouchers", "`isCancelled` INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                val addColumnSafe = { table: String, columnDef: String ->
                    try {
                        database.execSQL("ALTER TABLE `$table` ADD COLUMN $columnDef")
                    } catch (e: Exception) {
                        // Column might already exist
                    }
                }
                addColumnSafe("vouchers", "`designatedReceiver` TEXT DEFAULT NULL")
                addColumnSafe("vouchers", "`expiresAt` INTEGER NOT NULL DEFAULT ${System.currentTimeMillis() + 24 * 60 * 60 * 1000}")
            }
        }

        private val MIGRATION_14_15 = object : androidx.room.migration.Migration(14, 15) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                val addColumnSafe = { table: String, columnDef: String ->
                    try {
                        database.execSQL("ALTER TABLE `$table` ADD COLUMN $columnDef")
                    } catch (e: Exception) {
                        // Column might already exist
                    }
                }
                addColumnSafe("users", "`agentCommissionPercent` REAL NOT NULL DEFAULT 2.0")
            }
        }

        private val MIGRATION_15_16 = object : androidx.room.migration.Migration(15, 16) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                val addColumnSafe = { table: String, columnDef: String ->
                    try {
                        database.execSQL("ALTER TABLE `$table` ADD COLUMN $columnDef")
                    } catch (e: Exception) {
                        // Column might already exist
                    }
                }
                addColumnSafe("users", "`canDeposit` INTEGER NOT NULL DEFAULT 1")
                addColumnSafe("users", "`canWithdraw` INTEGER NOT NULL DEFAULT 1")
                addColumnSafe("users", "`canAddMoney` INTEGER NOT NULL DEFAULT 1")
                addColumnSafe("users", "`canOutMoney` INTEGER NOT NULL DEFAULT 1")
                addColumnSafe("users", "`canSendMoney` INTEGER NOT NULL DEFAULT 1")
                addColumnSafe("users", "`canReceiveMoney` INTEGER NOT NULL DEFAULT 1")
                addColumnSafe("users", "`eposCode` TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_18_19 = object : androidx.room.migration.Migration(18, 19) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                val addColumnSafe = { table: String, columnDef: String ->
                    try {
                        database.execSQL("ALTER TABLE `$table` ADD COLUMN $columnDef")
                    } catch (e: Exception) {
                        // Column might already exist
                    }
                }
                addColumnSafe("users", "`hiddenPenaltyMain` REAL NOT NULL DEFAULT 0.0")
                addColumnSafe("users", "`hiddenPenaltyOut` REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_19_20 = object : androidx.room.migration.Migration(19, 20) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                val addColumnSafe = { table: String, columnDef: String ->
                    try {
                        database.execSQL("ALTER TABLE `$table` ADD COLUMN $columnDef")
                    } catch (e: Exception) {
                        // Column might already exist
                    }
                }
                addColumnSafe("users", "`hiddenPenaltyCombined` REAL NOT NULL DEFAULT 0.0")
            }
        }

        private val MIGRATION_20_21 = object : androidx.room.migration.Migration(20, 21) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                val addColumnSafe = { table: String, columnDef: String ->
                    try {
                        database.execSQL("ALTER TABLE `$table` ADD COLUMN $columnDef")
                    } catch (e: Exception) {
                        // Column might already exist
                    }
                }
                addColumnSafe("users", "`agentDpCommissionPercent` REAL NOT NULL DEFAULT 2.0")
                addColumnSafe("users", "`agentWdCommissionPercent` REAL NOT NULL DEFAULT 2.0")
            }
        }

        private val MIGRATION_21_22 = object : androidx.room.migration.Migration(21, 22) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `offers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `dateCreated` TEXT NOT NULL)")
            }
        }

        fun getDatabase(context: Context): WalletDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalletDatabase::class.java,
                    "wallet_database"
                )
                .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
