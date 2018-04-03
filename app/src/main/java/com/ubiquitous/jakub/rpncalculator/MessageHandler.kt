package com.ubiquitous.jakub.rpncalculator

import android.content.Context
import android.widget.Toast

/**
 * Created by Jakub on 03.04.2018.
 */
class MessageHandler(val context: Context){
    fun displayMessage(message : String){
        Toast.makeText(this.context, message, Toast.LENGTH_SHORT).show()
    }
}