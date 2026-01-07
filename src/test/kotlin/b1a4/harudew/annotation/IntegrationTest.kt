package b1a4.harudew.annotation

import org.junit.jupiter.api.Tag

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Tag("integration") // JUnit 태그 포함
annotation class IntegrationTest()
