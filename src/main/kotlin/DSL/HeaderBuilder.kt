package DSL

class HeaderBuilder(private val level: Int) {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append("#".repeat(level))
        content.append(" ")
        content.append(this)
        content.append("\n")
    }

    override fun toString(): String = content.toString()
}