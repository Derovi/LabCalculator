package by.derovi.labcalculator

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class Calculation {
    var number1: BigDecimal? = null
    var number2: BigDecimal? = null
    var number3: BigDecimal = BigDecimal.ZERO
    var number4: BigDecimal = BigDecimal.ZERO
    var operation1: Operation? = null
    var operation2: Operation = Operation.PLUS
    var operation3: Operation = Operation.PLUS

    var status = 0
    var format: Format = Format.DEFAULT

    val outputFormat = let {
        val format = DecimalFormat("#,##0.000000")
        val customSymbol = DecimalFormatSymbols()
        customSymbol.groupingSeparator = ' '
        format.decimalFormatSymbols = customSymbol
        format.isParseBigDecimal = true
        format
    }

    fun formattedResult(): String {
        val res = result
        val output = if (res == null || zeroDiv) "?" else when(format) {
            Format.DEFAULT -> res.format()
            Format.CUT -> res.setScale(0, RoundingMode.DOWN).toString()
            Format.MATH -> res.setScale(0, RoundingMode.HALF_UP).toString()
            Format.BOOKER -> res.setScale(0, RoundingMode.HALF_EVEN).toString()
        } ?: "?"
        return if (status > 6) {
            "<b>$output</b>"
        } else output
    }

    fun BigDecimal?.format() = if (this == null) null else outputFormat.format(this)
    fun String.markIf(criteria: Int) = if (status == criteria) "<b>$this</b>" else this

    val str: String
        get() = (number1?.toString() ?: "?").markIf(0) +
                " ${(operation1?.symbol ?: "?").markIf(1)} " +
                "(${(number2?.toString() ?: "?").markIf(2)} " +
                "${operation2.symbol.markIf(3)} " +
                "${number3.toString().markIf(4)}) " +
                operation3.symbol.markIf(5) +
                " ${number4.toString().markIf(6)} =" +
                " ${formattedResult()}"

    var zeroDiv = false

    fun calc(a: BigDecimal, b: BigDecimal, operation: Operation): BigDecimal = when (operation) {
            Operation.PLUS -> a + b
            Operation.MINUS -> a - b
            Operation.MULTIPLY -> a * b
            Operation.DIV -> {
                if (b == BigDecimal.ZERO) {
                    zeroDiv = true
                    BigDecimal.ZERO
                } else a / b
            }
        }

    val result: BigDecimal?
        get() {
            zeroDiv = false
            if (number1 == null || number2 == null || operation1 == null) {
                return null
            }
            val b = calc(number2!!, number3, operation2)
            if ((operation1 == Operation.PLUS || operation1 == Operation.MINUS) &&
                (operation3 == Operation.MULTIPLY || operation3 == Operation.DIV)) {
                return calc(number1!!, calc(b, number4, operation3), operation1!!)
            } else {
                return calc(calc(number1!!, b, operation1!!), number4, operation3)
            }
        }
}
