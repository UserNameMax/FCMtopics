package ru.mishenko.maksim.fcmtopics

import kotlinx.coroutines.flow.MutableStateFlow

object EventList {
    var mutableEventList = MutableStateFlow(listOf<String>())
}