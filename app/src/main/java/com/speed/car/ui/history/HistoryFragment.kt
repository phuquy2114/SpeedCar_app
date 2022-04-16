package com.speed.car.ui.history

import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.speed.car.core.BaseFragment
import com.speed.car.core.utils.observe
import com.speed.car.databinding.FragmentHistoryBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class HistoryFragment : BaseFragment<HistoryViewModel, FragmentHistoryBinding>() {
    override val viewModel: HistoryViewModel by viewModel()

    override fun getViewBinding(): FragmentHistoryBinding =
        FragmentHistoryBinding.inflate(layoutInflater)

    override fun viewBinding() {
        binding.viewModel = viewModel
    }

    override fun observeViewModel() {
        observe(viewModel.back) {
            findNavController().popBackStack()
        }
    }
}