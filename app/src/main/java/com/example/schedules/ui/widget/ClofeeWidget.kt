package com.example.schedules.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.schedules.MainActivity
import com.example.schedules.data.local.ScheduleDatabase
import com.example.schedules.data.models.ClassStatus
import com.example.schedules.data.models.CourseClass
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Clofee Palette Color Providers for Glance widget styling.
 * Designed to strictly reflect the main app screen's theme (Color.kt) and visual hierarchy.
 */
object ClofeeWidgetColors {
    // Foundation Coffee Palette (directly matching Color.kt)
    private val DarkMocha = Color(0xFF543020)
    private val DeepEspresso = Color(0xFF4D2A00)
    private val WarmRoast = Color(0xFFB85D00)
    private val GoldenCrema = Color(0xFFD99A16)
    private val WarmCoffeeCream = Color(0xFFF5E6C5)
    private val SteamedMilk = Color(0xFFDCC58F)
    private val AppBackgroundBase = Color(0xFFD5B68B)
    private val SurfaceDark = Color(0xFF1B1009)
    private val SurfaceVariantDark = Color(0xFF302014)

    // Main Widget Backgrounds & Containers
    val WidgetBackground = ColorProvider(day = AppBackgroundBase, night = SurfaceDark)
    val CardBackground = ColorProvider(day = SteamedMilk, night = SurfaceVariantDark)
    val ClassItemContainer = ColorProvider(day = SteamedMilk, night = SurfaceVariantDark)

    // Left Accent Pillars (matching ClassItemCard)
    val AccentPillar = ColorProvider(day = WarmRoast, night = GoldenCrema)
    val OngoingAccentPillar = ColorProvider(day = GoldenCrema, night = GoldenCrema)
    val CompletedAccentPillar = ColorProvider(day = Color(0xFF9E7767), night = Color(0xFF5E453A))

    // Text Colors
    val PrimaryText = ColorProvider(day = DarkMocha, night = WarmCoffeeCream)
    val SecondaryText = ColorProvider(day = DeepEspresso, night = SteamedMilk)
    val MutedText = ColorProvider(day = Color(0xFF7A5847), night = Color(0xFFA68E80))
    val CompletedText = ColorProvider(day = Color(0xFF8A6C5D), night = Color(0xFF8C756B))

    // Header Badges
    val BadgeBg = ColorProvider(day = SteamedMilk, night = SurfaceVariantDark)
    val BadgeText = ColorProvider(day = DarkMocha, night = GoldenCrema)

    // Status Badges
    val OngoingBadgeBg = ColorProvider(day = GoldenCrema, night = GoldenCrema)
    val OngoingBadgeText = ColorProvider(day = DeepEspresso, night = DeepEspresso)

    val UpcomingBadgeBg = ColorProvider(day = WarmRoast, night = DeepEspresso)
    val UpcomingBadgeText = ColorProvider(day = WarmCoffeeCream, night = GoldenCrema)

    val CompletedBadgeBg = ColorProvider(day = Color(0xFFB89F88), night = Color(0xFF422E22))
    val CompletedBadgeText = ColorProvider(day = DarkMocha, night = SteamedMilk)

    // Hero / Next Class Card (reflecting Timer Overview / NextClassCard in main screen)
    val HeroBackground = ColorProvider(day = DeepEspresso, night = Color(0xFF2B1600))
    val HeroTextPrimary = ColorProvider(day = WarmCoffeeCream, night = WarmCoffeeCream)
    val HeroTextSecondary = ColorProvider(day = GoldenCrema, night = GoldenCrema)
    val HeroTextMuted = ColorProvider(day = SteamedMilk.copy(alpha = 0.85f), night = SteamedMilk.copy(alpha = 0.85f))
    val HeroBadgeBg = ColorProvider(day = GoldenCrema, night = GoldenCrema)
    val HeroBadgeText = ColorProvider(day = DeepEspresso, night = DeepEspresso)
}

private data class WidgetClassStatus(
    val courseClass: CourseClass?,
    val status: ClassStatus,
    val timeFormatted: String,
    val isAllCompleted: Boolean = false,
    val isFreeDay: Boolean = false
)

class ClofeeWidget : GlanceAppWidget() {

