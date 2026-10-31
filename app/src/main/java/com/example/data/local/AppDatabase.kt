package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ExamEntity
import com.example.data.model.GroupMeetupEntity
import com.example.data.model.GroupMessageEntity
import com.example.data.model.GroupTaskEntity
import com.example.data.model.StudyGroupEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        TaskEntity::class,
        SubjectEntity::class,
        ExamEntity::class,
        StudySessionEntity::class,
        StudyGroupEntity::class,
        GroupTaskEntity::class,
        GroupMeetupEntity::class,
        GroupMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_study_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.studyDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: StudyDao) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()

            val today = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrow = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 3)
            val inFourDays = dateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 6)
            val inTenDays = dateFormat.format(calendar.time)

            // 1. Initial Subjects
            val cs = SubjectEntity(id = 1, name = "Computer Science", code = "CS 301", colorHex = "#8B5CF6")
            val math = SubjectEntity(id = 2, name = "Applied Mathematics", code = "MATH 240", colorHex = "#0EA5E9")
            val bio = SubjectEntity(id = 3, name = "Cellular Biology", code = "BIO 110", colorHex = "#10B981")
            val ai = SubjectEntity(id = 4, name = "Artificial Intelligence", code = "AI 450", colorHex = "#F59E0B")
            dao.insertSubjects(listOf(cs, math, bio, ai))

            // 2. Initial Tasks
            dao.insertTasks(
                listOf(
                    TaskEntity(
                        id = 1,
                        title = "Implement Dijkstra Graph Algorithm",
                        description = "Complete the shortest path graph routing problem set in Kotlin.",
                        subjectId = 1,
                        dueDate = tomorrow,
                        dueTime = "23:59",
                        priority = "HIGH",
                        status = "IN_PROGRESS"
                    ),
                    TaskEntity(
                        id = 2,
                        title = "Calculus Problem Set 6: Differential Equations",
                        description = "Solve problems 14 through 28 on second-order linear differentials.",
                        subjectId = 2,
                        dueDate = today,
                        dueTime = "18:00",
                        priority = "MEDIUM",
                        status = "PENDING"
                    ),
                    TaskEntity(
                        id = 3,
                        title = "Cellular Respiration Lab Report",
                        description = "Synthesize spectrophotometer readings into discussion graphs.",
                        subjectId = 3,
                        dueDate = inFourDays,
                        dueTime = "14:00",
                        priority = "LOW",
                        status = "PENDING"
                    ),
                    TaskEntity(
                        id = 4,
                        title = "AI Model Evaluation Paper Review",
                        description = "Read attention mechanism architectures and draft 2-page summary.",
                        subjectId = 4,
                        dueDate = today,
                        dueTime = "12:00",
                        priority = "HIGH",
                        status = "COMPLETED"
                    ),
                    TaskEntity(
                        id = 5,
                        title = "Binary Tree Inversion & Unit Tests",
                        description = "Pass all recursive and iterative edge cases in JUnit.",
                        subjectId = 1,
                        dueDate = inTenDays,
                        dueTime = "20:00",
                        priority = "MEDIUM",
                        status = "PENDING"
                    )
                )
            )

            // 3. Initial Exams
            dao.insertExams(
                listOf(
                    ExamEntity(
                        id = 1,
                        title = "Algorithms & Data Structures Midterm",
                        subjectId = 1,
                        examDate = inFourDays,
                        examTime = "10:00",
                        location = "Science Hall 204",
                        notes = "Covers Divide & Conquer, Dynamic Programming, and Graph Traversals.",
                        readinessPercent = 75
                    ),
                    ExamEntity(
                        id = 2,
                        title = "Multivariable Calculus Final Exam",
                        subjectId = 2,
                        examDate = inTenDays,
                        examTime = "14:30",
                        location = "Auditorium B",
                        notes = "Formula sheet allowed. Focus on Green's and Stokes' theorems.",
                        readinessPercent = 60
                    )
                )
            )

            // 4. Initial Study Sessions (Timetable)
            dao.insertSessions(
                listOf(
                    StudySessionEntity(
                        id = 1,
                        subjectId = 1,
                        title = "Graph Algorithms Coding & Debugging",
                        dayOfWeek = 1, // Mon
                        startTime = "10:00",
                        endTime = "12:00",
                        isCompleted = true
                    ),
                    StudySessionEntity(
                        id = 2,
                        subjectId = 2,
                        title = "Calculus Differential Equations Practice",
                        dayOfWeek = 2, // Tue
                        startTime = "14:00",
                        endTime = "16:00",
                        isCompleted = false
                    ),
                    StudySessionEntity(
                        id = 3,
                        subjectId = 4,
                        title = "Neural Networks Hyperparameter Tuning",
                        dayOfWeek = 4, // Thu
                        startTime = "09:00",
                        endTime = "11:30",
                        isCompleted = false
                    ),
                    StudySessionEntity(
                        id = 4,
                        subjectId = 3,
                        title = "Biology Lab Report Analysis",
                        dayOfWeek = 5, // Fri
                        startTime = "15:00",
                        endTime = "17:00",
                        isCompleted = false
                    )
                )
            )

            // 5. Initial Collaborative Study Groups
            val group1 = StudyGroupEntity(
                id = 1,
                name = "Advanced Algorithms Masters",
                description = "University CS study circle for midterm preparation and LeetCode graph challenges.",
                subject = "Computer Science",
                joinCode = "CS301X",
                creatorName = "Karim Said",
                memberCount = 5
            )
            val group2 = StudyGroupEntity(
                id = 2,
                name = "Calculus & Linear Algebra Squad",
                description = "Weekly problem set solving and exam countdown review group.",
                subject = "Applied Mathematics",
                joinCode = "MTH240",
                creatorName = "Sarah Chen",
                memberCount = 4
            )
            dao.insertGroups(listOf(group1, group2))

            // 6. Group Tasks
            dao.insertGroupTasks(
                listOf(
                    GroupTaskEntity(
                        id = 1,
                        groupId = 1,
                        title = "Solve Problem Set 4: Bellman-Ford vs Floyd-Warshall",
                        description = "Review time complexity trade-offs and code benchmark.",
                        dueDate = inFourDays,
                        priority = "HIGH",
                        isCompletedByMe = true,
                        completedMembersCount = 3,
                        totalMembersCount = 5
                    ),
                    GroupTaskEntity(
                        id = 2,
                        groupId = 1,
                        title = "Review Lecture 12 Graph Decomposition Slides",
                        description = "Annotate slides with questions for professor's office hours.",
                        dueDate = tomorrow,
                        priority = "MEDIUM",
                        isCompletedByMe = false,
                        completedMembersCount = 2,
                        totalMembersCount = 5
                    ),
                    GroupTaskEntity(
                        id = 3,
                        groupId = 2,
                        title = "Eigenvalues & Eigenvectors Practice Sheet",
                        description = "Verify characteristic polynomial matrix roots.",
                        dueDate = inTenDays,
                        priority = "HIGH",
                        isCompletedByMe = false,
                        completedMembersCount = 1,
                        totalMembersCount = 4
                    )
                )
            )

            // 7. Group Meetups
            dao.insertGroupMeetups(
                listOf(
                    GroupMeetupEntity(
                        id = 1,
                        groupId = 1,
                        title = "Midterm Mock Exam & Question Breakdown",
                        date = inFourDays,
                        time = "18:00",
                        linkOrRoom = "https://meet.google.com/cs-algo-prep",
                        topic = "2-hour timed mock exam followed by peer code review."
                    ),
                    GroupMeetupEntity(
                        id = 2,
                        groupId = 2,
                        title = "Calculus Finals Review Marathon",
                        date = inTenDays,
                        time = "16:00",
                        linkOrRoom = "Library 3rd Floor Seminar Room",
                        topic = "Reviewing past 3 years of multivariable final exams."
                    )
                )
            )

            // 8. Group Messages (Live Chat)
            dao.insertGroupMessages(
                listOf(
                    GroupMessageEntity(
                        id = 1,
                        groupId = 1,
                        senderName = "Karim Said",
                        message = "Hey everyone! I uploaded the graph reduction diagram to our shared drive.",
                        timestamp = System.currentTimeMillis() - 7200000,
                        isMe = false
                    ),
                    GroupMessageEntity(
                        id = 2,
                        groupId = 1,
                        senderName = "Amina Noor",
                        message = "Awesome Karim, that makes topological sorting much clearer!",
                        timestamp = System.currentTimeMillis() - 3600000,
                        isMe = false
                    ),
                    GroupMessageEntity(
                        id = 3,
                        groupId = 1,
                        senderName = "You",
                        message = "I just finished the Dijkstra implementation, will share my unit test suite before tonight's call.",
                        timestamp = System.currentTimeMillis() - 1200000,
                        isMe = true
                    )
                )
            )
        }
    }
}
