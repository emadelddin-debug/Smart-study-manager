# Smart Study Manager — Implementation Plan

Smart Study Manager is a comprehensive academic productivity and collaborative study platform for university students. It combines intelligent task and exam management, dynamic timetable scheduling, a focus Pomodoro timer, AI-driven study planning, and real-time collaborative study groups with shared tasks, member progress tracking, virtual meetups, and group chat.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The application is architected natively for Android using Jetpack Compose and Material 3, fulfilling all SaaS productivity requirements with a high-fidelity mobile experience, bilingual Arabic/English RTL support, and collaborative study groups.

- **Platform Architecture**: Native Android application using Jetpack Compose, Material Design 3, Coroutine Flows, and state-driven navigation with adaptive handheld and tablet layouts.
- **Data Persistence Strategy (Hybrid Architecture)**:
  - **Local Persistence (Room Database)**: Personal tasks, subjects, exams, study schedules, Pomodoro logs, and user preferences are stored locally in Room for instant, offline-first reliability.
  - **Cloud Collaboration (Firebase Firestore)**: Collaborative study groups, shared group tasks, member progress synchronization, virtual meetup scheduling, and real-time group chat.
- **AI Study Assistant**: Firebase AI Logic powered by Gemini 3.8 Flash for intelligent daily schedule synthesis, priority recommendations, and bilingual academic coaching, with a built-in algorithmic fallback when offline.
- **Bilingual & Layout Mirroring**: Full in-app language switching between English (LTR) and Arabic (RTL) across all screens, charts, badges, and input forms.

---

## 1. Overview & Core Concept

### What It Does
Smart Study Manager eliminates university student disorganization by uniting six core academic pillars into one cohesive app:
1. **Academic Dashboard & Analytics**: Real-time KPI cards (Total, Completed, Pending, Overdue, Completion Rate), circular progress meters, weekly productivity charts, upcoming deadline timeline, and today's schedule.
2. **Task & Subject Management**: Full CRUD operations, subject color tagging, priority filters (Low, Medium, High), search, sorting, and overdue alert badges.
3. **Exams & Deadlines Tracker**: Exam countdown timers, days remaining, venue/notes tracking, and preparation readiness meters.
4. **Study Schedule & Timetable Planner**: Time-blocked study sessions, calendar views, start/end time validation, and daily schedule generation.
5. **Pomodoro Focus Timer**: 25m focus / 5m short break / 15m long break cycles with session counters, visual progress rings, and audio/vibration feedback.
6. **Collaborative Study Groups & Chat**: Study group creation and joining via code, shared group tasks, real-time member completion percentage tracking, group virtual meetup scheduling, and live group chat.
7. **Gemini AI Academic Assistant**: Automated schedule optimizer, task prioritizer, and bilingual academic advisor (English & Arabic).

### Target Audience & Persona
University students, graduate researchers, and academic study groups seeking structured time management, stress reduction before exams, and peer accountability.

---

## 2. User Experience & Visual Design

### Key User Flows
```
┌─────────────────────────────────────────────────────────────┐
│                       Welcome Screen                        │
│   (Hero illustration, 3 value pillars, "Get Started")       │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   Main Navigation Shell                     │
│  [Dashboard] [Tasks] [Exams] [Schedule] [Groups] [Pomodoro] │
└──────┬──────────┬────────┬───────┬──────────┬───────────┬───┘
       │          │        │       │          │           │
       ▼          ▼        ▼       ▼          ▼           ▼
  [Dashboard]  [Tasks &  [Exams  [Timetable [Study     [Pomodoro
   KPI Cards   Subjects] Tracker  Planner]   Groups &    Timer &
   & Timeline   Filter &  Countdown Validated Chat]      Cycles]
                CRUD      Meters   Sessions
```

1. **Onboarding / Welcome**: Engaging splash and hero banner, value proposition pills ("Smart Planning", "Task Management", "Productivity Tracking"), language selector (EN/AR), and instant transition to the Dashboard.
2. **Dashboard Overview**: Immediate visibility of academic health, circular completion ring, urgent deadlines, and a quick floating action button for fast task entry.
3. **Task & Subject Workflow**: Filter by subject pills, switch status tabs (Pending, In Progress, Completed), search query bar, and bottom sheet dialogs for creating or editing tasks.
4. **Exams & Countdown**: Visual card deck showing urgent exams at the top with badge highlights, animated countdown clocks, and preparation progress sliders.
5. **Collaborative Study Group Workflow**:
   - Create or join group via unique join code.
   - Group Hub: View members, member progress leaderboard, shared group milestones.
   - Shared Tasks: Assign tasks to the group with individual completion status.
   - Virtual Meetups & Sessions: Schedule group study calls/sessions with topic and meeting link.
   - Group Live Chat: Instant message stream with sender avatars, timestamps, and study notes.
