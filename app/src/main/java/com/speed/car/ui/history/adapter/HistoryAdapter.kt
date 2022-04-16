package com.speed.car.ui.history.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableArrayList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.speed.car.core.BaseRecyclerViewAdapter
import com.speed.car.databinding.ItemHistoryBinding
import com.speed.car.model.History
import com.speed.car.ui.history.HistoryViewModel

@BindingAdapter("app:bindingAdapter")
fun RecyclerView.bindingAdapter(viewModel: HistoryViewModel) {
    if (adapter == null) {
        apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = SearchNotesAdapter(
                viewModel.histories,
            )

        }
    } else {
        adapter?.notifyDataSetChanged()
    }
}

class SearchNotesAdapter(
    historyData: ObservableArrayList<History>
) : BaseRecyclerViewAdapter<HistoryViewHolder>(historyData) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(
            ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bindData(data?.get(position) as History)

    }
}

class HistoryViewHolder(
    private val binding: ItemHistoryBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bindData(data: History) {
        binding.data = data

    }
}
