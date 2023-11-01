package com.example.camera.ui.custom.dialog

import android.app.Dialog
import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.example.camera.R

class AskDialog {

    fun askPermissionDialog(
        context: Context,
        message: String,
        onSelection: (isConfirm: Boolean, isDismissed: Boolean) -> Unit
    ) {
        setup(
            context,
            message,
            context.getString(R.string.grant),
            context.getString(R.string.cancel),
            onSelection
        )
    }

    private fun setup(
        context: Context,
        message: String,
        yesButton: String,
        cancelButton: String,
        onSelection: (isConfirm: Boolean, isDismissed: Boolean) -> Unit
    ): Dialog {
        val dialog = createDialog(context, R.layout.dialog_ask)
        dialog.findViewById<AppCompatTextView>(R.id.confirmBtn).text = yesButton
        dialog.findViewById<AppCompatTextView>(R.id.cancelConfirm).text = cancelButton
        dialog.findViewById<AppCompatTextView>(R.id.textView6).text = message
        var isDismissed = false
        dialog.findViewById<AppCompatTextView>(R.id.cancelConfirm).setOnClickListener {
            dialog.dismiss()
            isDismissed = true
            onSelection.invoke(false, false)
        }
        dialog.findViewById<AppCompatTextView>(R.id.confirmBtn).setOnClickListener {
            dialog.dismiss()
            isDismissed = true
            onSelection.invoke(true, false)
        }
        dialog.findViewById<AppCompatImageView>(R.id.closeDialog).setOnClickListener {
            dialog.dismiss()
            isDismissed = true
            onSelection.invoke(false, true)
        }


        dialog.setOnDismissListener {
//            Log.e("Dialog State", "dissmiss")
            if (!isDismissed)
                onSelection.invoke(false, true)
        }
        return dialog
    }
}