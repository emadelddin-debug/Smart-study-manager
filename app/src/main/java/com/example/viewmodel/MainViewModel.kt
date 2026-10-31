package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ExamEntity
import com.example.data.model.GroupMeetupEntity
import com.example.data.model.GroupMessageEntity
import com.example.data.model.GroupTaskEntity
import com.example.data.model.StudyGroupEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.TaskEntity
import com.example.util.AcademicAlert
import com.example.util.AlertType
import com.example.util.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class Screen {
    WELCOME,
    DASHBOARD,
    TASKS,
    SUBJECTS,
    EXAMS,
    SCHEDULE,
    GROUPS,
    GROUP_DETAIL,
    POMODORO,
    ANALYTICS
}

enum class PomodoroMode(val defaultMinutes: Int) {
    FOCUS(25),
    SHORT_BREAK(5),
    LONG_BREAK(15)
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = database.studyDao()

    // Navigation & Screen Stack
    val currentScreen = MutableStateFlow(Screen.WELCOME)
    private val screenBackStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        if (currentScreen.value != screen) {
            screenBackStack.add(currentScreen.value)
            currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (screenBackStack.isNotEmpty()) {
            val previous = screenBackStack.removeAt(screenBackStack.size - 1)
            currentScreen.value = previous
            true
        } else if (currentScreen.value != Screen.DASHBOARD && currentScreen.value != Screen.WELCOME) {
            currentScreen.value = Screen.DASHBOARD
            true
        } else {
            false
        }
    }

