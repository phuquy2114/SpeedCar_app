package com.speed.car.ui.common

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.speed.car.R
import com.speed.car.databinding.DialogFragmentBinding
import com.speed.car.utils.gone
import com.speed.car.utils.isNotNull
import com.speed.car.utils.show

class CommonDialogFragment : DialogFragment() {

    var viewModel: CommonDialogViewModel? = null

    private val binding by lazy {
        DialogFragmentBinding.inflate(LayoutInflater.from(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let { window ->
            val metrics = resources.displayMetrics
            window.attributes.width = (metrics.widthPixels * 0.9).toInt()
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }

        setIcon()
        setTitle()
        setContent()
        setNegativeButton()
        setPositiveButton()
    }

    fun setIcon() {
        viewModel?.let { viewModel ->
            when {
                viewModel.resId.isNotNull() -> {
                    binding.imageView.setBackgroundResource(viewModel.resId!!)
                    binding.imageView.show()
                }
                else -> binding.imageView.gone()
            }
        }
    }

    fun setTitle() {
        viewModel?.let { viewModel ->
            when {
                viewModel.titleRes != -1 -> {
                    binding.title.setText(viewModel.titleRes)
                    binding.title.show()
                }
                !viewModel.titleString.isNullOrBlank() -> {
                    binding.title.text = viewModel.titleString
                    binding.title.show()
                }
                else -> {
                    binding.title.gone()
                }
            }
        }
    }

    fun setContent() {
        viewModel?.let { viewModel ->
            when {
                viewModel.contentRes != -1 -> {
                    binding.content.setText(viewModel.contentRes)
                    binding.content.show()
                }
                !viewModel.contentString.isNullOrBlank() -> {
                    binding.content.text = viewModel.contentString
                    binding.content.show()
                }
                else -> {
                    binding.content.gone()
                }
            }
        }
    }

    fun setPositiveButton(listener: (() -> Unit)? = null) {
        viewModel?.let { viewModel ->
            when {
                viewModel.positiveRes != -1 -> {
                    binding.btnOk.setText(viewModel.positiveRes)
                    binding.btnOk.show()
                }
                !viewModel.positiveTextButton.isNullOrBlank() -> {
                    binding.btnOk.text = viewModel.positiveTextButton
                    binding.btnOk.show()
                }
                else -> {
                    binding.btnOk.gone()
                }
            }
            binding.btnOk.setOnClickListener {
                viewModel.positiveButtonListener?.invoke()
                dismiss()
            }
        }
    }

    fun setNegativeButton() {
        viewModel?.let { viewModel ->
            when {
                viewModel.negativeRes != -1 -> {
                    binding.btnCancel.setText(viewModel.negativeRes)
                    binding.btnCancel.show()
                }
                !viewModel.negativeTextButton.isNullOrBlank() -> {
                    binding.btnCancel.text = viewModel.negativeTextButton
                    binding.btnCancel.show()
                }
                else -> {
                    binding.btnCancel.gone()
                }
            }
            binding.btnCancel.setOnClickListener {
                viewModel.negativeButtonListener?.invoke()
                dismiss()
            }
        }
    }

    class Builder(private val context: Context) {

        var viewModel = CommonDialogViewModel()

        fun setIcon(resId: Int?): Builder {
            viewModel.resId = resId
            return this
        }

        fun setIcon(@StringRes resId: Int): Builder {
            setIcon(resId)
            return this
        }

        fun setTitle(@StringRes resId: Int): Builder {
            viewModel.titleRes = resId
            return this
        }

        fun setTitle(title: String?): Builder {
            viewModel.titleString = title
            return this
        }

        fun setContent(content: String?): Builder {
            viewModel.contentString = content
            return this
        }

        fun setContent(@StringRes resId: Int): Builder {
            viewModel.contentRes = resId
            return this
        }

        fun setNegativeButton(
            @StringRes resId: Int = R.string.txt_cancel,
            listener: () -> Unit = {}
        ): Builder {
            viewModel.negativeRes = resId
            viewModel.negativeButtonListener = listener
            return this
        }

        fun setNegativeButton(
            text: String?,
            listener: () -> Unit = {}
        ): Builder {
            viewModel.negativeTextButton = text
            viewModel.negativeButtonListener = listener
            return this
        }

        fun setPositiveButton(
            @StringRes resId: Int = R.string.txt_ok,
            listener: () -> Unit = {}
        ): Builder {
            viewModel.positiveRes = resId
            viewModel.positiveButtonListener = listener
            return this
        }

        fun setPositiveButton(
            text: String?,
            listener: () -> Unit = {}
        ): Builder {
            viewModel.positiveTextButton = text
            viewModel.positiveButtonListener = listener
            return this
        }

        fun setIsCancelable(enable: Boolean): Builder {
            viewModel.isCancelable = enable
            return this
        }

        fun show() {
            CommonDialogFragment().apply {
                viewModel = this@Builder.viewModel
                isCancelable = this@Builder.viewModel.isCancelable
                if (this@Builder.context is AppCompatActivity) {
                    show(this@Builder.context.supportFragmentManager, this::class.java.simpleName)
                }
            }
        }
    }

    class CommonDialogViewModel {
        var resId: Int? = null
        var titleString: String? = null
        var contentString: String? = null
        var positiveTextButton: String? = null
        var negativeTextButton: String? = null

        var titleRes: Int = -1
        var contentRes: Int = -1
        var positiveRes: Int = -1
        var negativeRes: Int = -1

        var isCancelable = true

        var positiveButtonListener = {
        }

        var negativeButtonListener = {
        }
    }
}
