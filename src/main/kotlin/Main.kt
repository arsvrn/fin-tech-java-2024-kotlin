import DSL.ProgrammingLanguage
import DSL.readme
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("PrettyPrintLogger")

fun main() {
    try {
        readme {
            header(level = 1) { +"Kotlin Lecture" }
            header(level = 2) { +"DSL" }

            text {
                +("Today we will try to recreate ${bold("DSL")} from this article: ${
                    link(
                        link = "https://kotlinlang.org/docs/type-safe-builders.html",
                        text = "Kotlin Docs"
                    )
                }!!!")
                +"It is so ${underlined("fascinating and interesting")}!"
                code(language = ProgrammingLanguage.KOTLIN) {
                    +"""
                        fun main() {
                            println("Hello world!")
                        }
                    """.trimIndent()
                }
            }
        }
    } catch (e: Exception) {
        logger.error("An error occurred while generating the documentation: ${e.message}", e)
        throw e
    }
}