    companion object {
        val SMALL_SQUARE = DpSize(120.dp, 120.dp)
        val MEDIUM_HORIZONTAL = DpSize(240.dp, 120.dp)
        val LARGE_FULL = DpSize(260.dp, 260.dp)

        fun createTodayDeepLinkIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = "clofee://today".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        fun createClassDeepLinkIntent(context: Context, classId: Long): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = "clofee://class/$classId".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, MEDIUM_HORIZONTAL, LARGE_FULL)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val revision = prefs[ClofeeWidgetHelper.KEY_REVISION] ?: 0L

            // Retrieve fresh schedule entities from Room database asynchronously on state update
            val allEntities by produceState(initialValue = emptyList(), key1 = revision) {
                val dao = ScheduleDatabase.getDatabase(context.applicationContext).scheduleDao()
                value = dao.fetchAllSchedulesDirect()
            }

            val currentDate = LocalDate.now()
            val currentTime = LocalTime.now()
            val todayName = currentDate.dayOfWeek.name

            // Filter and map entities for today in chronological order
            val todayEntities = allEntities.filter { it.dayName.equals(todayName, ignoreCase = true) }
            val todayClasses = todayEntities.map { entity ->
                CourseClass(
                    id = entity.id,
                    courseCode = entity.courseCode,
                    courseName = entity.courseName,
                    startTime = entity.startTime,
                    endTime = entity.endTime,
                    room = entity.room
                )
            }.sortedBy { it.startTime }

