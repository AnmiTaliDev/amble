// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.ui.settings

data class OssLibrary(val name: String, val license: String, val copyright: String, val url: String)

val OSS_LIBRARIES = listOf(
    OssLibrary(
        name = "Kotlin",
        license = "Apache License 2.0",
        copyright = "Copyright 2010-2026 JetBrains s.r.o. and contributors",
        url = "https://github.com/JetBrains/kotlin/blob/master/LICENSE",
    ),
    OssLibrary(
        name = "kotlinx.coroutines",
        license = "Apache License 2.0",
        copyright = "Copyright 2016-2026 JetBrains s.r.o.",
        url = "https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt",
    ),
    OssLibrary(
        name = "AndroidX (Compose, Room, Lifecycle, Navigation, Glance, Core)",
        license = "Apache License 2.0",
        copyright = "Copyright The Android Open Source Project",
        url = "https://github.com/androidx/androidx/blob/androidx-main/LICENSE.txt",
    ),
    OssLibrary(
        name = "SQLCipher for Android",
        license = "BSD 3-Clause License",
        copyright = "Copyright 2008-2023 ZETETIC LLC",
        url = "https://github.com/sqlcipher/sqlcipher-android/blob/master/LICENSE",
    ),
)
