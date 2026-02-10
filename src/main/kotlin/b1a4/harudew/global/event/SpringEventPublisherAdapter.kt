package b1a4.harudew.global.event

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringEventPublisherAdapter(
    private val eventPublisher: ApplicationEventPublisher
) : DomainEventPublisherPort {

    override fun publish(event: DomainEvent) {
        eventPublisher.publishEvent(event)
    }

}
