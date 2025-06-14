package com.testhar.taskapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.testhar.taskapp.databinding.ActivityMainBinding
import com.testhar.taskapp.ui.activity.AddEditTaskActivity
import com.testhar.taskapp.ui.adapter.TaskAdapter
import com.testhar.taskapp.ui.common.showConfirmDialog
import com.testhar.taskapp.ui.common.showSnackbar
import com.testhar.taskapp.ui.viewmodel.TaskViewModel
import com.testhar.taskapp.utils.FilterState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val contentRoot by lazy { binding.root }
    private val taskViewModel: TaskViewModel by viewModels()

    private val adapter = TaskAdapter(
        onDelete = {task ->
            showConfirmDialog(
                context = this,
                title = "Delete Task",
                message = "Are you sure want to delete this task"
            ){
                taskViewModel.deleteTask(task)
                showSnackbar(binding.root,"Task Deleted")
            }
        },
        onToggle = {
            taskViewModel.updateTask(it.copy(isCompleted = !it.isCompleted))
        },
        onEdit = { task ->
            val intent = Intent(this, AddEditTaskActivity::class.java)
            intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID, task.id)
            startActivity(intent)
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(contentRoot)
        applyPropertyInset()
        binding.recyclerView.adapter = adapter
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddEditTaskActivity::class.java))

        }

        setupFilterListeners()



        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskViewModel.filteredTasks.collect { tasks ->
                    val filtered = when (taskViewModel.filter.value) {
                        FilterState.All -> tasks
                        FilterState.Completed -> tasks.filter { it.isCompleted }
                        FilterState.Pending -> tasks.filter { !it.isCompleted }
                    }
                    adapter.submitList(filtered)
                }
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

    private fun setupFilterListeners() {
        binding.btnFilterAll.setOnClickListener {
            taskViewModel.setFilter(FilterState.All)
        }
        binding.btnFilterCompleted.setOnClickListener {
            taskViewModel.setFilter(FilterState.Completed)
        }
        binding.btnFilterPending.setOnClickListener {
            taskViewModel.setFilter(FilterState.Pending)
        }
    }


}