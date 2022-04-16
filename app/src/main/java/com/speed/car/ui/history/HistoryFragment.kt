package com.speed.car.ui.history

import com.speed.car.core.BaseFragment
import com.speed.car.databinding.FragmentHistoryBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : BaseFragment<HistoryViewModel, FragmentHistoryBinding>() {
    override val viewModel: HistoryViewModel by viewModel()

    override fun getViewBinding(): FragmentHistoryBinding =
        FragmentHistoryBinding.inflate(layoutInflater)

    override fun viewBinding() {
        binding.viewModel = viewModel
    }

}