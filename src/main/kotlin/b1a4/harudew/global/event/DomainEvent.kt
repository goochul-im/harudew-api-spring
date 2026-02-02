package b1a4.harudew.global.event

import java.time.Instant
import java.util.UUID

abstract class DomainEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val createdAt: Instant = Instant.now(),
    val aggregateType: String,
    val aggregateId: String
)
