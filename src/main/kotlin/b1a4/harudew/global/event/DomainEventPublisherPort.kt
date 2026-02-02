package b1a4.harudew.global.event

interface DomainEventPublisherPort {

    fun publish(event: DomainEvent)

}
