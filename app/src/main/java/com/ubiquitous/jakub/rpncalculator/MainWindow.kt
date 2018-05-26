package com.ubiquitous.jakub.rpncalculator

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import com.ubiquitous.jakub.rpncalculator.R.id.*
import kotlinx.android.synthetic.main.activity_main_portrait.*
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.math.sqrt


class MainWindow : AppCompatActivity() {
    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putSerializable("stack", stack)
            putSerializable("lastStackState", lastStackState)
            putSerializable("lastInputFieldOperation", lastInputFieldOperation)
        }
        super.onSaveInstanceState(outState)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        init()

        savedInstanceState?.run {
            stack = getSerializable("stack") as Stack<Double>
            lastStackState = getSerializable("lastStackState") as Stack<Stack<Double>>
            lastInputFieldOperation = getSerializable("lastInputFieldOperation") as Stack<Int>
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_portrait)
        init()
        setButtonsColors()
        setNumberFormat()
    }

    private fun setButtonsColors() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this.applicationContext)
        digitButtons?.forEach { it -> it.setBackgroundColor(preferences.getInt("digitsColor", DEFAULT_COLOR)) }
        operandsButtons?.forEach { it -> it.setBackgroundColor(preferences.getInt("operandsColor", DEFAULT_COLOR)) }
        commandButtons?.forEach { it -> it.setBackgroundColor(preferences.getInt("commandsColor", DEFAULT_COLOR)) }
    }

    fun init() {
        operandsButtons = setOf(buttonPower, buttonAdd, buttonMultiply, buttonDivide, buttonSubstract, buttonSqrt, buttonSign, buttonDot)
        commandButtons = setOf(buttonUndo, buttonClear, buttonDrop, buttonEnter, buttonSettings, buttonSwap)
        digitButtons = setOf(button0, button1, button2, button3, button4, button5, button6, button7, button8, button9)

        errorHandler = ErrorHandler(errorField)
        messagesHandler = MessageHandler(this.applicationContext)

        buttonClear.setOnLongClickListener { _ -> clearAll() }
        buttonUndo.setOnLongClickListener { _ -> undoStack() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SETTINGS_RESULT_CODE) {
            setButtonsColors()
            setNumberFormat()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setNumberFormat() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        try {
            val usersFormat = preferences.getString(PREF_NAME_STACK_NUMBER, DEFAULT_NUMBER_FORMAT)
            checkValue(usersFormat) //throws an exception if format is wrong
            formatter.applyPattern(usersFormat)
            updateStack()
        } catch (e: Exception) {
            when (e) {
                is NumberFormatException, is IllegalArgumentException -> {
                    errorHandler?.displayError(RecoverableException(getString(R.string.number_format_exception)), this.applicationContext)
                }
                else -> errorHandler?.displayError(e, this.applicationContext)
            }
        }
    }


    fun inputFieldButton(view: View) {
        val bt = ButtonTranslator()
        when (view.id) {
            buttonSign.id -> switchSign()
            buttonDot.id -> {
                if (inputField.text.count { it -> it == bt.map[buttonDot.id] } == 0)
                    inputField.setText("${inputField.text}${bt.map[view.id]}")
            }
            else ->
                inputField.setText("${inputField.text}${bt.map[view.id]}")

        }
        lastInputFieldOperation.push(view.id)
    }

    fun settingsButton(view: View) {
        val i = Intent(this, SettingsWindow::class.java)
        i.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsWindow.GeneralPreferenceFragment::class.java.name)
        startActivityForResult(i, SETTINGS_RESULT_CODE)
        startActivity(i)
    }

    fun switchSign() {
        inputField.let {
            if (it.text.firstOrNull() == '-') {
                it.setText(it.text.substring(1, it.text.length))
            } else {
                it.setText("-${it.text}")
            }
        }
    }

    fun enterButton(view: View) {
        val newValue = inputField.text
        try {
            stack.add(newValue.toString().toDouble())
        } catch (e: NumberFormatException) {
            errorHandler?.displayError(e, this.applicationContext)
        }
        updateStack()
    }

    fun clearButton(view: View) {
        clearInput()
        if (!clearInfoDisplayed) {
            messagesHandler?.displayMessage("Przyciśnij dłużej, aby wyczyścić stos")
            clearInfoDisplayed = true
        }
    }

    private fun clearAll(): Boolean {
        stack.clear()
        clearInput()
        updateStack()
        return true
    }

    fun clearInput() {
        inputField.text.clear();
        lastInputFieldOperation.clear()
    }

    fun swapButton(view: View) {
        if (stack.size < 2) {
            errorHandler?.displayError(Exception("Too few elements on the stack!"), this.applicationContext)
            return
        }
        val reversedTwo = stack.takeLast(2).reversed()
        stack.pop(2)
        stack.addAll(reversedTwo)
        updateStack()
    }

    fun dropButton(view: View) {
        if (stack.empty()) {
            errorHandler?.displayError(Exception("No elements on the stack"), this.applicationContext)
            return
        }
        stack.pop(1)
        updateStack()
    }

    fun operationButton(view: View) {
        view.id.let {
            if (it in OperationTranslator().map.keys && !stack.empty())
                try {
                    operation(it)
                } catch (e: Exception) {
                    errorHandler?.displayError(e, this.applicationContext)
                }

        }
    }

    fun undoButton(view: View) {
        if (lastInputFieldOperation.empty() || inputField.text.isEmpty())
            return
        when (lastInputFieldOperation.pop()) {
            buttonSign.id -> switchSign()
            else ->
                inputField.setText(inputField.text.subSequence(0..inputField.text.length - 2))

        }

    }

    private fun undoStack(): Boolean {
        stack.clear()
        if (lastStackState.size < 2) {
            updateStack()
            //messagesHandler?.displayMessage("Brak historii")
            return true;
        }
        lastStackState.pop()
        stack.addAll(lastStackState.pop())
        updateStack(false)
        return true
    }


    fun updateStack(clearInput: Boolean = true) {
        if (stack.size > 0 && clearInput)
            lastStackState.push(stack.clone() as Stack<Double>)
        val values = stack.takeLast(4).map { it -> formatter.format(it) }.mapIndexed { ind, it -> "${ind + 1}: $it" }.joinToString(separator = "\n", postfix = "")
        stackField.setText(values)
        if (clearInput)
            clearInput()
    }

    fun operation(operationButtonID: Int) {
        OperationTranslator().let {
            val operation = it.map[operationButtonID]
                    ?: throw IllegalArgumentException("Button with id ${operationButtonID} has no mapping to operationType clearAll!")
            //check if we can add number to the stack
            if (!inputField.text.isEmpty()) {
                enterButton(View(applicationContext))
            }
            if ((it.map[operationButtonID]?.arity ?: 0) > stack.size) {
                throw RecoverableException("Zbyt mało argumentów na stosie do wykonania operacji!")
            }

            stack.takeLastAndPop(operation.arity).arithmeticOperation(operation.operationType).putOnStack(stack)
            inputField.setText(stack.takeLast(1).toString())
            updateStack()
        }
    }

    private var clearInfoDisplayed = false
    val formatter = DecimalFormat()
    var stack = Stack<Double>()
    var lastStackState = Stack<Stack<Double>>()
    var lastInputFieldOperation = Stack<Int>()
    var errorHandler: ErrorHandler? = null
    var messagesHandler: MessageHandler? = null
    var digitButtons: Set<Button>? = null
    var commandButtons: Set<Button>? = null
    var operandsButtons: Set<Button>? = null

}

