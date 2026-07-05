package com.example.data

data class TelegramSupportConfig(
    val link: String,
    val isActive: Boolean
) {
    fun serialize(): String {
        val sanitizedLink = link.replace(":", "").replace("\n", "").trim()
        return "$sanitizedLink::$isActive"
    }

    companion object {
        fun deserialize(str: String): TelegramSupportConfig? {
            val trimmed = str.trim()
            if (trimmed.isBlank()) return null
            val parts = trimmed.split("::")
            if (parts.size >= 2) {
                return TelegramSupportConfig(parts[0], parts[1].toBoolean())
            } else if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                // Backward compatibility or simpler format
                return TelegramSupportConfig(parts[0], true)
            }
            return null
        }
        
        fun parseList(raw: String): List<TelegramSupportConfig> {
            if (raw.isBlank()) return emptyList()
            return raw.split("\n").mapNotNull { deserialize(it) }
        }
        
        fun serializeList(list: List<TelegramSupportConfig>): String {
            return list.joinToString("\n") { it.serialize() }
        }
    }
}
