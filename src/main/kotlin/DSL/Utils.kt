package DSL

import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("PrettyPrintLogger")
fun readme(block: PrettyPrintBuilder.() -> Unit) {
    val builder = PrettyPrintBuilder()
    try {
        builder.block()
        println(builder.toString())
    } catch (e: Exception) {
        logger.error("An error occurred while building the documentation: ${e.message}", e)
        throw e
    }
}