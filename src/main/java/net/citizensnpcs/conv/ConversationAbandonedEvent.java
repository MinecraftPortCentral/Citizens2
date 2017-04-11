package net.citizensnpcs.conv;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

public class ConversationAbandonedEvent implements Event {

    private final ConversationContext context;
    private final Cause cause;

    public ConversationAbandonedEvent(ConversationContext context, Cause cause) {
        this.context = context;
        this.cause = cause;
    }

    public ConversationContext getContext() {
        return this.context;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

}
