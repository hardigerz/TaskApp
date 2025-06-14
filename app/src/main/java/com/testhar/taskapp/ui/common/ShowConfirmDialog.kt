package com.testhar.taskapp.ui.common

import android.content.Context
import androidx.appcompat.app.AlertDialog

fun showConfirmDialog(
    context: Context,
    title: String = "Confirm",
    message: String,
    onConfirmed: () -> Unit
) {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("Yes") { _, _ -> onConfirmed() }
        .setNegativeButton("No", null)
        .show()
}