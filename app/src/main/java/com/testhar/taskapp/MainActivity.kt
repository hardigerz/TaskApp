package com.testhar.taskapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.testhar.taskapp.databinding.ActivityMainBinding
import com.testhar.taskapp.ui.activity.AddEditTaskActivity
import com.testhar.taskapp.ui.activity.AddEditTaskActivity.Companion.EXTRA_TASK_ID
import com.testhar.taskapp.ui.adapter.TaskAdapter
import com.testhar.taskapp.ui.common.showConfirmDialog
import com.testhar.taskapp.ui.common.showSnackbar
import com.testhar.taskapp.ui.viewmodel.TaskViewModel
import com.testhar.taskapp.utils.Constants.KEY_SCROLL_POSITION
import com.testhar.taskapp.utils.TaskStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val contentRoot by lazy { binding.root }
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter

    private var lastFilter: TaskStatus = TaskStatus.All
    private var scrollPositionToRestore: Int = 0




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(contentRoot)

        scrollPositionToRestore = savedInstanceState?.getInt(KEY_SCROLL_POSITION) ?: 0
        applyPropertyInset()
        setupAdapter()
        setupChips()
        setupObservers()
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddEditTaskActivity::class.java))
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            taskViewModel.filteredTasks.collectLatest { tasks ->
                adapter.submitList(tasks.toList()) {
                    val isFilterChanged = lastFilter != taskViewModel.filter.value
                    if (isFilterChanged) {
                        binding.recyclerView.smoothScrollToPosition(0)
                    } else {
                        binding.recyclerView.scrollToPosition(scrollPositionToRestore)
                    }
                    lastFilter = taskViewModel.filter.value

                }
                val isEmpty = tasks.isEmpty()
                binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE

                if (isEmpty) {
                    val filterState = taskViewModel.filter.value
                    val messageRes = when (filterState) {
                        TaskStatus.All -> R.string.no_tasks
                        TaskStatus.Completed -> R.string.no_completed_tasks
                        TaskStatus.Pending -> R.string.no_pending_tasks
                    }
                    binding.emptyView.text = getString(messageRes)
                }
            }
        }
    }

    private fun setupAdapter() {
        adapter = TaskAdapter(
            onDeleteClick = { task ->
                showConfirmDialog(
                    context = this,
                    title = getString(R.string.title_delete_task),
                    message = getString(R.string.title_message_delete)
                ) {
                    taskViewModel.deleteTask(task)
                    showSnackbar(binding.root, getString(R.string.task_delete))
                }
            },
            onItemClick = { task ->
                val intent = Intent(this, AddEditTaskActivity::class.java).apply {
                    putExtra(EXTRA_TASK_ID, task.id)
                }
               startActivity(intent)
            },
            onStatusToggle = { task, isChecked ->
                taskViewModel.updateTask(task.copy(isCompleted = isChecked))
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter

            itemAnimator = null // flicker animation prevent
        }
    }


    private fun setupChips() {
        // Apply chip selection from ViewModel state
        when (taskViewModel.filter.value) {
            TaskStatus.All -> binding.chipGroup.check(binding.chipAll.id)
            TaskStatus.Completed -> binding.chipGroup.check(binding.chipCompleted.id)
            TaskStatus.Pending -> binding.chipGroup.check(binding.chipPending.id)
        }

        binding.chipAll.setOnClickListener {
            if (taskViewModel.filter.value != TaskStatus.All)
                taskViewModel.setFilter(TaskStatus.All)
        }
        binding.chipCompleted.setOnClickListener {
            if (taskViewModel.filter.value != TaskStatus.Completed)
                taskViewModel.setFilter(TaskStatus.Completed)
        }
        binding.chipPending.setOnClickListener {
            if (taskViewModel.filter.value != TaskStatus.Pending)
                taskViewModel.setFilter(TaskStatus.Pending)
        }
    }


    private fun applyPropertyInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val layoutManager = binding.recyclerView.layoutManager as? LinearLayoutManager
        val firstVisible = layoutManager?.findFirstVisibleItemPosition() ?: 0
        outState.putInt(KEY_SCROLL_POSITION, firstVisible)
    }


}