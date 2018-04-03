package com.ubiquitous.jakub.rpncalculator

import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * Created by Jakub on 02.04.2018.
 */
class MainWindowTest {
    @Test
    fun pop() {
        val stack : Stack<Int> = Stack();
        stack.push(3)
        stack.push(4)
        stack.push(7)

        stack.pop(2)
        assertTrue(stack.size == 1 && stack.pop() == 3)
    }


}