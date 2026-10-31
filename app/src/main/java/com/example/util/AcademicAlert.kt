package com.example.util

enum class AlertType {
    OVERDUE,
    DUE_TODAY,
    EXAM_URGENT,
    SESSION_TODAY
}

data class AcademicAlert(
    val id: String,
    val title: String,
    val message: String,
    val type: AlertType,
    val timestamp: Long = System.currentTimeMillis()
)
