package b1a4.harudew

import b1a4.harudew.emotion.domain.EmotionType
import b1a4.harudew.emotion.domain.toEmotionGroup
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HarudewApiApplication

fun main(args: Array<String>) {
    runApplication<HarudewApiApplication>(*args)
}
