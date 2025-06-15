package com.testhar.taskapp.ui.activity

import com.testhar.taskapp.R
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.testhar.taskapp.databinding.ActivityAddEditTaskBinding
import com.testhar.taskapp.ui.common.hideKeyboard
import com.testhar.taskapp.ui.common.showToast
import com.testhar.taskapp.ui.viewmodel.TaskViewModel
import com.testhar.taskapp.utils.TaskStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AddEditTaskActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAddEditTaskBinding.inflate(layoutInflater) }
    private val contentRoot by lazy { binding.root }
    private val viewModel: TaskViewModel by viewModels()

    // Task ID passed via Intent for editing (null if adding)
    private var editingTaskId: Int? = null

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply fade before super.onCreate
        window.enterTransition = android.transition.Fade().apply {
            duration = 200 // Optional: customize speed
        }
        window.returnTransition = null  // No return animation
        window.exitTransition = null    // No exit animation
        window.reenterTransition = null // No reenter animation

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(contentRoot)
        applyPropertyInset()
        setupStatusDropdown()

        editingTaskId = intent.getIntExtra(EXTRA_TASK_ID, -1).takeIf { it != -1 }

        // If we have an ID, load that task
        editingTaskId?.let { loadTaskData(it) }

        binding.btnSave.setOnClickListener { saveTask() }

    }


    /* ---------------- dropdown ---------------- */

    private fun setupStatusDropdown() {
        val statuses = TaskStatus.entries
        val statusStrings = statuses.map { getString(it.stringResId) }
        binding.autoCompleteStatus.setAdapter(
            ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, statusStrings)
        )
        binding.autoCompleteStatus.setText(getString(TaskStatus.Pending.stringResId), false)
    }

    /* ---------------- load / save ---------------- */

    private fun loadTaskData(id: Int) {
        lifecycleScope.launch {
            viewModel.getTaskById(id)?.let { task ->
                binding.etTitle.setText(task.title)
                binding.etDescription.setText(task.description ?: "")
                binding.autoCompleteStatus.setText(
                    getString(TaskStatus.fromCompleted(task.isCompleted).stringResId), false
                )
            }
        }
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val selectedStatus = TaskStatus.fromLocalizedName(this, binding.autoCompleteStatus.text.toString())
        val isCompleted = selectedStatus.isCompleted


        if (title.isBlank()) {
            binding.etTitle.error = getString(R.string.error_title_required)
            return
        }

        if (editingTaskId == null) {
            // Add
            viewModel.addTask(title, description, isCompleted)
            showToast(this, getString(R.string.task_added))
        } else {
            // Update
            lifecycleScope.launch {
                viewModel.getTaskById(editingTaskId!!)?.let { old ->
                    viewModel.updateTask(
                        old.copy(
                            title = title,
                            description = description,
                            isCompleted = isCompleted
                        )
                    )
                    showToast(context = applicationContext, getString(R.string.task_updated))
                }
            }
        }
        hideKeyboard()
        finishAfterTransition()
    }

    private fun applyPropertyInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}