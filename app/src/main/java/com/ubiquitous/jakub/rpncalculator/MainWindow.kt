package com.ubiquitous.jakub.rpncalculator

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.ubiquitous.jakub.rpncalculator.R.id.*
import kotlinx.android.synthetic.main.activity_main_portrait.*
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.math.sqrt


class MainWindow : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_portrait)

        stackField.isEnabled = false
        stackField.isLongClickable = false
        stackField.isClickable = false
        errorHandler = ErrorHandler(errorField)
        messagesHandler = MessageHandler(this.applicationContext)
        buttonClear.setOnLongClickListener { _ ->  clearAll()}
        buttonUndo.setOnLongClickListener { _ -> undoStack() }
    }

    fun inputFieldButton(view: View){
        val bt = ButtonTranslator()
        when(view.id){
            buttonSign.id  -> switchSign()
            buttonDot.id ->{
                if(inputField.text.count{it -> it == bt.map[buttonDot.id]} == 0)
                    inputField.setText( "${inputField.text}${bt.map[view.id]}")
            }
            else ->
                inputField.setText( "${inputField.text}${bt.map[view.id]}")

        }
        lastInputFieldOperation.push(view.id)
    }
    fun switchSign(){
        inputField.let{
            if(it.text.firstOrNull() == '-'){
                it.setText(it.text.substring(1, it.text.length))
            }else{
                it.setText("-${it.text}")
            }
        }
    }

    val stack = Stack<Double>()
    fun enterButton(view: View){
        val newValue = inputField.text
        try{
            stack.add(newValue.toString().toDouble())
        }catch(e: NumberFormatException){
            errorHandler?.displayError(e, this.applicationContext)
        }
        updateStack()
    }
    private var clearInfoDisplayed = false
    fun clearButton(view: View){
        clearInput()
        if(!clearInfoDisplayed) {
            messagesHandler?.displayMessage("Przyciśnij dłużej, aby wyczyścić stos")
            clearInfoDisplayed = true
        }
    }

    private fun clearAll() : Boolean{
        stack.clear()
        clearInput()
        updateStack()
        return true
    }
    fun clearInput() {
        inputField.text.clear();
        lastInputFieldOperation.clear()
    }

    fun swapButton(view: View){
        if(stack.size < 2){
            errorHandler?.displayError(Exception("Too few elements on the stack!"), this.applicationContext)
            return
        }
        val reversedTwo = stack.takeLast(2).reversed()
        stack.pop(2)
        stack.addAll(reversedTwo)
        updateStack()
    }

    fun dropButton(view: View){
        if(stack.empty()){
            errorHandler?.displayError(Exception("No elements on the stack"), this.applicationContext)
            return
        }
        stack.pop(1)
        updateStack()
    }

    fun operationButton(view: View){
        view.id.let {
            if (it in OperationTranslator().map.keys && !stack.empty())
                try{
                    operation(it)
                }catch(e: Exception){
                    errorHandler?.displayError(e, this.applicationContext)
                }

        }
    }

    fun undoButton(view: View){
        if(lastInputFieldOperation.empty() || inputField.text.isEmpty())
            return
        when(lastInputFieldOperation.pop()){
            buttonSign.id  -> switchSign()
            else ->
                inputField.setText(inputField.text.subSequence(0..inputField.text.length-2))

        }

    }

    private fun undoStack(): Boolean {
        stack.clear()
        stack.addAll(lastStackState)
        updateStack(false)
        return true
    }


    fun updateStack(clearInput : Boolean = true){
        lastStackState.clear()
        lastStackState.addAll(stack)
        val values = stack.takeLast(4).map { it -> DecimalFormat("#.##E0").format(it) }.mapIndexed{ ind, it -> "${ind+1}: $it"  }.joinToString(separator = "\n", postfix = "")
        stackField.setText(values)
        if(clearInput)
            clearInput()
    }

    fun operation(operationButtonID : Int){
        OperationTranslator().let {
            val operation = it.map[operationButtonID] ?: throw IllegalArgumentException("Button with id ${operationButtonID} has no mapping to operationType clearAll!")
            if( (it.map[operationButtonID]?.arity ?: 0) > stack.size){
                throw RecoverableException("Zbyt mało argumentów na stosie do wykonania operacji!")
            }
            stack.takeLastAndPop(operation.arity).arithmeticOperation(operation.operationType).putOnStack(stack)
            inputField.setText(stack.takeLast(1).toString())
            updateStack()
        }
    }


    var lastStackState = Stack<Double>()
    val lastInputFieldOperation = Stack<Int>()
    var errorHandler : ErrorHandler? = null
    var messagesHandler : MessageHandler? = null

}
private fun <E> Stack<E>.takeLastAndPop(numberOfElements: Int): List<E> {
    val toTake = this.takeLast(numberOfElements)
    this.pop(numberOfElements)
    return toTake
}

private fun List<Double>.arithmeticOperation(operationType: OperationType): Double {
    return when(operationType){

        OperationType.Sum -> this.reduce{result, element -> result + element}
        OperationType.Substract -> this.reduce{result, element -> result - element}
        OperationType.Divide -> this.reduce{result, element -> result / element}
        OperationType.Multiply -> this.reduce{result, element -> result * element}
        OperationType.Sqrt -> sqrt(this[0])
        OperationType.Power -> this[0].pow(this[1])
        OperationType.Undefined -> throw Exception("undefined operation")
    }
}

fun <T : Number> T.putOnStack(stack : Stack<T>) {
    stack.push(this)
}



fun <T> Stack<T>.pop(depth: Int){
    for(i in 1..depth){
        this.pop()
    }
}
class ButtonTranslator{
    val map : HashMap<Int, Char> = hashMapOf(
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


