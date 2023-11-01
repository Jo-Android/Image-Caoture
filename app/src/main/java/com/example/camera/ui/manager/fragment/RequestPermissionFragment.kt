package com.example.camera.ui.manager.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.camera.R
import com.example.camera.ui.custom.dialog.AskDialog
import com.example.camera.ui.manager.observer.CameraObserver
import org.koin.android.ext.android.inject

abstract class RequestPermissionFragment<B : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> B,
) : Fragment() {

    private var _binding: B?=null
    val binding get() = _binding!!
    private lateinit var observer: CameraObserver
    private val askDialog: AskDialog by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observer = CameraObserver(
            requireActivity().activityResultRegistry,
            onCameraGranted = {
                onCameraGranted()
            },
            onPermissionDenied = { isCamera ->
                askDialog.askPermissionDialog(
                    requireContext(),
                    getString(R.string.ask_camera_permission)
                ) { it, _ ->
                    if (it) {
                        if (isCamera)
                            observer.openCameraSettings(requireContext())
                    } else {
                        Log.e("TAG ", "Error Opening Camera Permission Denied")
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.camera_access_error),
                            Toast.LENGTH_LONG
                        ).show()
                        handelBackPressed()
                    }
                }
            }
        )
        lifecycle.addObserver(observer)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingFactory(inflater)
        initLayout()
        return _binding!!.root
    }

    abstract fun initLayout()

    abstract fun onStorageGranted()

    abstract fun onCameraGranted()

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        })
    }

    abstract fun onBackPressed()


    fun handelBackPressed(){
        if (findNavController().backQueue.size <= 1) {
            requireActivity().finish()
        } else {
            findNavController().popBackStack()
        }
    }

    fun requestCameraPermission(){
        observer.requestCameraPermission()
    }

    override fun onStart() {
        super.onStart()
        observer.register(viewLifecycleOwner)
        getPermission()
    }

    abstract fun getPermission()

    override fun onStop() {
        super.onStop()
        observer.destroy()
    }
}