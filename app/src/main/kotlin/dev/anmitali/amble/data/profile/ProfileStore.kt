// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.data.profile

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import dev.anmitali.amble.data.crypto.KeystoreCipher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

class ProfileStore(context: Context) {
    private val prefs = context.getSharedPreferences("amble_profile", Context.MODE_PRIVATE)
    private val cipher = KeystoreCipher("amble_profile_key")

    private val _profile = MutableStateFlow(readProfile())
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    fun save(profile: UserProfile) {
        val serialized = listOf(
            profile.weightKg.toString(),
            profile.heightCm.toString(),
            profile.birthDate.toString(),
            profile.sex.name,
            profile.strideLengthCm?.toString().orEmpty(),
            profile.dailyStepGoal.toString(),
            profile.dailyDistanceGoalKm.toString(),
            profile.dailyCalorieGoal.toString(),
        ).joinToString(FIELD_SEPARATOR)

        val encrypted = cipher.encrypt(serialized.toByteArray(Charsets.UTF_8))
        prefs.edit { putString(KEY_PROFILE, Base64.encodeToString(encrypted, Base64.NO_WRAP)) }
        _profile.value = profile
    }

    private fun readProfile(): UserProfile? {
        val stored = prefs.getString(KEY_PROFILE, null) ?: return null
        return try {
            val decrypted = cipher.decrypt(Base64.decode(stored, Base64.NO_WRAP))
            val parts = String(decrypted, Charsets.UTF_8).split(FIELD_SEPARATOR)
            UserProfile(
                weightKg = parts[0].toFloat(),
                heightCm = parts[1].toFloat(),
                birthDate = LocalDate.parse(parts[2]),
                sex = Sex.valueOf(parts[3]),
                strideLengthCm = parts[4].takeIf { it.isNotEmpty() }?.toFloat(),
                dailyStepGoal = parts.getOrNull(5)?.toIntOrNull() ?: 10_000,
                dailyDistanceGoalKm = parts.getOrNull(6)?.toFloatOrNull() ?: 5f,
                dailyCalorieGoal = parts.getOrNull(7)?.toIntOrNull() ?: 300,
            )
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val KEY_PROFILE = "profile_blob"
        const val FIELD_SEPARATOR = "|"
    }
}
