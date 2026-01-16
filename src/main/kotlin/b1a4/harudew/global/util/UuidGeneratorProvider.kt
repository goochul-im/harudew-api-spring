package b1a4.harudew.global.util

import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UuidGeneratorProvider() : UuidGenerator {

    override fun generate(): UUID {
        return UUID.randomUUID()
    }

}
