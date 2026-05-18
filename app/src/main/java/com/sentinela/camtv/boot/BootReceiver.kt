package com.sentinela.camtv.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sentinela.camtv.MainActivity
import com.sentinela.camtv.preferences.playerPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val preferences = playerPreferencesRepository(context).preferences.first()
                if (!preferences.autoStartOnBoot) return@runCatching

                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(launchIntent)
            }.onFailure { error ->
                Timber.w(error, "Falha ao iniciar app no boot")
            }
            pendingResult.finish()
        }
    }
}
