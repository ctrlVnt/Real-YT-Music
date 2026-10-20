package com.ctrlvnt.rytm.ui.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ctrlvnt.rytm.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object GithubUpdateChecker {

    private const val REPO = "ctrlVnt/Real-YT-Music"
    private const val LATEST_API = "https://api.github.com/repos/$REPO/releases/latest"
    private const val LATEST_PAGE = "https://github.com/$REPO/releases/latest"
    private const val UPDATE_FILE_NAME = "rytm-update.apk"
    private const val INSTALL_ACTION = "com.ctrlvnt.rytm.INSTALL_STATUS"
    private const val CANCEL_ACTION = "com.ctrlvnt.rytm.UPDATE_CANCEL"
    private const val CHANNEL_ID = "rytm_update"
    private const val NOTIFICATION_ID = 4645

    private var checked = false
    private var updateInFlight = false
    private var cancelled = false
    private val client = OkHttpClient()

    data class Release(val tag: String, val notes: String, val apkUrl: String, val size: Long)

    /**
     * Controlla gli aggiornamenti.
     * @param manual Se true, mostra Toast anche in caso di nessun aggiornamento.
     */
    fun check(context: Context, manual: Boolean = false) {
        if (checked && !manual) return
        checked = true

        if (manual) Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder().url(LATEST_API)
                    .header("User-Agent", "RYTM-Updater")
                    .header("Accept", "application/vnd.github+json")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        if (manual) withContext(Dispatchers.Main) { toast(context, "Update check failed") }
                        return@use
                    }

                    val json = JSONObject(response.body?.string() ?: "")
                    val tag = json.optString("tag_name", "")
                    val notes = json.optString("body", "No release notes provided.")

                    if (tag.isEmpty()) return@use

                    val installed = getInstalledVersion(context)
                    if (isNewer(normalize(tag), installed)) {
                        val release = extractApkData(json, tag, notes)
                        if (release != null) {
                            withContext(Dispatchers.Main) { showUpdatePrompt(context, release, installed) }
                        } else if (manual) {
                            withContext(Dispatchers.Main) { toast(context, "No APK found in the latest release") }
                        }
                    } else if (manual) {
                        withContext(Dispatchers.Main) { toast(context, "RYTM is up to date ($installed)") }
                    }
                }
            } catch (e: Exception) {
                if (manual) withContext(Dispatchers.Main) { toast(context, "Update check failed: ${e.message}") }
            }
        }
    }

    private fun extractApkData(json: JSONObject, tag: String, notes: String): Release? {
        val assets = json.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.optString("name", "")
            if (name.endsWith(".apk")) {
                return Release(
                    tag = tag,
                    notes = notes,
                    apkUrl = asset.optString("browser_download_url"),
                    size = asset.optLong("size", 0L)
                )
            }
        }
        return null
    }

    private fun showUpdatePrompt(context: Context, release: Release, installed: String) {
        val scrollView = ScrollView(context).apply {
            setPadding(48, 24, 48, 0)
        }
        val notesText = TextView(context).apply {
            text = "You have: $installed\nLatest: ${release.tag}\n\nWhat's new:\n${stripMarkdown(release.notes)}"
            textSize = 14f
        }
        scrollView.addView(notesText)

        MaterialAlertDialogBuilder(context, R.style.RoundedAlertDialog)
            .setTitle("Update Available!")
            .setView(scrollView)
            .setPositiveButton("Update") { dialog, _ ->
                if (!canRequestInstall(context)) {
                    requestInstallPermission(context)
                    Toast.makeText(context, "Allow installing apps, then tap Update again.", Toast.LENGTH_LONG).show()
                } else {
                    startDownloadAndInstall(context, release)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Later") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun startDownloadAndInstall(context: Context, release: Release) {
        if (updateInFlight) {
            Toast.makeText(context, "Update already in progress", Toast.LENGTH_SHORT).show()
            return
        }
        updateInFlight = true
        cancelled = false
        val appContext = context.applicationContext

        // Prepara UI del progresso
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        val statusText = TextView(context).apply { text = "Starting download..." }
        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, 24, 0, 24)
            }
        }
        layout.addView(statusText)
        layout.addView(progressBar)

        val progressDialog = MaterialAlertDialogBuilder(context, R.style.RoundedAlertDialog)
            .setTitle("Updating RYTM ${release.tag}")
            .setView(layout)
            .setCancelable(false)
            .setNegativeButton("Cancel") { _, _ -> cancelled = true }
            .create()

        progressDialog.show()
        registerCancelReceiver(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            val dest = File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), UPDATE_FILE_NAME)
            if (dest.exists()) dest.delete()

            try {
                val request = Request.Builder().url(release.apkUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Failed to download")

                    val total = response.body?.contentLength() ?: release.size
                    val inputStream = response.body?.byteStream() ?: throw Exception("No body")
                    val outputStream = FileOutputStream(dest)

                    withContext(Dispatchers.Main) { progressBar.isIndeterminate = false; progressBar.max = 100 }

                    var done = 0L
                    var lastUpdate = 0L
                    val buffer = ByteArray(8192)
                    var read: Int

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        if (cancelled) break
                        outputStream.write(buffer, 0, read)
                        done += read

                        if (total > 0 && done - lastUpdate > 150_000) { // Aggiorna UI ogni ~150KB
                            lastUpdate = done
                            val progress = (done * 100 / total).toInt()
                            withContext(Dispatchers.Main) {
                                progressBar.progress = progress
                                statusText.text = "Downloading... $progress%"
                            }
                            updateNotification(appContext, "Downloading... $progress%", progress)
                        }
                    }
                    outputStream.close()
                }

                if (cancelled) {
                    dest.delete()
                    cleanup(appContext, progressDialog)
                    withContext(Dispatchers.Main) { toast(appContext, "Update cancelled") }
                    return@launch
                }

                withContext(Dispatchers.Main) { statusText.text = "Preparing installation..." }
                updateNotification(appContext, "Preparing installation...", 100)

                stageAndInstall(appContext, dest)
                cleanup(appContext, progressDialog)

            } catch (e: Exception) {
                dest.delete()
                cleanup(appContext, progressDialog)
                withContext(Dispatchers.Main) { toast(appContext, "Download failed") }
            }
        }
    }

    private fun stageAndInstall(context: Context, apk: File) {
        val installer = context.packageManager.packageInstaller
        var session: PackageInstaller.Session? = null
        try {
            val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
            val sessionId = installer.createSession(params)
            session = installer.openSession(sessionId)

            FileInputStream(apk).use { input ->
                session.openWrite("rytm_update", 0, apk.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }

            val action = "$INSTALL_ACTION.$sessionId"
            registerInstallReceiver(context, action, apk)

            val intent = Intent(action).setPackage(context.packageName)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, flags)
            session.commit(pendingIntent.intentSender)

        } catch (e: Exception) {
            apk.delete()
            session?.abandon()
            CoroutineScope(Dispatchers.Main).launch { toast(context, "Install failed") }
        } finally {
            session?.close()
        }
    }

    private fun registerInstallReceiver(context: Context, action: String, apk: File) {
        val receiver = object : BroadcastReceiver() {
            var promptShown = false
            override fun onReceive(c: Context, intent: Intent) {
                val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, Integer.MIN_VALUE)
                if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                    if (promptShown) return
                    val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT) ?: return
                    confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    c.startActivity(confirm)
                    promptShown = true
                    return
                }

                updateInFlight = false
                apk.delete()
                try { c.unregisterReceiver(this) } catch (e: Exception) {}

                if (status != PackageInstaller.STATUS_SUCCESS) {
                    val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                    Toast.makeText(c, "Update failed: $msg", Toast.LENGTH_LONG).show()
                }
            }
        }
        val filter = IntentFilter(action)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    private var cancelReceiver: BroadcastReceiver? = null
    private fun registerCancelReceiver(context: Context) {
        cancelReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) { cancelled = true }
        }
        val filter = IntentFilter(CANCEL_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(cancelReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                context,
                cancelReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    private fun updateNotification(context: Context, text: String, progress: Int) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "RYTM Updates", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val cancelIntent = PendingIntent.getBroadcast(context, 0, Intent(CANCEL_ACTION).setPackage(context.packageName), flags)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Updating RYTM")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelIntent)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    private suspend fun cleanup(context: Context, dialog: AlertDialog) = withContext(Dispatchers.Main) {
        updateInFlight = false
        dialog.dismiss()
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.cancel(NOTIFICATION_ID)
        cancelReceiver?.let { try { context.unregisterReceiver(it) } catch (e: Exception) {} }
    }

    // --- Utils ---
    private fun getInstalledVersion(context: Context): String {
        return try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0" } catch (e: Exception) { "0" }
    }

    private fun normalize(tag: String) = tag.trim().removePrefix("v").removePrefix("V").replace('-', '.')

    private fun isNewer(latest: String, installed: String): Boolean {
        val a = latest.split(".")
        val b = installed.split(".")
        for (i in 0 until Math.max(a.size, b.size)) {
            val x = a.getOrNull(i)?.toIntOrNull() ?: 0
            val y = b.getOrNull(i)?.toIntOrNull() ?: 0
            if (x != y) return x > y
        }
        return false
    }

    private fun stripMarkdown(text: String) = text.replace("**", "").replace("`", "").replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")

    private fun canRequestInstall(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.packageManager.canRequestPackageInstalls() else true
    }

    private fun requestInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e: Exception) {
                context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    private fun toast(context: Context, msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}