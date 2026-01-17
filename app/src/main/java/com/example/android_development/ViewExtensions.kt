package com.example.android_development

import android.view.MotionEvent
import android.widget.EditText

fun EditText.setOnDrawableClickListener(onClick: () -> Unit) {
    setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.rawX >= (right - compoundDrawables[2].bounds.width())) {
                onClick()
                return@setOnTouchListener true
            }
        }
        false
    }
}
