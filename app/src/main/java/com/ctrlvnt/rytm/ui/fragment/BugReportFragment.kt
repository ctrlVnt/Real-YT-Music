package com.ctrlvnt.rytm.ui.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ctrlvnt.rytm.R
import com.ctrlvnt.rytm.ui.services.GithubIssueReporter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject

data class ReportedIssue(val title: String, val url: String)

class ReportedIssuesAdapter(
    private val issues: MutableList<ReportedIssue>,
    private val onIssueClicked: (ReportedIssue) -> Unit,
    private val onDeleteClicked: (ReportedIssue, Int) -> Unit
) : RecyclerView.Adapter<ReportedIssuesAdapter.IssueViewHolder>() {

    class IssueViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.issue_title_text)
        val urlText: TextView = view.findViewById(R.id.issue_url_text)
        val deleteBtn: ImageButton = view.findViewById(R.id.delete_issue_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reported_issue, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issues[position]
        holder.titleText.text = issue.title
        holder.urlText.text = issue.url

        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onIssueClicked(issues[currentPosition])
            }
        }

        holder.deleteBtn.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onDeleteClicked(issues[currentPosition], currentPosition)
            }
        }
    }

    override fun getItemCount(): Int = issues.size
}

class BugReportFragment : Fragment() {

    private lateinit var titleInput: TextInputEditText
    private lateinit var descInput: TextInputEditText
    private lateinit var submitButton: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportedIssuesAdapter
    private val reportedIssues = mutableListOf<ReportedIssue>()

    private val PREFS_NAME = "bug_report_prefs"
    private val ISSUES_KEY = "saved_issues"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bug_report, container, false)

        titleInput = view.findViewById(R.id.bug_title_input)
        descInput = view.findViewById(R.id.bug_desc_input)
        submitButton = view.findViewById(R.id.submit_bug_button)
        recyclerView = view.findViewById(R.id.issues_recycler_view)

        setupRecyclerView()
        loadIssues()

        submitButton.setOnClickListener { submitBug() }

        return view
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReportedIssuesAdapter(
            issues = reportedIssues,
            onIssueClicked = { issue ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(issue.url))
                startActivity(intent)
            },
            onDeleteClicked = { issue, position ->
                reportedIssues.removeAt(position)
                adapter.notifyItemRemoved(position)
                saveIssues()
                Toast.makeText(requireContext(), "Issue rimosso dalla lista", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter
    }

    private fun submitBug() {
        val title = titleInput.text.toString().trim()
        val desc = descInput.text.toString().trim()

        if (title.isEmpty()) {
            titleInput.error = "Title is required"
            return
        }

        submitButton.isEnabled = false
        Toast.makeText(requireContext(), "Sending report...", Toast.LENGTH_SHORT).show()

        GithubIssueReporter.reportBug(
            context = requireContext(),
            title = title,
            description = desc.ifEmpty { null },
            onSuccess = { issueUrl ->
                requireActivity().runOnUiThread {
                    submitButton.isEnabled = true
                    titleInput.text?.clear()
                    descInput.text?.clear()

                    val newIssue = ReportedIssue(title, issueUrl)
                    reportedIssues.add(0, newIssue)
                    adapter.notifyItemInserted(0)
                    recyclerView.scrollToPosition(0)
                    saveIssues()

                    Toast.makeText(requireContext(), "Report sent successfully!", Toast.LENGTH_LONG).show()
                }
            },
            onError = {
                requireActivity().runOnUiThread {
                    submitButton.isEnabled = true
                    Toast.makeText(requireContext(), "Failed to send report", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun saveIssues() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (issue in reportedIssues) {
            val jsonObject = JSONObject().apply {
                put("title", issue.title)
                put("url", issue.url)
            }
            jsonArray.put(jsonObject)
        }
        prefs.edit().putString(ISSUES_KEY, jsonArray.toString()).apply()
    }

    private fun loadIssues() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(ISSUES_KEY, null)
        reportedIssues.clear()

        if (!jsonString.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val jsonObj = jsonArray.getJSONObject(i)
                    val title = jsonObj.getString("title")
                    val url = jsonObj.getString("url")
                    reportedIssues.add(ReportedIssue(title, url))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        adapter.notifyDataSetChanged()
    }
}