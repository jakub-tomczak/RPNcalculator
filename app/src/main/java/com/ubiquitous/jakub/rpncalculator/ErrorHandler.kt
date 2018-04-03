package com.ubiquitous.jakub.rpncalculator

import android.content.Context
import android.util.Log
import android.widget.EditText

/**
 * Created by Jakub on 02.04.2018.
 */
class ErrorHandler constructor(val errorField: EditText?){
    fun displayError(error: Exception, context : Context){
        if(error is RecoverableException){
            MessageHandler(context).displayMessage(error.message.toString())
        }
        Log.e(ErrorHandler::class.simpleName, "Exception type:${error.cause}\nMessage:${error.message}")
    }
}