package by.derovi.labcalculator

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class Bot : TelegramLongPollingBot() {

    @Value("Lab Directory")
    lateinit var botUsernameValue: String

    @Value("\${bot.token}")
    lateinit var botTokenValue: String

    @Autowired
    lateinit var calculationService: CalculationService

    override fun onUpdateReceived(update: Update) {
        try {
            if (update.hasMessage() || update.hasCallbackQuery()) {
                val (from, text, chatId) = if (update.hasMessage())
                    Triple(update.message.from, update.message.text, update.message.chatId)
                else
                    Triple(update.callbackQuery.from, update.callbackQuery.data, update.callbackQuery.message.chatId)

                val request = Request(from.id, text, chatId, this)
                calculationService.update(request)
            }

        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
    }

    override fun getBotToken(): String {
        return botTokenValue
    }

    override fun getBotUsername(): String {
        return botUsernameValue
    }
}