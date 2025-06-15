package com.testhar.taskapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
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
import com.testhar.taskapp.utils.FilterState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val contentRoot by lazy { binding.root }
    private val taskViewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(contentRoot)
        applyPropertyInset()
        setupAdapter()
        setupChips()
        setupObservers()
        binding.btnAdd.setOnClickListener {
            val intent = Intent(this, AddEditTaskActivity::class.java)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
            startActivity(intent, options.toBundle())
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            taskViewModel.filteredTasks.collectLatest { tasks ->
                adapter.submitList(tasks.toList())
                val isEmpty = tasks.isEmpty()
                binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE

                if (isEmpty) {
                    val filterState = taskViewModel.filter.value
                    val messageRes = when (filterState) {
                        FilterState.All -> R.string.no_tasks
                        FilterState.Completed -> R.string.no_completed_tasks
                        FilterState.Pending -> R.string.no_pending_tasks
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
                    title = "Delete Task",
                    message = "Are you sure want to delete this task"
                ) {
                    taskViewModel.deleteTask(task)
                    showSnackbar(binding.root, "Task Deleted")
                }
            },
            onItemClick = { task ->
                val intent = Intent(this, AddEditTaskActivity::class.java).apply {
                    putExtra(EXTRA_TASK_ID, task.id)
                }
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this)
                startActivity(intent, options.toBundle())
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
            FilterState.All -> binding.chipGroup.check(binding.chipAll.id)
            FilterState.Completed -> binding.chipGroup.check(binding.chipCompleted.id)
            FilterState.Pending -> binding.chipGroup.check(binding.chipPending.id)
        }

        binding.chipAll.setOnClickListener {
            if (taskViewModel.filter.value != FilterState.All)
                taskViewModel.setFilter(FilterState.All)
        }
        binding.chipCompleted.setOnClickListener {
            if (taskViewModel.filter.value != FilterState.Completed)
                taskViewModel.setFilter(FilterState.Completed)
        }
        binding.chipPending.setOnClickListener {
            if (taskViewModel.filter.value != FilterState.Pending)
                taskViewModel.setFilter(FilterState.Pending)
        }
    }


    private fun applyPropertyInset() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


}