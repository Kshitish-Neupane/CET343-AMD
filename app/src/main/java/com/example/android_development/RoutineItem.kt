package com.example.android_development

data class RoutineItem(
    var title: String,
    var equipment: String = "",
    var reps: String = "",
    var sets: String = "",
    var checked: Boolean = false,
    var recommended: Boolean = false,
    var imageUri: String? = null,
    var isCompleted: Boolean = false
)
