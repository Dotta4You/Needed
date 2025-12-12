package de.doetchen.project.core.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoRegister(
    val module: String = "",
    val priority: Int = 0
)

