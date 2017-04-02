package org.spongepowered.api.conv;

import org.spongepowered.api.command.CommandSource;

public interface ConversationContext {

    void setSessionData(String string, Object obj);

    Object getSessionData(String string);

    CommandSource getForWhom();

}
