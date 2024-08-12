package org.bxkr.octodiary.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontStyle
import androidx.glance.text.TextAlign
import org.bxkr.octodiary.*
import org.bxkr.octodiary.R
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.ui.theme.Typography
import org.bxkr.octodiary.widget.StatusWidget.Companion.setUpdateFor
import java.util.*

class StatusWidget : GlanceAppWidget() {
	override suspend fun provideGlance(context: Context, id: GlanceId) {
		val cache = context.cachePrefs
		provideContent {
			when {
				cache.get<String>("eventCalendar") == null -> NoAccountFound()
				else -> Content(cache)
			}
		}
	}

	companion object {
		fun Context.setUpdateFor(date: Date) {
			val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
			alarmManager.setAndAllowWhileIdle(
				AlarmManager.RTC, date.time, PendingIntent.getBroadcast(
					this,
					0,
					Intent(this, StatusWidgetReceiver::class.java),
					PendingIntent.FLAG_IMMUTABLE
				)
			)
		}
	}
}

@Composable
private fun Content(cache: CachePrefs) {
	val context = LocalContext.current

	val events = cache.getFromJson<List<Event>>("eventCalendar")
	val currentEvent = events.firstOrNull {
		it.startAt.parseLongDate().time <= Date().time && it.finishAt.parseLongDate().time > Date().time
	}
	val nearestStartingEvent =
		events.filter { it.startAt.parseLongDate().time > Date().time }.minByOrNull {
			it.startAt.parseLongDate().time - Date().time
		}
	val timeTillNextEvent =
		if (nearestStartingEvent != null) (nearestStartingEvent.startAt.parseLongDate().time - Date().time) else null

	var updateWidgetAt: Date? = null

	Column(
		GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface)
			.cornerRadius(16.dp).clickable {
				context.setUpdateFor(Date())
			}
	) {
		Column(GlanceModifier.padding(16.dp).fillMaxSize()) {
			when {
				currentEvent != null -> CurrentEvent(currentEvent, nearestStartingEvent) { updateWidgetAt = it }
				timeTillNextEvent != null -> NextEvent(
					timeTillNextEvent,
					nearestStartingEvent,
					events
				) { updateWidgetAt = it }

				else -> Holidays()
			}
		}
	}
	if (updateWidgetAt != null) context.setUpdateFor(updateWidgetAt!!)
}

@Composable
private fun Holidays() {
	Header(stringRes(R.string.holidays))
	// No more events found (holidays)
	// Widget is updated by android:updatePeriodMillis
	Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		Image(
			ImageProvider(R.drawable.rounded_weekend_24),
			contentDescription = stringRes(R.string.no_event),
			colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary)
		)
	}
}

@Composable
private fun ColumnScope.CurrentEvent(
	currentEvent: Event,
	nearestStartingEvent: Event?,
	setUpdateWidgetAt: (Date) -> Unit
) {
	val context = LocalContext.current
	// Event is currently running (lesson)
	Header(stringRes(R.string.at_lesson_now))
	Column(GlanceModifier.fillMaxWidth().defaultWeight()) {
		setUpdateWidgetAt(currentEvent.finishAt.parseLongDate())
		ThemedText(
			text = currentEvent.subjectName ?: currentEvent.title ?: stringRes(
				R.string.event
			), style = Typography.titleMedium.toGlanceStyle()
		)
		ThemedText(
			text = stringRes(
				R.string.time_from_to,
				currentEvent.startAt.parseLongDate().formatToTime(),
				currentEvent.finishAt.parseLongDate().formatToTime()
			)
		)
		val remoteViews =
			RemoteViews(context.packageName, R.layout.widget_chronometer)
		remoteViews.setChronometer(
			R.id.chronometer,
			SystemClock.elapsedRealtime() - (Date().time - currentEvent.finishAt.parseLongDate().time),
			stringRes(R.string.s_till_the_end),
			true
		)
		remoteViews.setChronometerCountDown(R.id.chronometer, true)
		ThemedText(
			text = (currentEvent.roomName
				?: "") + " " + (currentEvent.roomNumber ?: "")
		)
		AndroidRemoteViews(remoteViews)
	}
	Column(
		GlanceModifier.fillMaxWidth(),
		verticalAlignment = Alignment.Vertical.Bottom
	) {
		if (nearestStartingEvent != null && (nearestStartingEvent.startAt.parseLongDate()
				.formatToDay() == Date().formatToDay())
		) {
			ThemedText(
				text = stringRes(
					resId = R.string.next_event,
					nearestStartingEvent.startAt.parseLongDate().formatToTime()
				)
			)
			ThemedText(
				text = nearestStartingEvent.subjectName
					?: nearestStartingEvent.title ?: stringRes(
						R.string.event
					),
				style = Typography.titleSmall.toGlanceStyle()
			)
		}
	}
}


