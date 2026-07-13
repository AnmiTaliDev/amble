// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.profile

import androidx.lifecycle.ViewModel
import dev.anmitali.amble.data.profile.ProfileStore
import dev.anmitali.amble.data.profile.UserProfile
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel(private val profileStore: ProfileStore) : ViewModel() {
    val profile: StateFlow<UserProfile?> = profileStore.profile

    fun save(profile: UserProfile) = profileStore.save(profile)
}
