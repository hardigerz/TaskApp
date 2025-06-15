package com.testhar.taskapp.ui.activity

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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(contentRoot)
        applyPropertyInset()

        setupStatusDropdown()

        editingTaskId = intent.getIntExtra(EXTRA_TASK_ID, -1).takeIf { it != -1 }

        // If we have an ID, load that task
        editingTaskId?.let { loadTaskData(it) }

        binding.btnSave.setOnClickListener { saveTask() }

        // optional fade animation
        window.enterTransition = android.transition.Fade()
        window.exitTransition = android.transition.Fade()
    }

    /* ---------------- dropdown ---------------- */

    private fun setupStatusDropdown() {
        val opts = listOf("Pending", "Completed")
        binding.autoCompleteStatus.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, opts)
        )
        binding.autoCompleteStatus.setText("Pending", false)
    }

    /* ---------------- load / save ---------------- */

    private fun loadTaskData(id: Int) {
        lifecycleScope.launch {
            viewModel.getTaskById(id)?.let { task ->
                binding.etTitle.setText(task.title)
                binding.etDescription.setText(task.description ?: "")
                binding.autoCompleteStatus.setText(
                    if (task.isCompleted) "Completed" else "Pending",
                    false
                )
            }
        }
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val isCompleted = binding.autoCompleteStatus.text.toString() == "Completed"

        if (title.isBlank()) {
            binding.etTitle.error = "Title required"
            return
        }

        if (editingTaskId == null) {
            // ➕ Add
            viewModel.addTask(title, description, isCompleted)
            showToast(this, "Task added ✅")
        } else {
            // ✏️ Update
            lifecycleScope.launch {
                viewModel.getTaskById(editingTaskId!!)?.let { old ->
                    viewModel.updateTask(
                        old.copy(
                            title = title,
                            description = description,
                            isCompleted = isCompleted
                        )
                    )
                    showToast(context = applicationContext, "Task updated ✅")
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