private fun <E> Stack<E>.takeLastAndPop(numberOfElements: Int): List<E> {
    val toTake = this.takeLast(numberOfElements)
    this.pop(numberOfElements)
    return toTake
}

private fun List<Double>.arithmeticOperation(operationType: OperationType): Double {
    return when (operationType) {

        OperationType.Sum -> this.reduce { result, element -> result + element }
        OperationType.Substract -> this.reduce { result, element -> result - element }
        OperationType.Divide -> this.reduce { result, element -> result / element }
        OperationType.Multiply -> this.reduce { result, element -> result * element }
        OperationType.Sqrt -> sqrt(this[0])
        OperationType.Power -> this[0].pow(this[1])
        OperationType.Undefined -> throw Exception("undefined operation")
    }
}

fun <T : Number> T.putOnStack(stack: Stack<T>) {
    stack.push(this)
}


fun <T> Stack<T>.pop(depth: Int) {
    for (i in 1..depth) {
        this.pop()
    }
}

class ButtonTranslator {
    val map: HashMap<Int, Char> = hashMapOf(
            button0 to '0',
            button1 to '1',
            button2 to '2',
            button3 to '3',
            button4 to '4',
            button5 to '5',
            button6 to '6',
            button7 to '7',
            button8 to '8',
            button9 to '9',
            buttonDot to '.',
            buttonSign to '-'
    )
}


