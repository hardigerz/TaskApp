package com.testhar.taskapp.ui.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.testhar.taskapp.databinding.ActivityAddEditTaskBinding
import com.testhar.taskapp.domain.model.Task
import com.testhar.taskapp.ui.common.hideKeyboard
import com.testhar.taskapp.ui.common.showSnackbar
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

    // Current task being edited, null if adding
    private var currentTask: Task? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(contentRoot)
        applyPropertyInset()

        // Setup Spinner with status options
        setupStatusSpinner()

        // Check if editing an existing task (taskId passed via Intent)
        editingTaskId = intent.getIntExtra(EXTRA_TASK_ID, -1).takeIf { it != -1 }
        editingTaskId?.let { loadTask(it) } // Load task into form if editing

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val desc = binding.etDescription.text.toString()
            val status = binding.spinnerStatus.selectedItem.toString()

            if (title.isBlank()) {
                binding.etTitle.error = "Title required"
                return@setOnClickListener
            }
            // Determine if status is "Completed"
            val isCompleted = status == "Completed"
//            viewModel.addTask(title, if (desc.isBlank()) null else desc, isCompleted)
//            finish()

            // If editing, update the existing task
            if (currentTask != null) {
                viewModel.updateTask(currentTask!!.copy(
                    title = title,
                    description = if (desc.isBlank()) null else desc,
                    isCompleted = isCompleted
                ))
                showSnackbar(binding.root, "Task updated successfully ✅")
            } else {
                // Otherwise, insert a new task
                viewModel.addTask(title, if (desc.isBlank()) null else desc, isCompleted)
                showSnackbar(binding.root, "Task updated successfully ✅")
            }

            // Close the screen
            hideKeyboard()
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 600)
        }
    }

    // Initialize Spinner with simple string list
    private fun setupStatusSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Pending", "Completed")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = adapter
    }

    // If editing, load task by ID and pre-fill the fields
    private fun loadTask(taskId: Int) {
        lifecycleScope.launch {
            viewModel.getTaskById(taskId)?.let { task ->
                currentTask = task
                binding.etTitle.setText(task.title)
                binding.etDescription.setText(task.description)
                val statusIndex = if (task.isCompleted) 1 else 0
                binding.spinnerStatus.setSelection(statusIndex)
            }
        }
    }


    private fun applyPropertyInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "task_id" // used in intent to pass taskId
    }
}