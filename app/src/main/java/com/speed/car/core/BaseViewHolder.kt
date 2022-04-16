package com.speed.car.core

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView

@Suppress("LeakingThis")
abstract class BaseViewHolder(view: View) :
	RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

	val context: Context = view.context

	init {
		view.setOnClickListener(this)
		view.setOnLongClickListener(this)
	}

	@Throws(Exception::class)
	abstract fun bindData(data: Any)
}