6. **Focus Pomodoro Session**: Clean, distraction-free circular countdown with smooth animated stroke, session state badges, and break intervals.
7. **AI Study Assistant Drawer / Modal**: One-tap "Generate My Daily Study Plan" analyzing current uncompleted tasks and approaching exams.

### Visual Identity & Theme
- **Primary Navy**: `#0F172A` (Deep Slate Navy)
- **Accent Purple**: `#8B5CF6` (Vibrant Purple)
- **Soft Lavender**: `#DDD6FE` (Lavender highlight and pill containers)
- **Light Surface / Background**: `#F8FAFC` (Crisp clean background) & `#FFFFFF` (Elevated cards)
- **Dark Surface / Background**: `#0B0F19` (OLED Dark background) & `#1E293B` (Dark card containers)
- **Status Accents**:
  - Emerald Green (`#10B981`): Completed / On Track
  - Amber Orange (`#F59E0B`): Approaching Deadline / In Progress
  - Crimson Red (`#EF4444`): Overdue / High Priority Exam

### Typography & Hierarchy
- **Display Headlines**: Bold contemporary sans-serif with high contrast and tight tracking.
- **Body & Micro-copy**: Crisp readable text at standard Android `sp` sizing with full Unicode Arabic typography support (Noto Sans / Cairo compatible font curves).
- **Numbers & KPI Stats**: Prominent tabular figures for time counters, percentages, and countdown days.

### Accessibility & Internationalization (LTR & RTL)
- Dual language dictionary (English & Arabic) covering 100% of labels, buttons, headers, and error messages.
- Automatic layout direction switching: `CompositionLocalProvider(LocalLayoutDirection provides layoutDirection)`.
- Full RTL mirroring for icons, navigation bars, cards, sliders, and form layouts.
- Minimum 48dp touch targets and high-contrast color pairings conforming to WCAG AA.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Hybrid Persistence (Room Database + Firestore Collaboration)
- **Approach**: All individual university data (personal assignments, courses, exam schedules, personal focus records) resides in a local Room database with Kotlin Flow streams. Collaborative study group entities (groups, shared tasks, member attendance/progress, and group chat) synchronize via Firebase Firestore.
- **Why**: Students need guaranteed, instant access to their personal homework and timetable even in classrooms without Wi-Fi, while study group interactions seamlessly synchronize with peers in the cloud.
- **Alternative Considered**: Pure cloud or pure local. Pure local prevents multi-member group chat and shared task progress; pure cloud would block students from viewing their homework offline.

### Decision 2: Smart AI Study Assistant with Deterministic Fallback
- **Approach**: Firebase AI Logic connects to `gemini-3.8-flash` to evaluate the student's task deadlines, exam weights, and user study habits to create tailored daily schedules and revision tips in English or Arabic. When offline or without active network keys, the app automatically switches to an internal heuristic scheduling algorithm that analyzes upcoming dates and generates a prioritized timetable.
- **Why**: Zero downtime and zero dead-ends for the student.

