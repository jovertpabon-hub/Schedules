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
import com.example.schedules.data.models.CourseClass
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * CoffeeLog Palette Color Providers for Glance widget styling.
 */
object CoffeeWidgetColors {
    private val DarkMocha = Color(0xFF623528)
    private val DeepEspresso = Color(0xFF4D2A00)
    private val WarmRoast = Color(0xFFCC6F00)
    private val GoldenCrema = Color(0xFFF2A900)
    private val WarmCoffeeCream = Color(0xFFFFF4D6)
    private val SteamedMilk = Color(0xFFF0DDAA)
    private val SurfaceDark = Color(0xFF1F1206)
    private val SurfaceVariantDark = Color(0xFF382104)

    val WidgetBackground = ColorProvider(day = WarmCoffeeCream, night = SurfaceDark)
    val CardBackground = ColorProvider(day = SteamedMilk, night = SurfaceVariantDark)
    val ClassItemContainer = ColorProvider(day = SteamedMilk, night = SurfaceVariantDark)
    val AccentPillar = ColorProvider(day = WarmRoast, night = GoldenCrema)

    val PrimaryText = ColorProvider(day = DarkMocha, night = SteamedMilk)
    val SecondaryText = ColorProvider(day = Color(0xFF7A4B3A), night = GoldenCrema)
    val MutedText = ColorProvider(day = Color(0xFF9E7767), night = Color(0xFFB89687))

    val BadgeBg = ColorProvider(day = WarmRoast, night = DeepEspresso)
    val BadgeText = ColorProvider(day = Color.White, night = GoldenCrema)
}

class CoffeeLogWidget : GlanceAppWidget() {

