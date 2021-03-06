package com.wink.dbse.service

import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.utils.AttachmentOption
import org.springframework.stereotype.Service

interface Messenger {

    /**
     * Sends a given message to a given channel. If the message length exceeds the maximum allowed by Discord (2000),
     * the message is broken up into multiple messages which are all sent in order.
     * @param channel the channel the message should be sent to
     * @param message the message to send
     */
    fun sendMessage(channel: MessageChannel, message: String)

    /**
     * Sends a given message to a given channel. If the message length exceeds the maximum allowed by Discord (2000),
     * the message is broken up into multiple messages which are all sent in order.
     * @param channel the channel the message should be sent to
     * @param embed the embeded message to send
     */
    fun sendMessage(channel: MessageChannel, embed: MessageEmbed)

    /**
     * Sends a given message with a given attachment to a given channel. If the message length exceeds the maximum
     * allowed by Discord (2000), the message is broken up into multiple messages which are all sent in order.
     * The attachment is always sent with the last message.
     * @param channel the channel the message should be sent to
     * @param message the message to send
     * @param attachment the attachment to include in the message
     * @param attachmentName the name of the attachment
     * @param options options to configure the attachment
     */
    fun sendMessage(channel: MessageChannel, message: String, attachment: ByteArray, attachmentName: String, vararg options: AttachmentOption)
}

@Service
class MessagingService : Messenger {

    companion object {
        private const val MAX_MESSAGE_LENGTH = 2000
    }

    /**
     * Sends a given message to a given channel. If the message length exceeds the maximum allowed by Discord (2000),
     * the message is broken up into multiple messages which are all sent in order.
     * @param channel the channel the message should be sent to
     * @param message the message to send
     */
    override fun sendMessage(channel: MessageChannel, message: String) {
        message.chunked(MAX_MESSAGE_LENGTH).forEach { channel.sendMessage(it).queue() }
    }

    /**
     * Sends a given message to a given channel. If the message length exceeds the maximum allowed by Discord (2000),
     * the message is broken up into multiple messages which are all sent in order.
     * @param channel the channel the message should be sent to
     * @param embed the embedded message to send
     */
    override fun sendMessage(channel: MessageChannel, embed: MessageEmbed) {
        channel.sendMessage(embed).queue()
    }

    /**
     * Sends a given message with a given attachment to a given channel. If the message length exceeds the maximum
     * allowed by Discord (2000), the message is broken up into multiple messages which are all sent in order.
     * The attachment is always sent with the last message.
     * @param channel the channel the message should be sent to
     * @param message the message to send
     * @param attachment the attachment to include in the message
     * @param attachmentName the name of the attachment
     * @param options settings to configure the attachment
     */
    override fun sendMessage(channel: MessageChannel, message: String, attachment: ByteArray, attachmentName: String, vararg options: AttachmentOption) {
        val messages = message.chunked(MAX_MESSAGE_LENGTH)
        messages.subList(0, messages.lastIndex).forEach { channel.sendMessage(it).queue() }
        channel.sendMessage(messages.last()).addFile(attachment, attachmentName, *options).queue()
    }
}
