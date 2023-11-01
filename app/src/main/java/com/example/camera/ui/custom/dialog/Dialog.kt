package com.example.camera.ui.custom.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Window
import android.view.WindowInsets
import android.widget.LinearLayout
import com.example.camera.R
import com.example.camera.ui.model.DimensionSize

fun createDialog(context: Context, layoutId: Int): Dialog {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.window!!.decorView.setBackgroundResource(R.drawable.background_area_selection)
    dialog.window!!.decorView.setPadding(0, 0, 0, 0)
    dialog.window!!.attributes.windowAnimations = R.style.DialogSlide1

    if (context is Activity) {
        dialog.window!!.attributes.height =
            LinearLayout.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes.width =
            ((getWidth(context) - context.resources.getDimension(R.dimen.size_40)).toInt())
    }
    dialog.setContentView(layoutId)
    dialog.show()
    return dialog
}


@Suppress("DEPRECATION")
fun getWidth(activity: Activity): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val insets: Insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

@Suppress("DEPRECATION")
fun getDisplaySize(activity: Activity): DimensionSize {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        DimensionSize(windowMetrics.bounds.width(), windowMetrics.bounds.height())
    } else {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        DimensionSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
}

fun getDP(dip: Int, context: Context): Float {
    return (TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dip.toFloat(),
        context.resources.displayMetrics
    ))
}

