package org.spongepowered.api.conv;

import org.spongepowered.api.event.Event;

public interface ConversationAbandonedEvent extends Event {

    ConversationContext getContext();

}
