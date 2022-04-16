package com.speed.car.utils

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.BindingAdapter
import com.speed.car.R
import com.speed.car.databinding.ItemHeaderBinding

@BindingAdapter(value = ["setClick", "isBack", "setTitle"], requireAll = false)
fun initView(view: HeaderView, invoke: HeaderListener?, isBack: Int?, title: String?) {
    view.setBack(isBack)
    view.setOnClick(invoke)
    view.setTitle(title)
}

interface HeaderListener {
    fun onClick()
}

class HeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding by lazy {
        ItemHeaderBinding.inflate(LayoutInflater.from(context), this, true)
    }

    init {
        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.Header)
        val title = styledAttributes.getString(R.styleable.Header_title)
        binding.txtTitle.text = title

        styledAttributes.recycle()
    }

    private fun fixedTitlePosition() {
        val parent = findViewById<ConstraintLayout>(R.id.parent_layout)
        val mConstraintSet = ConstraintSet().apply { clone(parent) }

        if (isTitleTooLong()) {
            binding.txtTitle.gravity = Gravity.START
            mConstraintSet.clear(R.id.txtTitle, ConstraintSet.START)
            mConstraintSet.connect(R.id.txtTitle, ConstraintSet.START, R.id.back, ConstraintSet.END)
        } else {
            binding.txtTitle.gravity = Gravity.CENTER
            mConstraintSet.clear(R.id.txtTitle, ConstraintSet.START)
            mConstraintSet.connect(
                R.id.txtTitle,
                ConstraintSet.START,
                R.id.back,
                ConstraintSet.START
            )
        }
        mConstraintSet.applyTo(parent)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        fixedTitlePosition()
    }

    private fun isTitleTooLong(): Boolean {
        val parentWidth = binding.parentLayout.measuredWidth
        val textWidth = binding.txtTitle.measuredWidth
        val backWidth = binding.back.measuredWidth

        return (parentWidth - textWidth) / 2f < backWidth
    }

    fun setOnClick(invoke: HeaderListener?) {
        binding.back.setOnClickListener { invoke?.onClick() }
    }

    fun setBack(isBack: Int?) {
        isBack?.let {
            binding.back.visibility = it
        }
    }

    fun setTitle(title: String?) {
        title?.let {
            binding.txtTitle.text = title
        }
    }
}
