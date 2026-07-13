// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble

import android.app.Application
import dev.anmitali.amble.data.StepsRepository
import dev.anmitali.amble.data.db.AmbleDatabase
import dev.anmitali.amble.data.db.DatabaseKeyStore
import dev.anmitali.amble.data.profile.ProfileStore

class AmbleApplication : Application() {

    lateinit var stepsRepository: StepsRepository
        private set

    lateinit var profileStore: ProfileStore
        private set

    override fun onCreate() {
        super.onCreate()
        val passphrase = DatabaseKeyStore(this).getOrCreatePassphrase()
        val database = AmbleDatabase.create(this, passphrase)
        stepsRepository = StepsRepository(database.dailyStepsDao())
        profileStore = ProfileStore(this)
    }
}
