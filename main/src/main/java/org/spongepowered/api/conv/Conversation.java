package org.spongepowered.api.conv;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Conversation {

    CommandSource source;
    private PluginContainer plugin;
    private boolean echo;
    private ImmutableSet<String> escapes;
    private boolean modal;
    private Prompt firstPrompt;
    private MessageChannel oldChannel;
    private MessageChannel suppressChannel;
    private ConversationContext context;
    private EventListeners eventListeners;
    private Prompt currentPrompt;
    private ImmutableList<EventListener<ConversationAbandonedEvent>> abandonListeners;

    Conversation(Builder builder, CommandSource source) {
        this.source = source;
        this.plugin = builder.plugin;
        this.echo = builder.echo;
        this.escapes = ImmutableSet.copyOf(builder.escapes);
        this.modal = builder.modal;
        this.firstPrompt = builder.firstPrompt;
        this.eventListeners = new EventListeners();
        this.abandonListeners = ImmutableList.copyOf(builder.abandonListeners);
    }

    void onTextEntered(String message) {
        if (this.echo) {
            send(message);
        }
        if (this.escapes.contains(message)) {
            abandon();
            return;
        }
        this.currentPrompt = this.currentPrompt.acceptInput(this.context, message);
        if (this.currentPrompt == null) {
            abandon();
        } else {
            sendPrompt(this.currentPrompt);
        }
    }

    private void send(String message) {
        this.source.sendMessage(Text.of(message));
    }

    public void abandon() {
        Sponge.getEventManager().unregisterListeners(this.eventListeners);
        ConversationAbandonedEvent event = new ConversationAbandonedEvent(this.context, Cause.source(this.plugin).build());
        for (EventListener<ConversationAbandonedEvent> listener : this.abandonListeners) {
            try {
                listener.handle(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.modal) {
            this.source.setMessageChannel(this.oldChannel);
        }
        this.context = null;
        this.currentPrompt = null;
    }

    public void begin() {
        checkState(this.context == null);
        if (this.modal) {
            if (this.suppressChannel == null) {
                this.suppressChannel = createSuppressChannel();
            }
            this.oldChannel = this.source.getMessageChannel();
            this.source.setMessageChannel(this.suppressChannel);
        }
        Sponge.getEventManager().registerListeners(this.plugin, this.eventListeners);
        this.context = new ConversationContext(this.source);
        sendPrompt(this.currentPrompt = this.firstPrompt);
    }

    private void sendPrompt(Prompt prompt) {
        send(prompt.getPromptText(this.context));
    }

    private MessageChannel createSuppressChannel() {
        return new MessageChannel() {

            @Override
            public Collection<MessageReceiver> getMembers() {
                return Collections.singleton(Conversation.this.source);
            }
            @Override
            public Optional<Text> transformMessage(Object sender, MessageReceiver recipient, Text original, ChatType type) {
                // TODO Auto-generated method stub
                return MessageChannel.super.transformMessage(sender, recipient, original, type);
            }
        };
    }

    private class EventListeners {

        EventListeners() {
        }

        @Listener(order = Order.FIRST)
        public void onPlayerChat(MessageChannelEvent.Chat event, @Root Player player) {
            if (player.equals(Conversation.this.source)) {
                onTextEntered(event.getMessage().toPlain());
                event.setCancelled(true);
                event.setMessageCancelled(true);
            }
        }

        @Listener(order = Order.FIRST)
        public void onCommand(SendCommandEvent event, @Root Player player) {
            if (player.equals(Conversation.this.source)) {
                onTextEntered(event.getCommand() + (event.getArguments().isEmpty() ? "" : " " + event.getArguments()));
                event.setCancelled(true);
            }
        }
    }

    public static Builder builder(PluginContainer plugin) {
        return new Builder(plugin);
    }

    public static class Builder {

        PluginContainer plugin;
        boolean echo;
        Set<String> escapes = Sets.newHashSet();
        boolean modal;
        Prompt firstPrompt;
        List<EventListener<ConversationAbandonedEvent>> abandonListeners = Lists.newArrayList();

        Builder(PluginContainer plugin) {
            this.plugin = plugin;
        }

        public Builder localEcho(boolean echo) {
            this.echo = echo;
            return this;
        }

        public Builder escapeSequence(String escape) {
            this.escapes.add(escape);
            return this;
        }

        public Builder modality(boolean modal) {
            this.modal = modal;
            return this;
        }

        public Builder firstPrompt(Prompt prompt) {
            this.firstPrompt = prompt;
            return this;
        }

        public Builder addConversationAbandonedListener(EventListener<ConversationAbandonedEvent> listener) {
            this.abandonListeners.add(listener);
            return this;
        }

        public Conversation build(CommandSource source) {
            checkState(this.firstPrompt != null);
            return new Conversation(this, source);
        }

    }
}
