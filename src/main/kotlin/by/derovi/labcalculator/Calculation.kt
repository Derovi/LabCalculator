package by.derovi.labcalculator

import java.math.BigDecimal

class Calculation {
    var firstNumber: BigDecimal? = null
    var secondNumber: BigDecimal? = null
    var operation: Operation? = null

    val result: BigDecimal?
        get() = if (firstNumber != null && secondNumber != null && operation != null) {
            when (operation) {
                Operation.PLUS -> firstNumber!! + secondNumber!!
                Operation.MINUS -> firstNumber!! - secondNumber!!
                Operation.MULTIPLY -> firstNumber!! * secondNumber!!
                Operation.DIV -> firstNumber!! / secondNumber!!
                else -> null
            }
        } else null
}