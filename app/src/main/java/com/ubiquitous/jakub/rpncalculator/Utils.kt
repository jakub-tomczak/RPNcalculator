package com.ubiquitous.jakub.rpncalculator

/**
 * Created by Jakub on 09.04.2018.
 */
fun checkValue(value: String){
    if (value.toString().contains(Regex("([a-d]|[f-z]|[A-D]|[F-Z]|[1-9])"))) {
        throw NumberFormatException(R.string.number_format_exception.toString())
    }
}