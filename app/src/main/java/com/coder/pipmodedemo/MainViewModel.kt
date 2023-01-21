package com.coder.pipmodedemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel: ViewModel() {

    private var job: Job? = null

    private val timeMillis = MutableLiveData("00:00:00")

    private val _started = MutableLiveData(false)

    val started: LiveData<Boolean> = _started
    val time = timeMillis

    private val sdf = SimpleDateFormat("hh:mm:ss:SS", Locale.US)


    fun startOrPause() {
        if (_started.value == true) {
            _started.value = false
            job?.cancel()
        } else {
            _started.value = true
            job = viewModelScope.launch { start() }
        }
    }

    private suspend fun CoroutineScope.start() {
        while (isActive) {
            timeMillis.value = sdf.format(Date())
            awaitFrame()
        }
    }

}
