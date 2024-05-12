package org.bxkr.octodiary

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.bxkr.octodiary.models.classmembers.ClassMember
import org.bxkr.octodiary.models.classranking.RankingMember
import org.bxkr.octodiary.models.events.Event
import org.bxkr.octodiary.models.homeworks.Homework
import org.bxkr.octodiary.models.lessonschedule.LessonSchedule
import org.bxkr.octodiary.models.mark.MarkInfo
import org.bxkr.octodiary.models.marklistdate.MarkListDate
import org.bxkr.octodiary.models.mealbalance.MealBalance
import org.bxkr.octodiary.models.profile.ProfileResponse
import org.bxkr.octodiary.models.profilesid.ProfilesId
import org.bxkr.octodiary.models.rankingforsubject.RankingForSubject
import org.bxkr.octodiary.models.schoolinfo.SchoolInfo
import org.bxkr.octodiary.models.sessionuser.SessionUser
import org.bxkr.octodiary.models.subjectranking.SubjectRanking
import org.bxkr.octodiary.models.visits.VisitsResponse
import org.bxkr.octodiary.network.MESLoginService.refreshToken
import org.bxkr.octodiary.network.NetworkService.externalApi
import org.bxkr.octodiary.network.interfaces.DSchoolAPI
import org.bxkr.octodiary.network.interfaces.MainSchoolAPI
import org.bxkr.octodiary.network.interfaces.SchoolSessionAPI
import org.bxkr.octodiary.network.interfaces.SecondaryAPI
import java.util.Calendar
import java.util.Date

object DataService {
    lateinit var subsystem: Diary
    lateinit var mainSchoolApi: MainSchoolAPI
    lateinit var dSchoolApi: DSchoolAPI
    lateinit var secondaryApi: SecondaryAPI
    lateinit var schoolSessionApi: SchoolSessionAPI

    lateinit var token: String

    val listOfValues
        get() = listOfNotNull(
            ::userId,
            ::sessionUser,
            ::eventCalendar,
            ::ranking,
            ::classMembers,
            ::profile,
            ::visits.takeIf { subsystem == Diary.MES },
            ::marksDate,
            ::marksSubject,
            ::homeworks,
            ::mealBalance.takeIf { subsystem == Diary.MES },
            ::schoolInfo,
            ::subjectRanking
        )

    val listOfStates
        get() = listOfNotNull(
            ::hasUserId,
            ::hasSessionUser,
            ::hasEventCalendar,
            ::hasRanking,
            ::hasClassMembers,
            ::hasProfile,
            ::hasVisits.takeIf { subsystem == Diary.MES },
            ::hasMarksDate,
            ::hasMarksSubject,
            ::hasHomeworks,
            ::hasMealBalance.takeIf { subsystem == Diary.MES },
            ::hasSchoolInfo,
            ::hasSubjectRanking
        )

    val mapOfDemoResourceIds = mapOf(
        ::userId to R.raw.demo_user_id,
        ::sessionUser to R.raw.demo_session_user,
        ::eventCalendar to R.raw.demo_event_calendar,
        ::eventsRange to R.raw.demo_events_range,
        ::ranking to R.raw.demo_ranking,
        ::classMembers to R.raw.demo_class_members,
        ::profile to R.raw.demo_profile,
        ::visits to R.raw.demo_visits,
        ::marksDate to R.raw.demo_marks_date,
        ::marksSubject to R.raw.demo_marks_subject,
        ::homeworks to R.raw.demo_homeworks,
        ::mealBalance to R.raw.demo_meal_balance,
        ::schoolInfo to R.raw.demo_school_info,
        ::subjectRanking to R.raw.demo_subject_ranking
    ).mapKeys { it.key.name }

    lateinit var userId: ProfilesId
    var hasUserId = false

    lateinit var sessionUser: SessionUser
    var hasSessionUser = false

    lateinit var eventCalendar: List<Event>
    var hasEventCalendar = false

    lateinit var eventsRange: List<Long>

    lateinit var ranking: List<RankingMember>
    var hasRanking = false

    lateinit var classMembers: List<ClassMember>
    var hasClassMembers = false

    lateinit var subjectRanking: List<SubjectRanking>
    var hasSubjectRanking = false

    lateinit var profile: ProfileResponse
    var hasProfile = false

    lateinit var visits: VisitsResponse
    var hasVisits = false

    lateinit var marksDate: MarkListDate
    var hasMarksDate = false

    lateinit var marksSubject: List<org.bxkr.octodiary.models.marklistsubject.MarkListSubjectItem>
    var hasMarksSubject = false

