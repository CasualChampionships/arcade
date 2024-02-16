package net.casual.arcade.datagen.language

public data class LanguageEntry(
    val key: String,
    val translation: String
) {
    override fun toString(): String {
        return """"${this.key}": "${this.translation}""""
    }

    public fun replaceInJson(json: String): String {
        val regex = Regex("""\Q"${this.key}"\E\s*:\s*"(\\.|[^"\\])*"""")
        return if (json.contains(regex)) {
            json.replace(regex, this.toString().replace("\\", "\\\\"))
        } else {
            val index = json.lastIndexOf('"')
            StringBuilder(json).insert(index + 1, ",\n  $this").toString()
        }
    }

    public companion object {
        public fun toJson(entries: Collection<LanguageEntry>): String {
            val builder = StringBuilder()
            builder.append("{\n")
            val iterator = entries.iterator()
            for (entry in iterator) {
                builder.append("  ").append(entry.toString())
                if (iterator.hasNext()) {
                    builder.append(",\n")
                }
            }
            builder.append("\n}")
            return builder.toString()
        }
    }
}