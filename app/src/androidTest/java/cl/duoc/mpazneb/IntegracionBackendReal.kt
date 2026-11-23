package cl.duoc.mpazneb

/**
 * Marca pruebas instrumentadas que requieren Spring Boot en el PC.
 * Excluidas por defecto de `connectedDebugAndroidTest` vía `notAnnotation` en Gradle.
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class IntegracionBackendReal