    lateinit var homeworks: List<Homework>
    var hasHomeworks = false

    lateinit var mealBalance: MealBalance
    var hasMealBalance = false

    lateinit var schoolInfo: SchoolInfo
    var hasSchoolInfo = false

    // ADD_NEW_FIELD_HERE
    // Don't forget to add demo cache data in res/raw folder, preferably with MES flavor

    val states
        get() =
            listOfNotNull(
                ::hasUserId,
                ::hasSessionUser,
                ::hasEventCalendar,
                ::hasEventCalendar,
                ::hasRanking,
                ::hasClassMembers,
                ::hasProfile,
                ::hasVisits.takeIf { subsystem == Diary.MES },
                ::hasMarksDate,
                ::hasMarksSubject,
                ::hasHomeworks,
                ::hasMealBalance.takeIf { subsystem == Diary.MES },
                ::hasSchoolInfo,
                ::hasSubjectRanking
            )

    val fields
        get() =
            listOfNotNull(
                ::userId,
                ::sessionUser,
                ::eventCalendar,
                ::eventsRange,
                ::ranking,
                ::classMembers,
                ::profile,
                ::visits.takeIf { subsystem == Diary.MES },
                ::marksDate,
                ::marksSubject,
                ::homeworks,
                ::mealBalance.takeIf { subsystem == Diary.MES },
                ::schoolInfo,
                ::subjectRanking
            )

    val loadedEverything = mutableStateOf(false)

    var tokenExpirationHandler: (() -> Unit)? = null

    var onSingleItemInUpdateAllLoadedHandler: ((name: String, progress: Float) -> Unit)? = null

    var loadingStarted = false

    var currentProfile = 0

