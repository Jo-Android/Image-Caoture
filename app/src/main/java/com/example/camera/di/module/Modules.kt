package com.example.camera.di.module

import android.content.Context
import com.example.camera.ui.custom.dialog.AskDialog
import com.example.camera.ui.viewModel.CameraViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

object Modules {

    val viewModelModule = module {
        viewModel { CameraViewModel() }
    }


    val ui = module {
        single { AskDialog() }
        factory { key ->
            androidContext().getSharedPreferences(key.get<String>(0), Context.MODE_PRIVATE)
        }
    }
}