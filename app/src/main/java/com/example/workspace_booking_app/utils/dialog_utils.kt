package com.example.workspace_booking_app.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog

object DialogUtils {
    fun showMessage(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "OK",
        positiveAction: (() -> Unit)? = null,
        negativeText: String? = null,
        negativeAction: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                positiveAction?.invoke()
                dialog.dismiss()
            }

        if (negativeText != null) {
            builder.setNegativeButton(negativeText) { dialog, _ ->
                negativeAction?.invoke()
                dialog.dismiss()
            }
        }

        builder.show()
    }
}
