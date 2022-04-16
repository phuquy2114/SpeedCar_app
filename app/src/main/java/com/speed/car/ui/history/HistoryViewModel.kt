package com.speed.car.ui.history

import androidx.databinding.ObservableArrayList
import com.speed.car.core.BaseViewModel
import com.speed.car.model.History
import java.util.*

class HistoryViewModel : BaseViewModel() {
    val histories = ObservableArrayList<History>()

    init {
        histories.addAll(
            listOf(
                History(0, Date(), "80.0", "75.0"),
                History(1, Date(), "75.0", "70.0"),
                History(2, Date(), "120", "100")
            )
        )
    }

}