            val widgetStatus = calculateWidgetStatus(todayClasses, currentTime)
            val size = LocalSize.current

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ClofeeWidgetColors.WidgetBackground)
                    .cornerRadius(16.dp)
                    .clickable(actionStartActivity(createTodayDeepLinkIntent(context)))
            ) {
                when {
                    size.height < 140.dp -> {
                        SmallWidgetContent(currentDate, widgetStatus, context)
                    }
                    size.width < 220.dp -> {
                        MediumWidgetContent(currentDate, todayClasses, widgetStatus, context)
                    }
                    else -> {
                        LargeWidgetContent(currentDate, currentTime, todayClasses, widgetStatus, context)
                    }
                }
            }
        }
    }

    @Composable
    private fun SmallWidgetContent(
        currentDate: LocalDate,
        widgetStatus: WidgetClassStatus,
        context: Context
    ) {
        val fullDateStr = currentDate.format(DateTimeFormatter.ofPattern("MMM d", Locale.US))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Header Row: App Branding + Date
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☕ parclock",
                    style = TextStyle(
                        color = ClofeeWidgetColors.PrimaryText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = fullDateStr,
                    style = TextStyle(
                        color = ClofeeWidgetColors.SecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Body Card: Reflecting Hero / Next Class Card
            WidgetHeroCard(
                widgetStatus = widgetStatus,
                context = context,
                modifier = GlanceModifier.fillMaxWidth().defaultWeight()
            )
        }
    }

    @Composable
    private fun MediumWidgetContent(
        currentDate: LocalDate,
        todayClasses: List<CourseClass>,
        widgetStatus: WidgetClassStatus,
        context: Context
    ) {
        val fullDateStr = currentDate.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☕ parclock",
                    style = TextStyle(
                        color = ClofeeWidgetColors.PrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = "•  $fullDateStr",
                    style = TextStyle(
                        color = ClofeeWidgetColors.SecondaryText,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .background(ClofeeWidgetColors.BadgeBg)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (todayClasses.isNotEmpty()) "${todayClasses.size} classes" else "Free Day",
                        style = TextStyle(
                            color = ClofeeWidgetColors.BadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (todayClasses.isNotEmpty()) {
                // Hero Card showing active/next class
                WidgetHeroCard(
                    widgetStatus = widgetStatus,
                    context = context,
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                )
            } else {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .background(ClofeeWidgetColors.CardBackground)
                        .cornerRadius(12.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "☕ A Clear Day Ahead",
                            style = TextStyle(
                                color = ClofeeWidgetColors.PrimaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Text(
                            text = "No classes scheduled today. Enjoy your day!",
                            style = TextStyle(
                                color = ClofeeWidgetColors.SecondaryText,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun LargeWidgetContent(
        currentDate: LocalDate,
        currentTime: LocalTime,
        todayClasses: List<CourseClass>,
        widgetStatus: WidgetClassStatus,
        context: Context
    ) {
        val fullDateStr = currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // App Header Row: Title & Date
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "☕",
                        style = TextStyle(fontSize = 18.sp)
                    )
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Column {
                        Text(
                            text = "parclock",
                            style = TextStyle(
                                color = ClofeeWidgetColors.PrimaryText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Your daily schedule",
                            style = TextStyle(
                                color = ClofeeWidgetColors.SecondaryText,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Text(
                    text = fullDateStr,
                    style = TextStyle(
                        color = ClofeeWidgetColors.SecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // 1. Hero / Timer Overview Card (Matching NextClassCard on main screen)
            WidgetHeroCard(
                widgetStatus = widgetStatus,
                context = context,
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(modifier = GlanceModifier.height(10.dp))

            // 2. Schedule Section Header
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Schedule",
                    style = TextStyle(
                        color = ClofeeWidgetColors.SecondaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.defaultWeight())

                Box(
                    modifier = GlanceModifier
                        .background(ClofeeWidgetColors.BadgeBg)
                        .cornerRadius(6.dp)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${todayClasses.size} ${if (todayClasses.size == 1) "Class" else "Classes"}",
                        style = TextStyle(
                            color = ClofeeWidgetColors.BadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // 3. Class Items List (Matching ClassItemCard on main screen)
            if (todayClasses.isNotEmpty()) {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                ) {
                    items(
                        items = todayClasses,
                        itemId = { course -> course.id ?: course.courseCode.hashCode().toLong() }
                    ) { course ->
                        WidgetClassItemCard(
                            course = course,
                            currentTime = currentTime,
                            context = context,
                            modifier = GlanceModifier.padding(vertical = 3.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .background(ClofeeWidgetColors.CardBackground)
                        .cornerRadius(12.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "☕ A Clear Day Ahead",
                            style = TextStyle(
                                color = ClofeeWidgetColors.PrimaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "No classes scheduled today. Enjoy your free time!",
                            style = TextStyle(
                                color = ClofeeWidgetColors.SecondaryText,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetHeroCard(
        widgetStatus: WidgetClassStatus,
        context: Context,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(ClofeeWidgetColors.HeroBackground)
                .cornerRadius(12.dp)
                .padding(10.dp)
                .clickable(
                    actionStartActivity(
                        if (widgetStatus.courseClass?.id != null) {
                            createClassDeepLinkIntent(context, widgetStatus.courseClass.id)
                        } else {
                            createTodayDeepLinkIntent(context)
                        }
                    )
                )
        ) {
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                // Header Row inside Hero Card
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (widgetStatus.status) {
                            ClassStatus.ONGOING -> "▶ CURRENT CLASS"
                            ClassStatus.UPCOMING -> "⏰ UP NEXT"
                            ClassStatus.NONE -> if (widgetStatus.isAllCompleted) "☕ ALL CLASSES FINISHED" else "☕ DAILY OVERVIEW"
                        },
                        style = TextStyle(
                            color = ClofeeWidgetColors.HeroTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    val course = widgetStatus.courseClass
                    val badgeText = when {
                        course != null -> course.courseCode
                        widgetStatus.isAllCompleted -> "COMPLETED"
                        else -> "FREE DAY"
                    }

                    Box(
                        modifier = GlanceModifier
                            .background(ClofeeWidgetColors.HeroBadgeBg)
                            .cornerRadius(6.dp)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = TextStyle(
                                color = ClofeeWidgetColors.HeroBadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Course Info inside Hero Card
                val course = widgetStatus.courseClass
                if (course != null) {
                    Text(
                        text = course.courseName,
                        style = TextStyle(
                            color = ClofeeWidgetColors.HeroTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.height(3.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = widgetStatus.timeFormatted,
                            style = TextStyle(
                                color = ClofeeWidgetColors.HeroTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        Text(
                            text = "•  ${formatTimeShort(course.startTime)} - ${formatTimeShort(course.endTime)}  •  Rm ${course.room}",
                            style = TextStyle(
                                color = ClofeeWidgetColors.HeroTextMuted,
                                fontSize = 10.sp
                            ),
                            maxLines = 1
                        )
                    }
                } else {
                    Text(
                        text = if (widgetStatus.isAllCompleted) "You've completed your schedule for today." else "No classes scheduled today.",
                        style = TextStyle(
                            color = ClofeeWidgetColors.HeroTextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = if (widgetStatus.isAllCompleted) "Enjoy your free time" else "Make the most of your day",
                        style = TextStyle(
                            color = ClofeeWidgetColors.HeroTextMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun WidgetClassItemCard(
        course: CourseClass,
        currentTime: LocalTime,
        context: Context,
        modifier: GlanceModifier = GlanceModifier
    ) {
        val isCompleted = !currentTime.isBefore(course.endTime)
        val isOngoing = !currentTime.isBefore(course.startTime) && currentTime.isBefore(course.endTime)

        val pillarColor = when {
            isOngoing -> ClofeeWidgetColors.OngoingAccentPillar
            isCompleted -> ClofeeWidgetColors.CompletedAccentPillar
            else -> ClofeeWidgetColors.AccentPillar
        }

        val titleTextColor = when {
            isCompleted -> ClofeeWidgetColors.CompletedText
            else -> ClofeeWidgetColors.PrimaryText
        }

        val subtitleTextColor = when {
            isCompleted -> ClofeeWidgetColors.CompletedText
            else -> ClofeeWidgetColors.MutedText
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(ClofeeWidgetColors.ClassItemContainer)
                .cornerRadius(10.dp)
                .padding(horizontal = 10.dp, vertical = 7.dp)
                .clickable(actionStartActivity(createClassDeepLinkIntent(context, course.id ?: 0L)))
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Accent Pillar Bar (Matching ClassItemCard in main app)
                Box(
                    modifier = GlanceModifier
                        .width(4.dp)
                        .height(28.dp)
                        .background(pillarColor)
                        .cornerRadius(2.dp)
                ) {}

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Course Info Column
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = course.courseCode,
                            style = TextStyle(
                                color = titleTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        Text(
                            text = course.courseName,
                            style = TextStyle(
                                color = subtitleTextColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    Text(
                        text = "${formatTimeShort(course.startTime)} - ${formatTimeShort(course.endTime)}  •  Rm ${course.room}",
                        style = TextStyle(
                            color = subtitleTextColor,
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.width(6.dp))

                // Status Badge Tag
                val (badgeBg, badgeText, label) = when {
                    isOngoing -> Triple(ClofeeWidgetColors.OngoingBadgeBg, ClofeeWidgetColors.OngoingBadgeText, "NOW")
                    isCompleted -> Triple(ClofeeWidgetColors.CompletedBadgeBg, ClofeeWidgetColors.CompletedBadgeText, "DONE")
                    else -> Triple(ClofeeWidgetColors.UpcomingBadgeBg, ClofeeWidgetColors.UpcomingBadgeText, "NEXT")
                }

                Box(
                    modifier = GlanceModifier
                        .background(badgeBg)
                        .cornerRadius(6.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = label,
                        style = TextStyle(
                            color = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }

    private fun calculateWidgetStatus(
        todayClasses: List<CourseClass>,
        currentTime: LocalTime
    ): WidgetClassStatus {
        if (todayClasses.isEmpty()) {
            return WidgetClassStatus(
                courseClass = null,
                status = ClassStatus.NONE,
                timeFormatted = "00:00:00",
                isFreeDay = true
            )
        }

        val ongoing = todayClasses.firstOrNull {
            !currentTime.isBefore(it.startTime) && currentTime.isBefore(it.endTime)
        }
        if (ongoing != null) {
            val remainingSeconds = maxOf(0L, Duration.between(currentTime, ongoing.endTime).seconds)
            return WidgetClassStatus(
                courseClass = ongoing,
                status = ClassStatus.ONGOING,
                timeFormatted = formatDurationShort(remainingSeconds)
            )
        }

        val upcoming = todayClasses.firstOrNull { currentTime.isBefore(it.startTime) }
        if (upcoming != null) {
            val startSeconds = maxOf(0L, Duration.between(currentTime, upcoming.startTime).seconds)
            return WidgetClassStatus(
                courseClass = upcoming,
                status = ClassStatus.UPCOMING,
                timeFormatted = formatUpcomingDuration(startSeconds)
            )
        }

        return WidgetClassStatus(
            courseClass = todayClasses.lastOrNull(),
            status = ClassStatus.NONE,
            timeFormatted = "Finished",
            isAllCompleted = true
        )
    }

    private fun formatDurationShort(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        return when {
            hrs > 0 -> "${hrs}h ${mins}m left"
            mins > 0 -> "${mins}m left"
            else -> "< 1m left"
        }
    }

    private fun formatUpcomingDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        return when {
            hrs > 0 -> "In ${hrs}h ${mins}m"
            mins > 0 -> "In ${mins}m"
            else -> "Starting"
        }
    }

    private fun formatTimeShort(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
    }
}
