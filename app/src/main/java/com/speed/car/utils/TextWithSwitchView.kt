package com.speed.car.utils

import com.speed.car.R
import com.speed.car.databinding.ViewTextWithSwitchBinding
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

import kotlin.concurrent.thread

class TextWithSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding by lazy {
        ViewTextWithSwitchBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    private var listener: OnClickListener? = null

    private var intercept: () -> Boolean = { true }

    val isChecked: Boolean
        get() = binding.toggle.isChecked

    init {

        if (attrs != null) {
            context.obtainStyledAttributes(attrs, R.styleable.TextWithSwitchView).use {
                setText(
                    it.getResourceId(
                        R.styleable.TextWithSwitchView_twsv_text,
                        R.string.empty_string
                    )
                )
                setSubText(
                    it.getResourceId(
                        R.styleable.TextWithSwitchView_twsv_sub_text,
                        R.string.empty_string
                    )
                )

                it.getBoolean(R.styleable.TextWithSwitchView_twsv_checked, false)
                    .let(this::setChecked)
            }
        }
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        listener = onClickListener
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (intercept().not()) return true // means `cancel`.
        }

        val result = super.dispatchTouchEvent(event)
        val view = this
        val handler = Handler()

        if (event.action == MotionEvent.ACTION_UP) {
            thread {
                handler.post {
                    listener?.onClick(view)
                }
            }
        }

        return result
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (intercept().not()) return true // means `cancel`.
        }

        if (event.action == KeyEvent.ACTION_UP && (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || event.keyCode == KeyEvent.KEYCODE_ENTER)) {
            listener?.onClick(this)
        }

        return super.dispatchKeyEvent(event)
    }

    fun setInterceptor(intercept: () -> Boolean) {
        this.intercept = intercept
    }

    fun setTwravBackground(drawable: Drawable?) {
        binding.rootLayout.background = drawable
    }

    fun setText(@StringRes resId: Int) {
        binding.text.setText(resId)
    }

    fun setSubText(@StringRes resId: Int) {
        if (resId == R.string.empty_string) {
            return
        }
        binding.textSub.isVisible = true
        binding.textSub.setText(resId)
    }

    fun setChecked(checked: Boolean) {
        binding.toggle.isChecked = checked
    }

    fun setOnCheckedChange(onChecked: ((checked: Boolean) -> Unit)? = null) {
        binding.toggle.setOnCheckedChangeListener { _, checked ->
            onChecked?.invoke(checked)
        }
    }

    fun setOnSwitchClick(onChecked: ((checked: Boolean) -> Unit)? = null) {
        binding.toggle.setOnClickListener {
            onChecked?.invoke(binding.toggle.isChecked)
        }
    }

    fun setEnable(enable: Boolean) {
        binding.toggle.isEnabled = enable
    }
}

@BindingAdapter("twsv_checked")
fun TextWithSwitchView.bindChecked(checked: Boolean?) {
    checked?.let(this::setChecked)
}

@BindingAdapter("twsv_enable")
fun TextWithSwitchView.setEnable(enable: Boolean?) {
    enable?.let(this::setEnable)
}
