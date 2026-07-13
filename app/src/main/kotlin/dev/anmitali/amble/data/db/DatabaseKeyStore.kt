// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.data.db

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import dev.anmitali.amble.data.crypto.KeystoreCipher
import java.security.SecureRandom

class DatabaseKeyStore(context: Context) {
    private val prefs = context.getSharedPreferences("amble_db_key", Context.MODE_PRIVATE)
    private val cipher = KeystoreCipher("amble_db_key_wrap")

    fun getOrCreatePassphrase(): ByteArray {
        prefs.getString(KEY_PASSPHRASE, null)?.let { return cipher.decrypt(Base64.decode(it, Base64.NO_WRAP)) }

        val passphrase = ByteArray(32)
        SecureRandom().nextBytes(passphrase)
        val encrypted = cipher.encrypt(passphrase)
        prefs.edit { putString(KEY_PASSPHRASE, Base64.encodeToString(encrypted, Base64.NO_WRAP)) }
        return passphrase
    }

    private companion object {
        const val KEY_PASSPHRASE = "sqlcipher_passphrase"
    }
}
