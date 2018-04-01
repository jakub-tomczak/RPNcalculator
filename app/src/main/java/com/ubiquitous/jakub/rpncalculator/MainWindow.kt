package com.ubiquitous.jakub.rpncalculator

import android.content.res.Configuration
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main_portrait.*

class MainWindow : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_portrait)

        editText.isEnabled = false
        editText.isLongClickable = false
        editText.isClickable = false
    }
    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
//        Log.i(this.localClassName, "changing configuration")
//        newConfig?.let {
//            when(it.orientation)
//            {
//                Configuration.ORIENTATION_LANDSCAPE -> {
//                    Log.i(this.localClassName, "Orientation Landscape")
//                   // setContentView(R.layout.activity_main_landscape)
//                }
//                Configuration.ORIENTATION_PORTRAIT -> {
//                    Log.i(this.localClassName, "Orientation Portrait")
//                      //  setContentView(R.layout.activity_main_portrait)
//                }
//                else -> Log.i(this.localClassName, "Orientation undefined")
//            }
//        }
    }
}