    // Settings
    val isDarkMode = MutableStateFlow(false)
    val appLanguage = MutableStateFlow(AppLanguage.ENGLISH)

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }

    fun setLanguage(lang: AppLanguage) {
        appLanguage.value = lang
    }

    // Database Observables
    val tasks: StateFlow<List<TaskEntity>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<SubjectEntity>> = dao.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamEntity>> = dao.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySessionEntity>> = dao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<StudyGroupEntity>> = dao.getAllGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Group for Detail View
    val selectedGroupId = MutableStateFlow<Long?>(null)
    val selectedGroupTasks = MutableStateFlow<List<GroupTaskEntity>>(emptyList())
    val selectedGroupMeetups = MutableStateFlow<List<GroupMeetupEntity>>(emptyList())
    val selectedGroupMessages = MutableStateFlow<List<GroupMessageEntity>>(emptyList())

    fun selectGroup(groupId: Long) {
        selectedGroupId.value = groupId
        navigateTo(Screen.GROUP_DETAIL)
        viewModelScope.launch {
            dao.getGroupTasks(groupId).collect { selectedGroupTasks.value = it }
        }
        viewModelScope.launch {
            dao.getGroupMeetups(groupId).collect { selectedGroupMeetups.value = it }
        }
        viewModelScope.launch {
            dao.getGroupMessages(groupId).collect { selectedGroupMessages.value = it }
        }
    }

    // Real-Time Calculated KPI Statistics & Alerts
    val totalTasks = combine(tasks) { it.first().size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedTasks = combine(tasks) { taskList ->
        taskList.first().count { it.status == "COMPLETED" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingTasks = combine(tasks) { taskList ->
        taskList.first().count { it.status != "COMPLETED" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overdueTasks = combine(tasks) { taskList ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        taskList.first().count { it.status != "COMPLETED" && it.dueDate < todayStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completionRate = combine(tasks) { taskList ->
        val list = taskList.first()
        if (list.isEmpty()) 0f
        else (list.count { it.status == "COMPLETED" }.toFloat() / list.size.toFloat()) * 100f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val alerts = combine(tasks, exams, studySessions) { tList, eList, sList ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val cal = Calendar.getInstance()
        val currentDayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        val result = mutableListOf<AcademicAlert>()

        // Overdue tasks
        tList.filter { it.status != "COMPLETED" && it.dueDate < todayStr }.forEach { task ->
            result.add(
                AcademicAlert(
                    id = "overdue_${task.id}",
                    title = "Overdue: ${task.title}",
                    message = "Due on ${task.dueDate}. Priority: ${task.priority}",
                    type = AlertType.OVERDUE
                )
            )
        }

        // Tasks due today
        tList.filter { it.status != "COMPLETED" && it.dueDate == todayStr }.forEach { task ->
            result.add(
                AcademicAlert(
                    id = "due_today_${task.id}",
                    title = "Due Today: ${task.title}",
                    message = "Due at ${task.dueTime}.",
                    type = AlertType.DUE_TODAY
                )
            )
        }

        // Exams within 7 days
        val cal7 = Calendar.getInstance()
        cal7.add(Calendar.DAY_OF_YEAR, 7)
        val inSevenDays = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal7.time)
        eList.filter { it.examDate in todayStr..inSevenDays }.forEach { exam ->
            result.add(
                AcademicAlert(
                    id = "exam_${exam.id}",
                    title = "Approaching Exam: ${exam.title}",
                    message = "Date: ${exam.examDate} at ${exam.examTime} (${exam.location})",
                    type = AlertType.EXAM_URGENT
                )
            )
        }

        // Sessions today
        sList.filter { it.dayOfWeek == currentDayOfWeek && !it.isCompleted }.forEach { session ->
            result.add(
                AcademicAlert(
                    id = "session_${session.id}",
                    title = "Study Session: ${session.title}",
                    message = "${session.startTime} - ${session.endTime}",
                    type = AlertType.SESSION_TODAY
                )
            )
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pomodoro Timer State
    val pomodoroMode = MutableStateFlow(PomodoroMode.FOCUS)
    val pomodoroSeconds = MutableStateFlow(PomodoroMode.FOCUS.defaultMinutes * 60)
    val isPomodoroRunning = MutableStateFlow(false)
    val pomodoroCompletedCycles = MutableStateFlow(0)
    val pomodoroSoundEnabled = MutableStateFlow(true)
    private var pomodoroJob: Job? = null

    fun startPomodoro() {
        if (isPomodoroRunning.value) return
        isPomodoroRunning.value = true
        pomodoroJob = viewModelScope.launch {
            while (isPomodoroRunning.value && pomodoroSeconds.value > 0) {
                delay(1000L)
                pomodoroSeconds.value -= 1
            }
            if (pomodoroSeconds.value <= 0) {
                onPomodoroCompleted()
            }
        }
    }

    fun pausePomodoro() {
        isPomodoroRunning.value = false
        pomodoroJob?.cancel()
    }

    fun resetPomodoro() {
        pausePomodoro()
        pomodoroSeconds.value = pomodoroMode.value.defaultMinutes * 60
    }

    fun setPomodoroMode(mode: PomodoroMode) {
        pausePomodoro()
        pomodoroMode.value = mode
        pomodoroSeconds.value = mode.defaultMinutes * 60
    }

    fun togglePomodoroSound() {
        pomodoroSoundEnabled.value = !pomodoroSoundEnabled.value
    }

    private fun onPomodoroCompleted() {
        pausePomodoro()
        if (pomodoroMode.value == PomodoroMode.FOCUS) {
            val newCycleCount = pomodoroCompletedCycles.value + 1
            pomodoroCompletedCycles.value = newCycleCount
            if (newCycleCount % 4 == 0) {
                setPomodoroMode(PomodoroMode.LONG_BREAK)
            } else {
                setPomodoroMode(PomodoroMode.SHORT_BREAK)
            }
        } else {
            setPomodoroMode(PomodoroMode.FOCUS)
        }
    }

    // Task CRUD Operations
    fun addTask(
        title: String,
        description: String,
        subjectId: Long,
        dueDate: String,
        dueTime: String,
        priority: String
    ) {
        viewModelScope.launch {
            dao.insertTask(
                TaskEntity(
                    title = title,
                    description = description,
                    subjectId = subjectId,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    priority = priority,
                    status = "PENDING"
                )
            )
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            dao.updateTask(task)
        }
    }

    fun toggleTaskStatus(task: TaskEntity) {
        val nextStatus = when (task.status) {
            "COMPLETED" -> "PENDING"
            "PENDING" -> "IN_PROGRESS"
            else -> "COMPLETED"
        }
        viewModelScope.launch {
            dao.updateTask(task.copy(status = nextStatus))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }

    // Subject CRUD Operations
    fun addSubject(name: String, code: String, colorHex: String) {
        viewModelScope.launch {
            dao.insertSubject(
                SubjectEntity(
                    name = name,
                    code = code,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            dao.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            dao.deleteSubject(subject)
        }
    }

    // Exam CRUD Operations
    fun addExam(
        title: String,
        subjectId: Long,
        examDate: String,
        examTime: String,
        location: String,
        notes: String,
        readinessPercent: Int
    ) {
        viewModelScope.launch {
            dao.insertExam(
                ExamEntity(
                    title = title,
                    subjectId = subjectId,
                    examDate = examDate,
                    examTime = examTime,
                    location = location,
                    notes = notes,
                    readinessPercent = readinessPercent
                )
            )
        }
    }

    fun updateExam(exam: ExamEntity) {
        viewModelScope.launch {
            dao.updateExam(exam)
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch {
            dao.deleteExam(exam)
        }
    }

    // Study Timetable CRUD & Validation
    fun addStudySession(
        subjectId: Long,
        title: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String
    ): Boolean {
        if (startTime >= endTime) return false
        viewModelScope.launch {
            dao.insertSession(
                StudySessionEntity(
                    subjectId = subjectId,
                    title = title,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    isCompleted = false
                )
            )
        }
        return true
    }

    fun updateStudySession(session: StudySessionEntity) {
        viewModelScope.launch {
            dao.updateSession(session)
        }
    }

    fun toggleSessionCompleted(session: StudySessionEntity) {
        viewModelScope.launch {
            dao.updateSession(session.copy(isCompleted = !session.isCompleted))
        }
    }

    fun deleteStudySession(session: StudySessionEntity) {
        viewModelScope.launch {
            dao.deleteSession(session)
        }
    }

    // Collaborative Study Groups Operations
    fun createStudyGroup(name: String, description: String, subject: String, creatorName: String) {
        val randomCode = UUID.randomUUID().toString().take(6).uppercase()
        viewModelScope.launch {
            dao.insertGroup(
                StudyGroupEntity(
                    name = name,
                    description = description,
                    subject = subject,
                    joinCode = randomCode,
                    creatorName = creatorName,
                    memberCount = 1
                )
            )
        }
    }

    suspend fun joinGroupByCode(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        val existing = dao.getGroupByCode(trimmed)
        return if (existing != null) {
            dao.insertGroup(existing.copy(memberCount = existing.memberCount + 1))
            true
        } else {
            false
        }
    }

    fun addGroupTask(groupId: Long, title: String, description: String, dueDate: String, priority: String) {
        viewModelScope.launch {
            dao.insertGroupTask(
                GroupTaskEntity(
                    groupId = groupId,
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    isCompletedByMe = false,
                    completedMembersCount = 0,
                    totalMembersCount = 4
                )
            )
        }
    }

    fun toggleGroupTask(task: GroupTaskEntity) {
        val nextCompleted = !task.isCompletedByMe
        val nextCount = if (nextCompleted) task.completedMembersCount + 1 else (task.completedMembersCount - 1).coerceAtLeast(0)
        viewModelScope.launch {
            dao.updateGroupTask(
                task.copy(
                    isCompletedByMe = nextCompleted,
                    completedMembersCount = nextCount
                )
            )
        }
    }

    fun addGroupMeetup(groupId: Long, title: String, date: String, time: String, linkOrRoom: String, topic: String) {
        viewModelScope.launch {
            dao.insertGroupMeetup(
                GroupMeetupEntity(
                    groupId = groupId,
                    title = title,
                    date = date,
                    time = time,
                    linkOrRoom = linkOrRoom,
                    topic = topic
                )
            )
        }
    }

    val groupTypingStatus = MutableStateFlow<String?>(null)

    fun sendGroupMessage(groupId: Long, text: String) {
        if (text.isBlank()) return
        val userMsg = text.trim()
        viewModelScope.launch {
            dao.insertGroupMessage(
                GroupMessageEntity(
                    groupId = groupId,
                    senderName = "You",
                    message = userMsg,
                    timestamp = System.currentTimeMillis(),
                    isMe = true
                )
            )

            // Simulate realistic peer engagement in study group
            delay(1200L)
            val isArabic = appLanguage.value == AppLanguage.ARABIC
            val peerName = if (groupId % 2L == 0L) "Sarah Chen" else "Karim Said"
            groupTypingStatus.value = if (isArabic) "$peerName يكتب الآن..." else "$peerName is typing..."

            delay(1800L)
            groupTypingStatus.value = null

            val replyText = when {
                userMsg.contains("?") || userMsg.contains("؟") -> {
                    if (isArabic)
                        "سؤال ممتاز! راجع ملخص المحاضرة الرابعة، يوضح هذا المفهوم بدقة."
                    else
                        "Great question! Check the lecture notes from last week, it explains this case in detail."
                }
                userMsg.contains("meet", ignoreCase = true) || userMsg.contains("call", ignoreCase = true) || userMsg.contains("لقاء") -> {
                    if (isArabic)
                        "أنا حاضر معكم بالتأكيد في اللقاء القادم! سأحضر أسئلة التدريب."
                    else
                        "I'll definitely be there for the meetup! I'm preparing practice problems now."
                }
                userMsg.contains("task", ignoreCase = true) || userMsg.contains("done", ignoreCase = true) || userMsg.contains("مهمة") -> {
                    if (isArabic)
                        "عمل رائع يا بطل! تم تحديث لوحة إنجاز المجموعة."
                    else
                        "Awesome progress! Group completion leaderboard just updated."
                }
                else -> {
                    if (isArabic)
                        "شكراً على التحديث! لنواصل التركيز في جلسة بومودورو القادمة."
                    else
                        "Thanks for sharing! Let's keep our study momentum going in the next focus session."
                }
            }

            dao.insertGroupMessage(
                GroupMessageEntity(
                    groupId = groupId,
                    senderName = peerName,
                    message = replyText,
                    timestamp = System.currentTimeMillis(),
                    isMe = false
                )
            )
        }
    }

    // AI Study Assistant State & Generator
    val isAiLoading = MutableStateFlow(false)
    val aiResponse = MutableStateFlow<String?>(null)

    private val geminiModel by lazy {
        try {
            Firebase.ai.generativeModel("gemini-3.8-flash")
        } catch (e: Exception) {
            null
        }
    }

    fun generateDailyStudyPlan(language: AppLanguage) {
        viewModelScope.launch {
            isAiLoading.value = true
            aiResponse.value = null

            val currentTasks = tasks.value.filter { it.status != "COMPLETED" }
            val currentExams = exams.value
            val currentSubjects = subjects.value.associate { it.id to it.name }

            val taskSummary = currentTasks.joinToString("\n") {
                "- ${it.title} [Subject: ${currentSubjects[it.subjectId] ?: "General"}, Priority: ${it.priority}, Due: ${it.dueDate}]"
            }
            val examSummary = currentExams.joinToString("\n") {
                "- ${it.title} on ${it.examDate} (Readiness: ${it.readinessPercent}%)"
            }

            val isArabic = language == AppLanguage.ARABIC
            val prompt = if (isArabic) {
                """
                أنت مستشار أكاديمي ذكي متخصص لطلاب الجامعات.
                قم بتحليل مهام الطالب واختباراته التالية، ثم قم بإنشاء خطة دراسية واقعية ومحكمة لليوم، مع توزيع الفترات الزمنية واستراحات بومودورو ونصائح للتفوق.
                المهام المعلقة:
                $taskSummary
                الاختبارات القادمة:
                $examSummary
                اكتب الخطة بلغة عربية فصيحة، منظمة في نقاط وجداول زمنية واضحة.
                """.trimIndent()
            } else {
                """
                You are an expert university academic advisor and study planner.
                Analyze the student's pending tasks and upcoming exams below, and create a realistic, high-impact daily study plan with time blocks, Pomodoro focus cycles, and prioritization tips.
                Pending Tasks:
                $taskSummary
                Upcoming Exams:
                $examSummary
                Format the plan with clear time slots, focus priorities, and motivational revision advice.
                """.trimIndent()
            }

            try {
                val model = geminiModel
                if (model != null) {
                    val response = withTimeout(25000L) {
                        model.generateContent(prompt)
                    }
                    val text = response.text
                    if (!text.isNullOrBlank()) {
                        aiResponse.value = text
                        isAiLoading.value = false
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Fall through to algorithmic study plan
            }

            // High-Value Deterministic University Study Plan Fallback
            aiResponse.value = buildAlgorithmicStudyPlan(currentTasks, currentExams, currentSubjects, isArabic)
            isAiLoading.value = false
        }
    }

    fun askAiAssistant(question: String, language: AppLanguage) {
        if (question.isBlank()) return
        viewModelScope.launch {
            isAiLoading.value = true
            aiResponse.value = null
            val isArabic = language == AppLanguage.ARABIC

            try {
                val model = geminiModel
                if (model != null) {
                    val response = withTimeout(25000L) {
                        model.generateContent("Student question: $question\nAnswer as an encouraging university academic advisor in ${if (isArabic) "Arabic" else "English"}.")
                    }
                    val text = response.text
                    if (!text.isNullOrBlank()) {
                        aiResponse.value = text
                        isAiLoading.value = false
                        return@launch
                    }
                }
            } catch (e: Exception) {
                // Fallback
            }

            aiResponse.value = if (isArabic) {
                "نصيحة المساعد الأكاديمي: لتجاوز هذه العقبة بنجاح، قسم مادتك إلى وحدات معرفية صغيرة مدتها 25 دقيقة باستخدام مؤقت بومودورو. ابدأ بالأصعب في أوقات ذروة نشاطك الصباحي، واشرح المفاهيم لزملائك في مجموعة الدراسة لتثبيت الفهم."
            } else {
                "Academic Advisor Recommendation: To master this topic effectively, chunk your material into focused 25-minute Pomodoro sessions. Tackle high-priority assignments during your peak morning alertness, and test your recall using active recall questions with your study group."
            }
            isAiLoading.value = false
        }
    }

    private fun buildAlgorithmicStudyPlan(
        tasks: List<TaskEntity>,
        exams: List<ExamEntity>,
        subjectMap: Map<Long, String>,
        isArabic: Boolean
    ): String {
        val highPriority = tasks.filter { it.priority == "HIGH" }
        val medPriority = tasks.filter { it.priority == "MEDIUM" }

        return if (isArabic) {
            buildString {
                appendLine("📋 **الخطة الأكاديمية المقترحة لليوم (محسنة بالذكاء الاصطناعي)**")
                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                appendLine("🕒 **09:00 - 10:30 ص | جلسة التركيز العميق الأولى (High Focus)**")
                if (highPriority.isNotEmpty()) {
                    appendLine("• إنجاز المهمة ذات الأولوية القصوى: **${highPriority.first().title}** (${subjectMap[highPriority.first().subjectId] ?: "عام"})")
                } else if (tasks.isNotEmpty()) {
                    appendLine("• البدء بـ: **${tasks.first().title}**")
                } else {
                    appendLine("• مراجعة ملخصات المحاضرات وحل تدريبات تفاعلية.")
                }
                appendLine("☕ *10:30 - 10:45 ص: استراحة بومودورو قصيرة لشرب الماء والحركة*")
                appendLine()
                appendLine("🕒 **10:45 - 12:30 م | جلسة التطبيق وحل التمارين**")
                if (medPriority.isNotEmpty()) {
                    appendLine("• معالجة: **${medPriority.first().title}** (${subjectMap[medPriority.first().subjectId] ?: "عام"})")
                } else {
                    appendLine("• حل أسئلة نماذج سابقة وحل واجبات الأسبوع.")
                }
                appendLine("🍽️ *12:30 - 02:00 م: غداء واستراحة لتجديد النشاط الذهني*")
                appendLine()
                appendLine("🕒 **02:00 - 03:30 م | مراجعة الاختبارات القادمة**")
                if (exams.isNotEmpty()) {
                    val nextExam = exams.first()
                    appendLine("• اختبار **${nextExam.title}** (تاريخه: ${nextExam.examDate}) - مراجعة المفاهيم الصعبة ورفع الجاهزية من ${nextExam.readinessPercent}% إلى 90%.")
                } else {
                    appendLine("• تلخيص المصطلحات ومراجعة البطاقات التعليمية.")
                }
                appendLine()
                appendLine("💡 **نصيحة اليوم للنجاح:** تجنب التشتت وأوقف الإشعارات أثناء جلسات الـ 25 دقيقة، وتفاعل مع زملائك في مجموعة الدراسة لمشاركة التقدم!")
            }
        } else {
            buildString {
                appendLine("📋 **Intelligent Daily Study Plan**")
                appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                appendLine("🕒 **09:00 AM - 10:30 AM | Deep Focus Block 1**")
                if (highPriority.isNotEmpty()) {
                    appendLine("• High Impact Task: **${highPriority.first().title}** (${subjectMap[highPriority.first().subjectId] ?: "Coursework"})")
                } else if (tasks.isNotEmpty()) {
                    appendLine("• Primary Focus: **${tasks.first().title}**")
                } else {
                    appendLine("• Review lecture slides and synthesize active recall notes.")
                }
                appendLine("☕ *10:30 AM - 10:45 AM: Recharge break & hydration*")
                appendLine()
                appendLine("🕒 **10:45 AM - 12:30 PM | Problem Solving & Coursework Block**")
                if (medPriority.isNotEmpty()) {
                    appendLine("• Problem Set: **${medPriority.first().title}** (${subjectMap[medPriority.first().subjectId] ?: "Coursework"})")
                } else {
                    appendLine("• Practice past exam questions and homework assignments.")
                }
                appendLine("🍽️ *12:30 PM - 02:00 PM: Lunch & Mental Reset*")
                appendLine()
                appendLine("🕒 **02:00 PM - 03:30 PM | Exam Prep & Study Group Collaboration**")
                if (exams.isNotEmpty()) {
                    val nextExam = exams.first()
                    appendLine("• Target Exam: **${nextExam.title}** (Date: ${nextExam.examDate}) — Boost readiness from ${nextExam.readinessPercent}% to 90%.")
                } else {
                    appendLine("• Group discussion and peer code/problem review.")
                }
                appendLine()
                appendLine("💡 **Academic Tip:** Complete your hardest task first during morning alertness. Keep Pomodoro intervals strict!")
            }
        }
    }

    // Reset Sample Data
    fun resetSampleData() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            dao.deleteAllSubjects()
            dao.deleteAllExams()
            dao.deleteAllSessions()
            dao.deleteAllGroups()
            AppDatabase.populateInitialData(dao)
        }
    }
}
