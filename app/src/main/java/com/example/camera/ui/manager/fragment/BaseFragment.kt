package com.example.camera.ui.manager.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<B : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B,
    private val isCustomBackPress: Boolean = false
) : Fragment() {

    private var _binding: B? = null
    val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingFactory(inflater)
        initLayout()
        observe()
        return _binding!!.root
    }

    abstract fun observe()

    abstract fun initLayout()


    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isCustomBackPress)
                    onBackPressed()
                else
                    handelBackPressed()
            }
        })
    }

    abstract fun onBackPressed()


    fun handelBackPressed() {
        if (findNavController().backQueue.size <= 2) {
            requireActivity().finish()
        } else {
            findNavController().popBackStack()
        }
    }
}