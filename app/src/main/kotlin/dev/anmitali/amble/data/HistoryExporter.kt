// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.data

import android.content.Context
import android.net.Uri
import dev.anmitali.amble.data.db.StepSource
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

private const val FORMAT_NAME = "amble-history"
private const val FORMAT_VERSION = 1

class HistoryExporter(private val context: Context, private val stepsRepository: StepsRepository) {

    suspend fun exportTo(uri: Uri) {
        val days = stepsRepository.getAllDays()
        val json = JSONObject().apply {
            put("format", FORMAT_NAME)
            put("formatVersion", FORMAT_VERSION)
            put("exportedAt", Instant.now().toString())
            put(
                "days",
                JSONArray().apply {
                    days.forEach { day ->
                        put(
                            JSONObject().apply {
                                put("date", day.date)
                                put("steps", day.steps)
                                put("source", day.source)
                            },
                        )
                    }
                },
            )
        }

        val stream =
            context.contentResolver.openOutputStream(uri) ?: throw IOException("Could not open destination file")
        stream.use { it.write(json.toString(2).toByteArray(Charsets.UTF_8)) }
    }

    suspend fun importFrom(uri: Uri): Int {
        val stream = context.contentResolver.openInputStream(uri) ?: throw IOException("Could not open file")
        val text = stream.use { it.readBytes().toString(Charsets.UTF_8) }

        val json = JSONObject(text)
        if (json.optString("format") != FORMAT_NAME) {
            throw IOException("Not an Amble history export")
        }

        val days = json.getJSONArray("days")
        for (i in 0 until days.length()) {
            val entry = days.getJSONObject(i)
            val date = LocalDate.parse(entry.getString("date"))
            val steps = entry.getInt("steps")
            val source = StepSource.entries.find { it.name == entry.optString("source") } ?: StepSource.COUNTER
            stepsRepository.importDay(date, steps, source)
        }
        return days.length()
    }
}
