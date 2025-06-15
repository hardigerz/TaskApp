
package com.testhar.taskapp.utils
import com.testhar.taskapp.R

import android.content.Context

enum class TaskStatus(val stringResId: Int, val isCompleted: Boolean) {
    All(R.string.status_all,false),
    Pending(R.string.status_pending, false),
    Completed(R.string.status_completed, true);

    companion object {
        fun fromCompleted(completed: Boolean): TaskStatus =
            if (completed) Completed else Pending

        fun fromLocalizedName(context: Context, name: String): TaskStatus {
            return entries.firstOrNull {
                context.getString(it.stringResId).equals(name, ignoreCase = true)
            } ?: Pending
        }
    }
}