@Composable
private fun NextEvent(
	timeTillNextEvent: Long,
	nearestStartingEvent: Event?,
	events: List<Event>,
	setUpdateWidgetAt: (Date) -> Unit
) {
	val context = LocalContext.current
	setUpdateWidgetAt(nearestStartingEvent!!.startAt.parseLongDate())
	if (timeTillNextEvent <= 40 * 60 * 1000) {
		// No event is currently running, next event is soon (break)
		Header(stringRes(R.string.break_label))
		ThemedText(
			text = stringRes(
				resId = R.string.next_event,
				nearestStartingEvent.startAt.parseLongDate().formatToTime()
			)
		)
		val remoteViews =
			RemoteViews(context.packageName, R.layout.widget_chronometer)
		remoteViews.setChronometer(
			R.id.chronometer,
			SystemClock.elapsedRealtime() - (Date().time - nearestStartingEvent.startAt.parseLongDate().time),
			stringRes(R.string.s_till_the_start),
			true
		)
		remoteViews.setChronometerCountDown(R.id.chronometer, true)
		AndroidRemoteViews(remoteViews, GlanceModifier.wrapContentHeight())
		ThemedText(
			text = (nearestStartingEvent.roomName
				?: "") + " " + (nearestStartingEvent.roomNumber ?: "")
		)
		ThemedText(
			text = nearestStartingEvent.subjectName
				?: nearestStartingEvent.title ?: stringRes(
					R.string.event
				),
			style = Typography.titleSmall.toGlanceStyle(),
			modifier = GlanceModifier.padding(bottom = 16.dp)
		)
		if (nearestStartingEvent.homework?.entries != null) {
			ThemedText(
				stringRes(R.string.homework),
				style = Typography.titleSmall.toGlanceStyle()
			)
			LazyColumn {
				items(nearestStartingEvent.homework.entries) {
					ThemedText(it.description, fontStyle = FontStyle.Italic)
				}
			}
		}
	} else {
		// Next event starts more than 40 minutes later (rest)
		Header(stringRes(R.string.rest))
		val locale = context.resources.configuration.locales[0]
		val onTime =
			if (nearestStartingEvent.startAt.parseLongDate()
					.formatToDay() != Date().formatToDay()
			) {
				if (locale.language == Locale("ru").language) Calendar.getInstance()
					.apply {
						time = nearestStartingEvent.startAt.parseLongDate()
					}
					.getRussianWeekdayOnFormat() else nearestStartingEvent.startAt.parseLongDate()
					.formatToWeekday(context)
			} else {
				stringRes(R.string.today_lowercase)
			}
		ThemedText(
			stringRes(
				R.string.when_to_go,
				onTime,
				nearestStartingEvent.startAt.parseLongDate().formatToTime()
			)
		)
		LazyColumn(GlanceModifier.padding(top = 16.dp)) {
			items(
				nearestStartingEvent.startAt.parseLongDate().formatToDay().let {
					events.filter { ev ->
						ev.startAt.parseLongDate().formatToDay() == it
					}
				}) { event ->
				val time = event.startAt.parseLongDate().formatToTime()
				val name = event.subjectName
					?: event.title ?: stringRes(
						R.string.event
					)
				val place =
					event.roomNumber ?: event.place ?: event.buildingName ?: ""
				ThemedText(
					stringRes(
						R.string.widget_lesson_desc_template,
						time,
						name,
						place
					)
				)
			}
		}
	}
}

@Composable
fun Header(title: String) {
	val context = LocalContext.current
	Row(
		GlanceModifier.fillMaxWidth(),
		verticalAlignment = Alignment.Vertical.CenterVertically
	) {
		Row(GlanceModifier.defaultWeight()) {
			Image(
				provider = ImageProvider(R.mipmap.ic_launcher_round),
				contentDescription = stringRes(resId = R.string.app_name),
				modifier = GlanceModifier.size(24.dp).padding(4.dp),
				contentScale = ContentScale.Fit
			)
			ThemedText(text = title, style = Typography.titleMedium.toGlanceStyle())
		}
		Row(
			GlanceModifier.defaultWeight(),
			horizontalAlignment = Alignment.Horizontal.End
		) {
			AndroidRemoteViews(
				RemoteViews(
					context.packageName,
					R.layout.widget_textclock
				), GlanceModifier.fillMaxWidth()
			)
		}
	}
}

@Composable
private fun stringRes(@StringRes resId: Int, vararg formatArgs: Any) =
	LocalContext.current.getString(resId, *formatArgs)

@Composable
private fun stringRes(@StringRes resId: Int) = LocalContext.current.getString(resId)

@Composable
private fun NoAccountFound() {
	val context = LocalContext.current
	Box(
		GlanceModifier.fillMaxSize().background(GlanceTheme.colors.surface)
			.cornerRadius(16.dp).clickable {
				context.startActivity(Intent(context, MainActivity::class.java))
			}, Alignment.Center
	) {
		Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
			Image(
				ImageProvider(R.drawable.rounded_person_add_24),
				contentDescription = stringRes(R.string.log_in),
				colorFilter = ColorFilter.tint(GlanceTheme.colors.secondary)
			)
			ThemedText(
				stringRes(R.string.no_account_found_please_log_in),
				style = Typography.titleSmall.toGlanceStyle()
					.copy(textAlign = TextAlign.Center),
			)
		}
	}
}
