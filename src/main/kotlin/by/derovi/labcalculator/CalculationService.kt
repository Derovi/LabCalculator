package by.derovi.labcalculator

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

@Service
class CalculationService {
    val userToCalculation = mutableMapOf<Long, Calculation>()

    fun update(request: Request) {
        if (request.message == "/start") {
            sendMessage(request, buildString {
                append("<b>Лабораторная работа №1.</b> Финансовый калькулятор\n")
                append("<b>Демидович Роман, 4 курс, 4 группа, 2022</b>")
            })
        }
        if (request.message == "/start" || request.message == "/reset") {
            userToCalculation[request.userID] = Calculation()
            askNumber(request)
            return
        }
        val calculation = userToCalculation[request.userID] ?: Calculation().also { userToCalculation[request.userID] = it }
        fun validateZeroDiv(): Boolean {
            calculation.result
            return if (calculation.zeroDiv) {
                sendMessage(request, "Деление на ноль! Введите число снова!")
                askNumber(request)
                false
            } else true
        }
        when (calculation.status) {
            0 -> {
                calculation.number1 = validateNumber(request) ?: let {
                    invalidInput(request)
                    askNumber(request)
                    return
                }
                calculation.status++
                askOperation(request)
            }
            1 -> {
                calculation.operation1 = validateOperation(request) ?: let {
                    invalidInput(request)
                    askOperation(request)
                    return
                }
                calculation.status++
                askNumber(request)
            }
            2 -> {
                calculation.number2 = validateNumber(request) ?: let {
                    invalidInput(request)
                    askNumber(request)
                    return
                }
                if (validateZeroDiv()) {
                    calculation.status++
                    askOperation(request)
                }
            }
            3 -> {
                calculation.operation2 = validateOperation(request) ?: let {
                    invalidInput(request)
                    askOperation(request)
                    return
                }
                calculation.status++
                askNumber(request)
            }
            4 -> {
                calculation.number3 = validateNumber(request) ?: let {
                    invalidInput(request)
                    askNumber(request)
                    return
                }
                if (validateZeroDiv()) {
                    calculation.status++
                    askOperation(request)
                }
            }
            5 -> {
                calculation.operation3 = validateOperation(request) ?: let {
                    invalidInput(request)
                    askOperation(request)
                    return
                }
                calculation.status++
                askNumber(request)
            }
            6 -> {
                calculation.number4 = validateNumber(request) ?: let {
                    invalidInput(request)
                    askNumber(request)
                    return
                }
                if (validateZeroDiv()) {
                    calculation.status++
                    askFormat(request)
                }
            }
            7 -> {
                calculation.format = validateFormat(request) ?: let {
                    invalidInput(request)
                    askFormat(request)
                    return
                }
                calculation.status++
                printResult(request)
                userToCalculation[request.userID] = Calculation()
                askNumber(request)
            }
        }
    }

    fun printResult(request: Request) {
        sendMessage(request, "Результат: \n${getHeader(request)}")
    }

    fun getHeader(request: Request) = userToCalculation[request.userID]?.str ?: ""

    fun validateFormat(request: Request): Format? = Format.values().find { request.message == it.name }

    fun askNumber(request: Request) {
        sendMessage(request, "${getHeader(request)}\nВведите число",
            with(InlineKeyboardMarkup.builder()) {
                keyboardRow(mutableListOf(
                    InlineKeyboardButton
                        .builder()
                        .text("Сброс")
                        .callbackData("/reset")
                        .build()))
                build()
            }
        )
    }

    fun validateNumber(request: Request): BigDecimal? {
        val format = let {
            val format = DecimalFormat("#,##0.#####################################")
            val customSymbol = DecimalFormatSymbols()
            customSymbol.groupingSeparator = ' '
            format.decimalFormatSymbols = customSymbol
            format.isParseBigDecimal = true
            format
        }


        val input = request.message.replace(",", ".")
        val realPart = input.substringAfter(".", "")
        var zeroesNum = realPart.reversed().takeWhile { it == '0' }.length
        if (zeroesNum == realPart.length && realPart.isNotEmpty()) ++zeroesNum
        val noSpaceInput = input.replace(" ", "")
        val result = noSpaceInput.toBigDecimalOrNull()
        return if (result != null && format.format(result) == input.take(input.length - zeroesNum) || result.toString() == input) {
            result
        } else null
    }

    fun askFormat(request: Request) {
        sendMessage(request, "${getHeader(request)}\nУкажите округление", with(InlineKeyboardMarkup.builder()) {
            keyboardRow(mutableListOf(
                InlineKeyboardButton
                    .builder()
                    .text("Математическое")
                    .callbackData(Format.MATH.name)
                    .build(),
                InlineKeyboardButton
                    .builder()
                    .text("Бухгалтерское")
                    .callbackData(Format.BOOKER.name)
                    .build(),
                InlineKeyboardButton
                    .builder()
                    .text("Усечение")
                    .callbackData(Format.CUT.name)
                    .build(),
            ))
            keyboardRow(mutableListOf(
                InlineKeyboardButton
                    .builder()
                    .text("Сброс")
                    .callbackData("/reset")
                    .build()))
            build()
        })
    }

    fun askOperation(request: Request) {
        sendMessage(request, "${getHeader(request)}\nУкажите операцию", with(InlineKeyboardMarkup.builder()) {
            keyboardRow(mutableListOf(
                InlineKeyboardButton
                    .builder()
                    .text("+")
                    .callbackData("+")
                    .build(),
                InlineKeyboardButton
                    .builder()
                    .text("-")
                    .callbackData("-")
                    .build(),
                InlineKeyboardButton
                    .builder()
                    .text("*")
                    .callbackData("*")
                    .build(),
                InlineKeyboardButton
                    .builder()
                    .text("/")
                    .callbackData("/")
                    .build(),
            ))
            keyboardRow(mutableListOf(
                InlineKeyboardButton
                    .builder()
                    .text("Сброс")
                    .callbackData("/reset")
                    .build()))
            build()
        })
    }

    fun validateOperation(request: Request) = when (request.message) {
        "+" -> Operation.PLUS
        "-" -> Operation.MINUS
        "*" -> Operation.MULTIPLY
        "/" -> Operation.DIV
        else -> null
    }

    fun sendMessage(request: Request, text: String, keyboard: InlineKeyboardMarkup? = null) {
        val message = SendMessage()
        message.setChatId(request.chatID)
        message.text = if (text.isEmpty()) "No text?!" else text

        message.replyMarkup = keyboard
        message.parseMode = "HTML"
        message.disableWebPagePreview()
        request.bot.execute(message)
    }

    fun invalidInput(request: Request) {
        sendMessage(request, "Некорректный ввод. Попробуйте снова!")
    }
}
