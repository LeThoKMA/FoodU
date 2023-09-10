package com.example.footu.base

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

abstract class BaseDialog<BINDING : ViewDataBinding> : DialogFragment() {

    lateinit var binding: BINDING

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            getLayoutResource(),
            container,
            false,
        )
        if (this.dialog!!.window != null) {
            this.dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        init(savedInstanceState, binding.root)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setSizeForDialog(9)
    }

    /**
     * Set size for base dialog
     */
    open fun setSizeForDialog(width: Int) {
        if (dialog?.window != null) {
            dialog?.window?.setLayout(
                (activity?.window?.decorView?.width?.times(width) ?: 0) / 10,
                WindowManager.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    protected fun setFullScreenDialog() {
        if (dialog?.window != null) {
            activity?.window?.decorView?.width?.let {
                dialog?.window?.setLayout(
                    it,
                    WindowManager.LayoutParams.MATCH_PARENT,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUp(view)
    }

    protected abstract fun getLayoutResource(): Int

    protected abstract fun init(saveInstanceState: Bundle?, view: View?)

    protected abstract fun setUp(view: View?)

    override fun show(
        fragmentManager: FragmentManager,
        tag: String?,
    ) {
        val transaction =
            fragmentManager.beginTransaction()
        val prevFragment = fragmentManager.findFragmentByTag(tag)
        if (prevFragment != null) {
            transaction.remove(prevFragment)
        }
        transaction.addToBackStack(null)
        show(transaction, tag)
    }

    fun dismissDialog(tag: String?) {
        dismiss()
    }
}
