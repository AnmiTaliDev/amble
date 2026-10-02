// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

fun renderRingBitmap(
    diameterPx: Int,
    strokeWidthPx: Float,
    gapPx: Float,
    progress: Float,
    ringColor: Int,
    trackColor: Int,
): Bitmap {
    val bitmap = Bitmap.createBitmap(diameterPx, diameterPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
    }
    val inset = strokeWidthPx / 2f
    val rect = RectF(inset, inset, diameterPx - inset, diameterPx - inset)
    val radius = rect.width() / 2f
    val gapDegrees = Math.toDegrees(((gapPx + strokeWidthPx) / radius).toDouble()).toFloat()

    val clamped = progress.coerceIn(0f, 1f)
    val sweep = 360f * clamped
    val trackSweep = 360f - sweep - 2 * gapDegrees

    when {
        clamped == 0f -> {
            paint.color = trackColor
            canvas.drawArc(rect, -90f, 360f, false, paint)
        }
        trackSweep <= 0f -> {
            paint.color = ringColor
            canvas.drawArc(rect, -90f, 360f, false, paint)
        }
        else -> {
            paint.color = trackColor
            canvas.drawArc(rect, -90f + sweep + gapDegrees, trackSweep, false, paint)
            paint.color = ringColor
            canvas.drawArc(rect, -90f, sweep, false, paint)
        }
    }
    return bitmap
}