    companion object {
        val SMALL_SQUARE = DpSize(120.dp, 120.dp)
        val MEDIUM_HORIZONTAL = DpSize(240.dp, 120.dp)
        val LARGE_FULL = DpSize(260.dp, 260.dp)

        fun createTodayDeepLinkIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = "coffeelog://today".toUri()
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        fun createClassDeepLinkIntent(context: Context, classId: Long): Intent {
            return Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = "coffeelog://class/$classId".toUri()
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
            val revision = prefs[CoffeeLogWidgetHelper.KEY_REVISION] ?: 0L

            // Retrieve fresh schedule entities from Room database asynchronously on state update
            val allEntities by produceState(initialValue = emptyList(), key1 = revision) {
                val dao = ScheduleDatabase.getDatabase(context.applicationContext).scheduleDao()
                value = dao.fetchAllSchedulesDirect()
            }

            val currentDate = LocalDate.now()
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

            val size = LocalSize.current
            val todayIntent = createTodayDeepLinkIntent(context)

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(CoffeeWidgetColors.WidgetBackground)
                    .cornerRadius(16.dp)
                    .clickable(actionStartActivity(todayIntent))
            ) {
                when {
                    size.width < 210.dp || size.height < 180.dp -> {
                        SmallWidgetContent(
                            currentDate = currentDate,
                            todayClasses = todayClasses,
                            context = context
                        )
                    }
                    size.height < 210.dp -> {
                        MediumWidgetContent(
                            currentDate = currentDate,
                            todayClasses = todayClasses,
                            context = context
                        )
                    }
                    else -> {
                        LargeWidgetContent(
                            currentDate = currentDate,
                            todayClasses = todayClasses,
                            context = context
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun SmallWidgetContent(
        currentDate: LocalDate,
        todayClasses: List<CourseClass>,
        context: Context
    ) {
        val shortDateStr = currentDate.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Header: Day & Date
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "☕ $shortDateStr",
                    style = TextStyle(
                        color = CoffeeWidgetColors.PrimaryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                if (todayClasses.isNotEmpty()) {
                    Box(
                        modifier = GlanceModifier
                            .background(CoffeeWidgetColors.BadgeBg)
                            .cornerRadius(6.dp)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${todayClasses.size}",
                            style = TextStyle(
                                color = CoffeeWidgetColors.BadgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            if (todayClasses.isNotEmpty()) {
                val maxVisible = 2
                val visibleClasses = todayClasses.take(maxVisible)
                val remainingCount = todayClasses.size - maxVisible

                Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    visibleClasses.forEachIndexed { idx, course ->
                        if (idx > 0) Spacer(modifier = GlanceModifier.height(4.dp))
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(CoffeeWidgetColors.ClassItemContainer)
                                .cornerRadius(8.dp)
                                .padding(6.dp)
                                .clickable(actionStartActivity(createClassDeepLinkIntent(context, course.id ?: 0L)))
                        ) {
                            Column {
                                Text(
                                    text = course.courseCode,
                                    style = TextStyle(
                                        color = CoffeeWidgetColors.PrimaryText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = "${formatTimeShort(course.startTime)} - ${formatTimeShort(course.endTime)} • ${course.room}",
                                    style = TextStyle(
                                        color = CoffeeWidgetColors.MutedText,
                                        fontSize = 9.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (remainingCount > 0) {
                        Spacer(modifier = GlanceModifier.height(3.dp))
                        Text(
                            text = "+$remainingCount more classes",
                            style = TextStyle(
                                color = CoffeeWidgetColors.SecondaryText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            } else {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .background(CoffeeWidgetColors.CardBackground)
                        .cornerRadius(10.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No classes scheduled today.",
                        style = TextStyle(
                            color = CoffeeWidgetColors.SecondaryText,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun MediumWidgetContent(
        currentDate: LocalDate,
        todayClasses: List<CourseClass>,
        context: Context
    ) {
        val fullDateStr = currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US))

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
                    text = "☕ CoffeeLog",
                    style = TextStyle(
                        color = CoffeeWidgetColors.PrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = "•  $fullDateStr",
                    style = TextStyle(
                        color = CoffeeWidgetColors.SecondaryText,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .background(CoffeeWidgetColors.BadgeBg)
                        .cornerRadius(8.dp)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${todayClasses.size} classes",
                        style = TextStyle(
                            color = CoffeeWidgetColors.BadgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (todayClasses.isNotEmpty()) {
                val displayClasses = todayClasses.take(3)
                Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    displayClasses.forEachIndexed { idx, course ->
                        if (idx > 0) Spacer(modifier = GlanceModifier.height(4.dp))
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(CoffeeWidgetColors.ClassItemContainer)
                                .cornerRadius(10.dp)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .clickable(actionStartActivity(createClassDeepLinkIntent(context, course.id ?: 0L)))
                        ) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .width(3.dp)
                                        .height(20.dp)
                                        .background(CoffeeWidgetColors.AccentPillar)
                                        .cornerRadius(2.dp)
                                ) {}
                                Spacer(modifier = GlanceModifier.width(8.dp))
                                Text(
                                    text = course.courseCode,
                                    style = TextStyle(
                                        color = CoffeeWidgetColors.PrimaryText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = GlanceModifier.width(6.dp))
                                Text(
                                    text = course.courseName,
                                    style = TextStyle(
                                        color = CoffeeWidgetColors.SecondaryText,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Text(
                                    text = "${formatTimeShort(course.startTime)} - ${formatTimeShort(course.endTime)} • Rm ${course.room}",
                                    style = TextStyle(
                                        color = CoffeeWidgetColors.MutedText,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .background(CoffeeWidgetColors.CardBackground)
                        .cornerRadius(12.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No classes scheduled today.",
                        style = TextStyle(
                            color = CoffeeWidgetColors.SecondaryText,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun LargeWidgetContent(
        currentDate: LocalDate,
        todayClasses: List<CourseClass>,
        context: Context
    ) {
        val fullDateStr = currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.US))

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Header Row: App Title & Full Date
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.Start
            ) {
                // CoffeeLog branding
                Row(
                    modifier = GlanceModifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "☕",
                        style = TextStyle(fontSize = 20.sp)
                    )

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    Column {
                        Text(
                            text = "CoffeeLog",
                            style = TextStyle(
                                color = CoffeeWidgetColors.PrimaryText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = "Your daily schedule",
                            style = TextStyle(
                                color = CoffeeWidgetColors.SecondaryText,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Current date
                Text(
                    text = fullDateStr,
                    style = TextStyle(
                        color = CoffeeWidgetColors.SecondaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            // Daily Schedule List
            if (todayClasses.isNotEmpty()) {
                LazyColumn(
                    modifier = GlanceModifier.fillMaxWidth().defaultWeight()
                ) {
                    items(items = todayClasses, itemId = { course -> course.id ?: course.courseCode.hashCode().toLong() }) { course ->
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .background(CoffeeWidgetColors.ClassItemContainer)
                                .cornerRadius(10.dp)
                                .padding(10.dp)
                                .clickable(actionStartActivity(createClassDeepLinkIntent(context, course.id ?: 0L)))
                        ) {
                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Accent Indicator Bar
                                Box(
                                    modifier = GlanceModifier
                                        .width(4.dp)
                                        .height(32.dp)
                                        .background(CoffeeWidgetColors.AccentPillar)
                                        .cornerRadius(2.dp)
                                ) {}

                                Spacer(modifier = GlanceModifier.width(10.dp))

                                // Course Details
                                Column(modifier = GlanceModifier.defaultWeight()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = course.courseCode,
                                            style = TextStyle(
                                                color = CoffeeWidgetColors.PrimaryText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Spacer(modifier = GlanceModifier.width(6.dp))
                                        Text(
                                            text = course.courseName,
                                            style = TextStyle(
                                                color = CoffeeWidgetColors.SecondaryText,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = GlanceModifier.height(2.dp))

                                    Text(
                                        text = "${formatTimeShort(course.startTime)} - ${formatTimeShort(course.endTime)}  •  Room ${course.room}",
                                        style = TextStyle(
                                            color = CoffeeWidgetColors.MutedText,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .background(CoffeeWidgetColors.CardBackground)
                        .cornerRadius(12.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "☕ No classes scheduled today.",
                            style = TextStyle(
                                color = CoffeeWidgetColors.PrimaryText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }

    private fun formatTimeShort(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
    }
}
