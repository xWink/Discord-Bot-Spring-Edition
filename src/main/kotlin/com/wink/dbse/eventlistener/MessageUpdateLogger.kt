package com.wink.dbse.eventlistener

import com.wink.dbse.entity.MessageEntity
import com.wink.dbse.property.ChannelIds
import com.wink.dbse.repository.MessageRepository
import com.wink.dbse.service.formatter.ILoggedMessageFormatter
import com.wink.dbse.service.messenger.IMessenger
import com.wink.dbse.service.converter.MessageConvertingService
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MessageUpdateLogger @Autowired constructor(
        private val repository: MessageRepository,
        private val formatter: ILoggedMessageFormatter,
        private val messenger: IMessenger,
        private val converter: MessageConvertingService,
        private val channels: ChannelIds
) : ListenerAdapter() {

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        val editedMessage: MessageEntity = converter.convert(event)
        val editedMessagesChannel: TextChannel? = event.guild.getTextChannelById(channels.editedMessages!!)
        if (editedMessagesChannel == null) {
            logger.warn("No such channel with editedMessages id. Removing ${this.javaClass.name} from event listeners.")
            event.jda.removeEventListener(this)
            return
        }

        val originalMessage: MessageEntity = repository.findFirstByMessageId(editedMessage.messageId) ?: return
        val originalContent: String = originalMessage.content + "\n" + originalMessage.attachment
        val channel: String? = event.guild.getTextChannelById(originalMessage.channelId)?.name
        val user: User = event.jda.getUserById(originalMessage.authorId) ?: return
        val message: String = formatter.format(originalMessage.timeSentSecs, channel, user.name, originalContent)

        messenger.sendMessage(editedMessagesChannel, message)
        repository.updateByMessageId(editedMessage.messageId, editedMessage.timeSentSecs, editedMessage.content)
        logger.info("Successfully logged an edited message by user \"${user.name}\" in channel \"$channel\"")
    }

    private companion object {
        @JvmStatic private val logger: Logger = LoggerFactory.getLogger(MessageUpdateLogger::class.java)
    }
}