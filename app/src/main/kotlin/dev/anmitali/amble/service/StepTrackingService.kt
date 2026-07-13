// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTaliDev
package dev.anmitali.amble.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import dev.anmitali.amble.AmbleApplication
import dev.anmitali.amble.MainActivity
import dev.anmitali.amble.R
import dev.anmitali.amble.data.StepsRepository
import dev.anmitali.amble.data.db.StepSource
import dev.anmitali.amble.sensor.StepSensorManager
import dev.anmitali.amble.widget.StepsWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StepTrackingService : LifecycleService() {

    private lateinit var stepsRepository: StepsRepository
    private lateinit var sensorManager: StepSensorManager

    private var liveSteps = 0

    override fun onCreate() {
        super.onCreate()
        stepsRepository = (application as AmbleApplication).stepsRepository
        sensorManager = StepSensorManager(this)

        ensureNotificationChannel()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(0),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
        )
        _isRunning.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        sensorManager.start(
            onCounterReading = { cumulativeSteps ->
                lifecycleScope.launch {
                    liveSteps = stepsRepository.applySensorReading(cumulativeSteps, StepSource.COUNTER)
                    updateNotification(liveSteps)
                    StepsWidget().updateAll(this@StepTrackingService)
                }
            },
            onStepDetected = {
                if (sensorManager.activeSource == StepSource.COUNTER) {
                    liveSteps++
                    updateNotification(liveSteps)
                } else {
                    lifecycleScope.launch {
                        liveSteps = stepsRepository.addSteps(1, sensorManager.activeSource)
                        updateNotification(liveSteps)
                        StepsWidget().updateAll(this@StepTrackingService)
                    }
                }
            },
        )
        _activeSource.value = sensorManager.activeSource
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.stop()
        _isRunning.value = false
        _activeSource.value = null
        super.onDestroy()
    }

    private fun ensureNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.step_tracking_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.step_tracking_channel_description)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(steps: Int): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_steps)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.step_tracking_notification_text, steps))
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(steps: Int) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(steps))
    }

    companion object {
        private const val CHANNEL_ID = "step_tracking"
        private const val NOTIFICATION_ID = 1

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _activeSource = MutableStateFlow<StepSource?>(null)
        val activeSource: StateFlow<StepSource?> = _activeSource.asStateFlow()

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, StepTrackingService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StepTrackingService::class.java))
        }
    }
}
