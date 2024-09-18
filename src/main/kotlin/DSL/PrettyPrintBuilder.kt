package DSL

class PrettyPrintBuilder {
    private val content = StringBuilder()

    fun header(level: Int, block: HeaderBuilder.() -> Unit) {
        val builder = HeaderBuilder(level)
        builder.block()
        content.append(builder.toString())
    }

    fun text(block: TextBuilder.() -> Unit) {
        val builder = TextBuilder()
        builder.block()
        content.append(builder.toString())
    }

    override fun toString(): String = content.toString()
}