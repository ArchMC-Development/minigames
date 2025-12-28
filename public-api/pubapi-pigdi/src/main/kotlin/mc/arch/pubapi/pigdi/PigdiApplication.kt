package mc.arch.pubapi.pigdi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * PIGDI - Public Immutable Game Data Interface
 * Spring Boot REST API for accessing game statistics.
 *
 * @author Subham
 * @since 12/27/24
 */
@SpringBootApplication
@EnableScheduling
class PigdiApplication

fun main(args: Array<String>)
{
    runApplication<PigdiApplication>(*args)
}
