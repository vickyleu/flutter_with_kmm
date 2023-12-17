import java.io.File

class CocoapodsAppender {
    private constructor()
    class Builder(private val file: File) {
        private var lines: MutableList<String>
        init {
            if (!file.exists()) {
                throw IllegalArgumentException("file not exist")
            }
            lines = file.readText().lines().toMutableList()
        }
        fun build(): String {
            return lines.joinToString("\n")
        }

        fun append(searchBy: String, appendText: String): Builder {
            var index = lines.indexOfFirst { it.contains(searchBy) }
            lines.add(index+1,appendText)
            return this
        }
        fun replace(searchBy: String, replaceText: String): Builder {
            var index = lines.indexOfFirst { it.contains(searchBy) }
            lines[index] =   replaceText
            return this
        }
    }
}