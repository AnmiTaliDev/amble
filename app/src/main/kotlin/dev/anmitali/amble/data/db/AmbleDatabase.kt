// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(entities = [DailyStepsEntity::class], version = 1, exportSchema = false)
abstract class AmbleDatabase : RoomDatabase() {
    abstract fun dailyStepsDao(): DailyStepsDao

    companion object {
        fun create(context: Context, passphrase: ByteArray): AmbleDatabase {
            System.loadLibrary("sqlcipher")
            return Room.databaseBuilder(context.applicationContext, AmbleDatabase::class.java, "amble.db")
                .openHelperFactory(SupportOpenHelperFactory(passphrase))
                .build()
        }
    }
}
