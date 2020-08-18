package com.fossil.trackme.base

import androidx.lifecycle.ViewModel
import com.fossil.trackme.utils.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Job
import kotlinx.coroutines.async

/**
 * BaseViewModel contains some functions that use regularly in project
 */
abstract class BaseViewModel : ViewModel(), BasicState {

    protected val viewModelJob = Job()
    protected val viewModelScope = CoroutineScope(Default + viewModelJob)

    val globalState = SingleLiveEvent<GlobalState>()

    /**
     * Trigger after View got destroyed
     * In this case will interupt any task run in the ViewModel
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * Run a task in background
     * @return a Deferred to stop the task manually
     */
    protected fun async(
        error: (throwable: Throwable) -> Unit = {},
        call: suspend () -> Unit = {}
    ): Deferred<*> {
        return viewModelScope.async {
            runCatching {
                call()
            }.onFailure {
                it.printStackTrace()
                error(it)
                postError(it.message)
            }
        }
    }

    override fun postError(msg: String?) {
        globalState.postValue(GlobalState.ERROR.apply {
            message = msg ?: ""
        })
    }

    override fun showLoading() {
        globalState.postValue(GlobalState.SHOW_LOADING)
    }

    override fun hideLoading() {
        globalState.postValue(GlobalState.HIDE_LOADING)
    }


}

interface BasicState {

    /**
     *
     */
    fun postError(msg: String?)

    /**
     * Not sure if have to use this, since show loading make app feel slower
     */
    fun showLoading()

    /**
     *
     */
    fun hideLoading()


}

/**
 * Any state that most of the screen need will define here
 */
enum class GlobalState {
    SHOW_LOADING,
    HIDE_LOADING,
    ERROR;
    var message: String = ""
}
