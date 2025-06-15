package com.testhar.taskapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.testhar.taskapp.databinding.ItemTaskBinding
import com.testhar.taskapp.domain.model.Task

class TaskAdapter(
    private val onDeleteClick: (Task) -> Unit,
    private val onItemClick: (Task) -> Unit,
    private val onStatusToggle: (Task, Boolean) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    inner class TaskViewHolder(val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.binding.apply {
            tvTitle.text = task.title
            tvDescription.text = task.description ?: ""
            // Prevent checkbox flicker by disabling listener before setting checked state
            checkbox.setOnCheckedChangeListener(null)
            if (checkbox.isChecked != task.isCompleted) {
                checkbox.isChecked = task.isCompleted
            }

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != task.isCompleted) {
                    onStatusToggle(task, isChecked)
                }
            }

            btnDelete.setOnClickListener { onDeleteClick(task) }
            root.setOnClickListener { onItemClick(task) }
        }
    }

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean = oldItem == newItem
    }
}