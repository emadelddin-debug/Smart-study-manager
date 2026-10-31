package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val subjectId: Long = 0,
    val dueDate: String, // YYYY-MM-DD
    val dueTime: String = "23:59",
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val status: String = "PENDING", // PENDING, IN_PROGRESS, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val colorHex: String
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectId: Long = 0,
    val examDate: String, // YYYY-MM-DD
    val examTime: String = "09:00",
    val location: String = "Main Hall",
    val notes: String = "",
    val readinessPercent: Int = 50
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long = 0,
    val title: String,
    val dayOfWeek: Int, // 1 (Mon) - 7 (Sun)
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val isCompleted: Boolean = false
)

@Entity(tableName = "study_groups")
data class StudyGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val subject: String,
    val joinCode: String,
    val creatorName: String,
    val memberCount: Int = 1
)

@Entity(tableName = "group_tasks")
data class GroupTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val title: String,
    val description: String = "",
    val dueDate: String,
    val priority: String = "MEDIUM",
    val isCompletedByMe: Boolean = false,
    val completedMembersCount: Int = 0,
    val totalMembersCount: Int = 4
)

@Entity(tableName = "group_meetups")
data class GroupMeetupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val title: String,
    val date: String,
    val time: String,
    val linkOrRoom: String,
    val topic: String = ""
)

@Entity(tableName = "group_messages")
data class GroupMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isMe: Boolean = false
)