### Decision 3: Comprehensive Collaborative Study Suite
- **Approach**: Study groups feature group creation, join by code, shared group tasks with individual member checklist states, collaborative meetup scheduling, and a real-time group message feed.
- **Why**: Solves university study isolation and provides genuine peer accountability.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram
```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Shell                        │
│   Theme (Light/Dark) • Localization (EN/AR RTL) • M3 Top/Bottom Bar    │
├───────────────┬────────────────┬────────────────┬──────────────────────┤
│ Welcome Screen│ Dashboard View │ Task & Subject │ Exams & Timetable    │
│ & Onboarding  │ & Analytics    │ Manager        │ Planner              │
├───────────────┴────────────────┼────────────────┴──────────────────────┤
│      Pomodoro Focus Timer      │  Study Groups & Collaborative Chat    │
│    (State machine + ticking)   │  (Members, Shared Tasks, Meetups)     │
└───────────────▲────────────────┴────────────────▲──────────────────────┘
                │                                 │
                ▼                                 ▼
┌────────────────────────────────┐ ┌────────────────────────────────────┐
│      MainViewModel (State)     │ │    GroupViewModel & AI Engine      │
│  Reactive StateFlows, Filters, │ │  Firestore Sync & Gemini Flash     │
│  Calculated KPI Analytics      │ │  Bilingual Prompt Generation       │
└───────────────┬────────────────┘ └──────────────┬─────────────────────┘
                │                                 │
                ▼                                 ▼
┌────────────────────────────────┐ ┌────────────────────────────────────┐
│    Room Database (Local)       │ │     Firebase Firestore (Cloud)     │
│  • TaskEntity                  │ │  • study_groups                    │
│  • SubjectEntity               │ │  • group_members                   │
│  • ExamEntity                  │ │  • group_tasks & progress          │
│  • StudySessionEntity          │ │  • group_messages (live chat)      │
│  • PomodoroHistoryEntity       │ │  • group_meetups                   │
└────────────────────────────────┘ └────────────────────────────────────┘
```

### Core Entities & Data Models
1. **Task**: `id`, `title`, `description`, `subjectId`, `dueDate`, `dueTime`, `priority` (LOW/MEDIUM/HIGH), `status` (PENDING/IN_PROGRESS/COMPLETED), `isOverdue`.
2. **Subject**: `id`, `name`, `colorHex`, `code`, `targetWeeklyHours`.
3. **Exam**: `id`, `title`, `subjectId`, `examDate`, `examTime`, `location`, `notes`, `readinessPercent`.
4. **StudySession**: `id`, `subjectId`, `title`, `dayOfWeek`, `startTime`, `endTime`, `isCompleted`.
5. **StudyGroup**: `id`, `name`, `description`, `subject`, `joinCode`, `creatorId`, `creatorName`, `createdAt`.
6. **GroupMember**: `userId`, `userName`, `avatarColor`, `tasksCompleted`, `totalAssignedTasks`.
7. **GroupTask**: `id`, `groupId`, `title`, `dueDate`, `assignedMemberIds`, `completedMemberIds`.
8. **GroupChatMessage**: `id`, `groupId`, `senderId`, `senderName`, `message`, `timestamp`.
9. **GroupMeetup**: `id`, `groupId`, `title`, `date`, `time`, `platformOrLink`, `attendeeIds`.
10. **PomodoroState**: Focus/Break phase, seconds remaining, session count, sound enabled.

### Key Interactive Components
- **WelcomeScreen**: Visual hero preview, value cards, bilingual switch, "Get Started" and "Explore Features" actions.
- **DashboardScreen**: Real-time KPI summaries, circular completion meter, weekly productivity bar graph, upcoming urgency alerts, quick action triggers.
- **TaskListScreen**: Interactive search bar, subject filter chips, priority sort, swipe/toggle complete, add/edit task sheet.
- **SubjectManagementSheet**: Color picker palette, subject task counter, progress bar per subject.
- **ExamsScreen**: Dynamic countdown badge ("X days remaining"), urgency color grading, readiness adjustment.
- **ScheduleScreen**: Day-of-week tabs, time-blocked agenda cards, conflict validation for study session times.
- **PomodoroScreen**: Canvas circular arc timer, play/pause/reset, phase indicator, interval configuration dialog.
- **StudyGroupsScreen**:
  - Group List & Join Code dialog
  - Group Workspace: Tabbed view for **Tasks**, **Meetups**, **Members Progress**, and **Group Chat**
  - Live Chat message feed with instant send and auto-scroll
- **AiStudyAssistantSheet**: Task context injector, daily study plan generator, bilingual advisor chat.

---

## Plan Verification & Approval Gate

- All prompt requirements (Welcome page, Dashboard KPIs, Task CRUD, Subjects, Exams, Schedule, AI Assistant, Pomodoro, Analytics, In-App Notifications, English/Arabic RTL, Dark Mode, Data Persistence) and the collaborative study feature (Study Groups, Shared Tasks, Member Progress, Meetups, Group Chat) are fully integrated into this architectural plan.
- Verification will be conducted using `compile_applet` to ensure pristine compilation and error-free execution.
