package com.ctrlvnt.rytm.ui.services

import android.content.Context
import android.os.Build
import android.util.Log
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.utils.apikey.GITHUBKEY
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GithubIssueReporter {

    private const val TAG = "GithubIssueReporter"
    private const val OWNER = "ctrlVnt"
    private const val REPO = "Real-YT-Music"
    private const val API_URL = "https://api.github.com/repos/$OWNER/$REPO/issues"

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * Reports a bug/issue to GitHub.
     *
     * @param title Required. The issue title.
     * @param description Optional. Additional details from the user.
     */
    fun reportBug(
        context: Context,
        title: String,
        description: String? = null,
        onSuccess: (issueUrl: String) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (title.isBlank()) {
            onError(IllegalArgumentException(context.getString(R.string.github_issue_error_title_empty)))
            return
        }

        val appVersion = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app version", e)
            "unknown"
        }

        val issueTitle = context.getString(R.string.github_issue_title_prefix, title.take(80))

        val body = buildString {
            appendLine(context.getString(R.string.github_issue_user_description))
            appendLine(if (description.isNullOrBlank()) context.getString(R.string.github_issue_no_description) else description)
            appendLine()
            appendLine("---")
            appendLine(context.getString(R.string.github_issue_device_info))
            appendLine(context.getString(R.string.github_issue_app_version, appVersion))
            appendLine(context.getString(R.string.github_issue_android_version, Build.VERSION.RELEASE, Build.VERSION.SDK_INT))
            appendLine(context.getString(R.string.github_issue_device, Build.MANUFACTURER, Build.MODEL))
        }


        val jsonBody = JSONObject().apply {
            put("title", issueTitle)
            put("body", body)
            put("labels", JSONArray().put("user-report"))
        }

        Log.d(TAG, "Sending request to: $API_URL")

        val request = Request.Builder()
            .url(API_URL)
            .addHeader("Authorization", "Bearer $GITHUBKEY")
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .post(jsonBody.toString().toRequestBody(JSON))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Request failed (network/connection)", e)
                onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val bodyString = it.body?.string() ?: ""
                    if (it.isSuccessful) {
                        Log.d(TAG, "Issue created successfully: $bodyString")
                        val respJson = JSONObject(bodyString)
                        val htmlUrl = respJson.optString("html_url", "")
                        onSuccess(htmlUrl)
                    } else {
                        Log.e(TAG, "GitHub API error - code: ${it.code}, body: $bodyString")
                        onError(IOException("GitHub API error: ${it.code} $bodyString"))
                    }
                }
            }
        })
    }
}