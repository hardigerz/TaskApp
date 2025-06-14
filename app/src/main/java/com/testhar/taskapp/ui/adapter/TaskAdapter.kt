package com.testhar.taskapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.testhar.taskapp.databinding.ItemTaskBinding
import com.testhar.taskapp.domain.model.Task

class TaskAdapter(
    private val onDelete: (Task) -> Unit,
    private val onToggle: (Task) -> Unit,
    val onEdit: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.tvDescription.text = task.description ?: ""
            binding.checkbox.isChecked = task.isCompleted

            binding.checkbox.setOnClickListener {
                onToggle(task)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(task)
            }
            binding.root.setOnClickListener {
                onEdit(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
