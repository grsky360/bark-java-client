package ilio.bark

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Push(
    @SerialName("device_key")
    val device: String,

    var title: String = "Bark",

    val body: String = "",
//    val category: String,

    @Serializable(with = Level.LevelSerializer::class)
    val level: Level? = null,
    val badge: Int? = null,

    @Serializable(with = SerializeBooleanToInt::class)
    val automaticallyCopy: Boolean? = null,
    val copy: String? = null,
    val sound: String? = null,
    val icon: String? = null,
    val group: String? = null,

    @Serializable(with = SerializeBooleanToString::class)
    val isArchive: Boolean? = null,
    val url: String? = null
) {

    fun validate() {
        if (device.isEmpty()) {
            throw IllegalArgumentException("device_key is empty")
        }
    }

    enum class Level(val code: String) {
        ACTIVE("active"), TIME_SENSITIVE("timeSensitive"), PASSIVE("passive");

        class LevelSerializer : KSerializer<Level?> {
            override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LevelSerializer", PrimitiveKind.STRING)
            override fun serialize(encoder: Encoder, value: Level?) = wrapUnit { value?.let { encoder.encodeString(it.code) } }
            override fun deserialize(decoder: Decoder): Level? = values().firstOrNull { it.code == decoder.decodeString() }
        }
    }

    class SerializeBooleanToInt : KSerializer<Boolean> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SerializeBooleanToInt", PrimitiveKind.INT)
        override fun serialize(encoder: Encoder, value: Boolean) = wrapUnit { value.doIf { encoder.encodeInt(1) }.doElse { encoder.encodeNull() } }
        override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() == 1
    }

    class SerializeBooleanToString : KSerializer<Boolean> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("SerializeBooleanToString", PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, value: Boolean) = wrapUnit { value.doIf { encoder.encodeString("1") }.doElse { encoder.encodeNull() } }
        override fun deserialize(decoder: Decoder): Boolean = decoder.decodeString() == "1"
    }
}
