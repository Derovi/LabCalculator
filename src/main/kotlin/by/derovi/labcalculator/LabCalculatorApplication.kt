package by.derovi.labcalculator

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication(proxyBeanMethods = true)
@EnableScheduling
class BotP2pApplication : CommandLineRunner {
    override fun run(vararg args: String?) {
        Thread.currentThread().join()
    }
}

fun main(args: Array<String>) {
    runApplication<BotP2pApplication>(*args)
}