    fun updateUserId(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        dSchoolApi.profilesId(token)
            .baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
                if (body.size == 0) {
                    tokenExpirationHandler?.invoke()
                } else {
                    userId = body
                    hasUserId = true
                    onUpdated()
                }
            }
    }

    fun updateSessionUser(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::userId.isInitialized)
        schoolSessionApi.sessionUser(SessionUser.Body(token)).baseEnqueue(
            ::baseErrorFunction, ::baseInternalExceptionFunction
        ) { body ->
            sessionUser = body
            hasSessionUser = true
            onUpdated()
        }
    }

    fun updateEventCalendar(weeksBefore: Int = 0, weeksAfter: Int = 0, onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        val startDate = Calendar.getInstance().also {
            it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR) - weeksBefore)
            it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val endDate = Calendar.getInstance().also {
            it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR) + weeksAfter)
            it.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        secondaryApi.events(
            "Bearer $token",
            personIds = profile.children[currentProfile].contingentGuid,
            beginDate = startDate.time.formatToDay(),
            endDate = endDate.time.formatToDay(),
            expandFields = "homework,marks"
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
            eventCalendar = body.response
            eventsRange = listOf(startDate.time.time, endDate.time.time)
            hasEventCalendar = true
            onUpdated()
        }
    }

    fun getEventWeek(date: Date, listener: (events: List<Event>, range: List<Long>) -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        val startDate = Calendar.getInstance().also {
            it.time = date
            it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR))
            it.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val endDate = Calendar.getInstance().also {
            it.time = date
            it.set(Calendar.WEEK_OF_YEAR, it.get(Calendar.WEEK_OF_YEAR))
            it.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        secondaryApi.events(
            "Bearer $token",
            personIds = profile.children[currentProfile].contingentGuid,
            beginDate = startDate.time.formatToDay(),
            endDate = endDate.time.formatToDay(),
            expandFields = "homework,marks"
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { body ->
            listener(body.response, listOf(startDate.time.time, endDate.time.time))
        }
    }

    fun getMarkInfo(markId: Long, errorListener: (String) -> Unit, listener: (MarkInfo) -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        mainSchoolApi.markInfo(
            token,
            markId = markId,
            studentId = profile.children[currentProfile].studentId
        ).baseEnqueue(errorListenerForMessage(errorListener)) { listener(it) }
    }

    fun updateRanking(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        var rankingFinished = false
        var classMembersFinished = false

        // Ranking request:
        secondaryApi.classRanking(
            token,
            personId = profile.children[currentProfile].contingentGuid,
            date = Date().formatToDay()
        ).baseEnqueue({ errorBody: ResponseBody, httpCode: Int, className: String? ->
            val errorText = errorBody.string()

            if (errorText.contains("Рейтинг не доступен.")) {
                ranking = emptyList()
                hasRanking = true
                rankingFinished = true
                if (classMembersFinished) onUpdated()
            } else {
                baseErrorFunction(errorBody, httpCode, className)
            }
        }, ::baseInternalExceptionFunction) {
            ranking = it
            hasRanking = true
            rankingFinished = true
            if (classMembersFinished) onUpdated()
        }

        // Class members request for matching names:
//        dSchoolApi.classMembers( todo: uncomment when serverside fixed (or idk)
//            token,
//            classUnitId = profile.children[currentProfile].classUnitId
//        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
//            classMembers = it
//            hasClassMembers = true
//            classMembersFinished = true
//            if (rankingFinished) onUpdated()
//        }
        classMembers = listOf()
        hasClassMembers = true
        classMembersFinished = true
        if (rankingFinished) onUpdated()
    }

    fun updateSubjectRanking(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        secondaryApi.subjectRanking(
            token,
            profile.children[currentProfile].contingentGuid,
            Date().formatToDay()
        ).baseEnqueue({ errorBody: ResponseBody, httpCode: Int, className: String? ->
            val errorText = errorBody.string()

            if (errorText.contains("Рейтинг не доступен.")) {
                subjectRanking = emptyList()
                hasSubjectRanking = true
                onUpdated()
            } else {
                baseErrorFunction(errorBody, httpCode, className)
            }
        }) {
            subjectRanking = it
            hasSubjectRanking = true
            onUpdated()
        }
    }

    fun updateProfile(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)

        mainSchoolApi.profile(token)
            .baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
                profile = it
                hasProfile = true
                onUpdated()
            }
    }

    fun updateVisits(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        assert(subsystem == Diary.MES)

        mainSchoolApi.visits(
            token,
            profile.children[0].contractId,
            fromDate = Calendar.getInstance().apply {
                time = Date()
                set(Calendar.DAY_OF_YEAR, get(Calendar.DAY_OF_YEAR) - 61)
            }.time.formatToDay(),
            toDate = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) { visitsResponse ->
            visits = VisitsResponse(
                payload = visitsResponse.payload.sortedByDescending {
                    it.date.parseFromDay().toInstant().toEpochMilli()
                }
            )
            hasVisits = true
            onUpdated()
        }
    }

    fun updateMarksDate(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.markList(
            token,
            studentId = profile.children[currentProfile].studentId,
            fromDate = Calendar.getInstance().run {
                set(Calendar.WEEK_OF_YEAR, get(Calendar.WEEK_OF_YEAR) - 4)
                time
            }.formatToDay(),
            toDate = Date().formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            marksDate = it
            hasMarksDate = true
            onUpdated()
        }
    }

    fun updateMarksSubject(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.subjectMarks(
            token,
            studentId = profile.children[currentProfile].studentId
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            marksSubject = it.payload
            hasMarksSubject = true
            onUpdated()
        }
    }

    fun updateHomeworks(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.homeworks(
            token,
            studentId = profile.children[currentProfile].studentId,
            fromDate = Date().formatToDay(),
            toDate = Calendar.getInstance().run {
                set(Calendar.WEEK_OF_YEAR, get(Calendar.WEEK_OF_YEAR) + 1)
                time
            }.formatToDay()
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            homeworks = it.payload
            hasHomeworks = true
            onUpdated()
        }
    }

    fun updateMealBalance(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)
        assert(subsystem == Diary.MES)

        dSchoolApi.mealBalance(
            token,
            contractId = profile.children[currentProfile].contractId
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            mealBalance = it
            hasMealBalance = true
            onUpdated()
        }
    }

    fun updateSchoolInfo(onUpdated: () -> Unit) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.schoolInfo(
            token,
            schoolId = profile.children[currentProfile].school.id,
            classUnitId = profile.children[currentProfile].classUnitId
        ).baseEnqueue(::baseErrorFunction, ::baseInternalExceptionFunction) {
            schoolInfo = it
            hasSchoolInfo = true
            onUpdated()
        }
    }

    fun getRankingForSubject(
        subjectId: Long,
        errorListener: (String) -> Unit,
        listener: (List<RankingForSubject>) -> Unit,
    ) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        secondaryApi.rankingForSubject(
            token,
            profile.children[currentProfile].contingentGuid,
            profile.children[currentProfile].classUnitId,
            Date().formatToDay(),
            subjectId
        ).baseEnqueue(errorFunction = errorListenerForMessage(errorListener)) { listener(it) }
    }

    fun refreshToken(context: Context, onUpdated: () -> Unit) {
        assert(this::token.isInitialized)

        if (subsystem == Diary.MES) {
            context.refreshToken {
                onUpdated()
            }
        } else {
            secondaryApi.refreshToken("Bearer $token")
                .baseEnqueue(::baseErrorFunction) {
                    token = it
                    updateUserId { onUpdated() }
                }
        }
    }

    fun setHomeworkDoneState(homeworkId: Long, state: Boolean, listener: () -> Unit) {
        assert(this::token.isInitialized)

        if (state) {
            mainSchoolApi.doHomework(token, homeworkId)
                .baseEnqueue(::baseErrorFunction) { listener() }
        } else {
            mainSchoolApi.undoHomework(token, homeworkId)
                .baseEnqueue(::baseErrorFunction) { listener() }
        }
    }

    fun getLessonInfo(
        lessonId: Long,
        errorListener: (String) -> Unit,
        listener: (LessonSchedule) -> Unit,
    ) {
        assert(this::token.isInitialized)
        assert(this::profile.isInitialized)

        mainSchoolApi.lessonSchedule(
            token,
            lessonId,
            profile.children[currentProfile].studentId
        ).baseEnqueue(errorListenerForMessage(errorListener)) {
            listener(it)
        }
    }

    fun getLaunchUrl(homeworkId: Long, materialId: String, listener: (String) -> Unit) {
        assert(this::token.isInitialized)

        dSchoolApi.launchMaterial(token, homeworkId, materialId)
            .baseEnqueue({ errorBody, httpCode, className ->
                if (httpCode < 400) {
                    listener(errorBody.string())
                } else {
                    baseErrorFunction(errorBody, httpCode, className)
                }
            }) {}
    }

    fun sendStatistic(onUpdated: () -> Unit) {
        assert(this::userId.isInitialized)

        externalApi().sendStat(
            subsystem.ordinal,
            encodeToBase64(hash(userId[0].id.toString()))
        ).baseEnqueue { onUpdated() }
    }

    fun <Model> pushUserSettings(path: String, content: Model, onUpdated: () -> Unit) {
        assert(this::token.isInitialized)

        mainSchoolApi.pushUserSettings(token, path, Gson().toJsonTree(content).asJsonObject)
            .baseEnqueue {
                onUpdated()
            }
    }

    fun updateAll(context: Context? = null) {
        if (loadingStarted) return else loadingStarted = true
        // ADD_NEW_FIELD_HERE
        states.forEach { it.set(false) }
        val onSingleItemLoad = { name: String ->
            val statesInit = states.map { it.get() }
            onSingleItemInUpdateAllLoadedHandler?.invoke(name, (statesInit.count { it }
                .toFloat()) / (statesInit.size.toFloat()))
            if (!(statesInit.contains(false))) {
                loadedEverything.value = true
            }
            println("$name response is loaded, $statesInit")
        }
        if (context != null) {
            refreshToken(context) {}
        }
        updateUserId {
            onSingleItemLoad(::userId.name)
            updateSessionUser {
                onSingleItemLoad(::sessionUser.name)
                updateProfile {
                    onSingleItemLoad(::profile.name)
                    updateEventCalendar {
                        onSingleItemLoad(::eventCalendar.name)
                        onSingleItemLoad(::eventsRange.name)
                    }
                    updateMarksDate { onSingleItemLoad(::marksDate.name) }
                    updateMarksSubject { onSingleItemLoad(::marksSubject.name) }
                    updateHomeworks { onSingleItemLoad(::homeworks.name) }
                    updateRanking {
                        onSingleItemLoad(::classMembers.name)
                        onSingleItemLoad(::ranking.name)
                    }
                    updateSubjectRanking { onSingleItemLoad(::subjectRanking.name) }
                    if (subsystem == Diary.MES) updateVisits { onSingleItemLoad(::visits.name) }
                    if (subsystem == Diary.MES) updateMealBalance { onSingleItemLoad(::mealBalance.name) }
                    updateSchoolInfo { onSingleItemLoad(::schoolInfo.name) }
                }
            }
        }
    }

    fun loadFromCache(get: (String) -> String) {
        fields.map { it.name }.forEachIndexed { index, it ->
            javaClass.getDeclaredField(it)
                .set(this, Gson().fromJson(get(it), javaClass.getDeclaredField(it).genericType))
            states[index].set(true)
        }
    }

    fun Context.loadDemoCache() =
        DataService.loadFromCache {
            resources.openRawResource(
                DataService.mapOfDemoResourceIds.getValue(
                    it
                )
            ).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }

}