package by.derovi.labcalculator

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton

@Service
class CalculationService {
    val userToCalculation = mutableMapOf<Long, Calculation>()

    fun update(request: Request) {
        if (request.message == "/start") {
            sendMessage(request, buildString {
                append("<b>Лабораторная работа №1.</b> Финансовый калькулятор\n")
                append("<b>Демидович Роман, 4 курс, 4 группа, 2022</b>")
            })
            requireFirstNumber(request)
            return
        }
        val calculation = userToCalculation[request.userID]
        if (calculation == null) {
            firstNumberInput(request)
        } else if (calculation.operation == null) {
            operationInput(request, calculation)
        } else {
            secondNumberInput(request, calculation)
        }
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

    fun requireFirstNumber(request: Request) {
        sendMessage(request, "Укажите первое число")
    }

    fun firstNumberInput(request: Request) {
        val value = request.message.replace(",", ".").toBigDecimalOrNull()
        if (value == null) {
            invalidInput(request)
            return
        }

        val calculation = Calculation()
        calculation.firstNumber = value
        userToCalculation[request.userID] = calculation
        sendMessage(request, "Укажите необходимую операцию", with(InlineKeyboardMarkup.builder()) {
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
            ))
            build()
        })
    }

    fun operationInput(request: Request, calculation: Calculation) {
        calculation.operation = when (request.message) {
            "+" -> Operation.PLUS
            "-" -> Operation.MINUS
            else -> {
                invalidInput(request)
                return
            }
        }
        sendMessage(request, "Укажите второе число")
    }

    fun secondNumberInput(request: Request, calculation: Calculation) {
        val value = request.message.replace(",", ".").toBigDecimalOrNull()
        if (value == null) {
            invalidInput(request)
            return
        }
        calculation.secondNumber = value
        sendMessage(request, "Результат: \n<b>${calculation.result}</b>")
        userToCalculation.remove(request.userID)
        requireFirstNumber(request)
    }
}