package com.speed.car.ui.history

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.viewModelScope
import com.bitkey.workhub.utils.SingleLiveEvent
import com.speed.car.core.BaseViewModel
import com.speed.car.domain.usecase.HistoryUseCase
import com.speed.car.model.History
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

class HistoryViewModel(
    private val historyUseCase: HistoryUseCase
) : BaseViewModel() {
    val histories = ObservableArrayList<History>()
    val back = SingleLiveEvent<Boolean>()

    init {
        viewModelScope.launch {
            historyUseCase.loadHistory().collectLatest {
                histories.addAll(it)
            }
        }
    }

    fun back() {
        back.postValue(true)
    }
}