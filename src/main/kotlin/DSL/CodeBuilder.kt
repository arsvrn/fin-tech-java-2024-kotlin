package DSL

class CodeBuilder(private val language: ProgrammingLanguage) {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append("```${language.name.lowercase()}\n")
        content.append(this)
        content.append("\n```")
    }

    override fun toString(): String = content.toString()
}