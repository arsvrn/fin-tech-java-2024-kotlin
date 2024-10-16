package DSL

class TextBuilder {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append(this)
        content.append("\n")
    }

    fun bold(text: String) = "<b>$text</b>"
    fun underlined(text: String) = "<u>$text</u>"
    fun link(link: String, text: String) = "<a href=\"$link\">$text</a>"

    fun code(language: ProgrammingLanguage, block: CodeBuilder.() -> Unit) {
        val builder = CodeBuilder(language)
        builder.block()
        content.append(builder.toString())
    }

    override fun toString(): String = content.toString()
}