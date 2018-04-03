package com.ubiquitous.jakub.rpncalculator

/**
 * Created by Jakub on 03.04.2018.
 */
class OperationTranslator{
    val map : HashMap<Int, Operation> = hashMapOf(
            R.id.buttonAdd to Operation(OperationType.Sum, OperationType.Substract, 2),
            R.id.buttonSubstract to Operation(OperationType.Substract, OperationType.Sum, 2),
            R.id.buttonDivide to Operation(OperationType.Divide, OperationType.Divide, 2),
            R.id.buttonMultiply to Operation(OperationType.Multiply, OperationType.Divide, 2),
            R.id.buttonPower to Operation(OperationType.Power, OperationType.Undefined, 2),
            R.id.buttonSqrt to Operation(OperationType.Sqrt, OperationType.Undefined, 1)
    )
}

enum class OperationType{
    Sum, Substract, Divide, Multiply, Sqrt, Power, Undefined
}
class Operation(val operationType: OperationType, val reverseOperation: OperationType, val arity: Int){
}
