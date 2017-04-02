package org.spongepowered.api.conv;

import com.google.common.collect.Maps;
import org.spongepowered.api.command.CommandSource;

import java.util.Map;

public class ConversationContext {

    private final Map<String, Object> data = Maps.newHashMap();
    private final CommandSource source;

    public ConversationContext(CommandSource source) {
        this.source = source;
    }

    public void setSessionData(String key, Object value) {
        this.data.put(key, value);
    }

    public Object getSessionData(String key) {
        return this.data.get(key);
    }

    public CommandSource getForWhom() {
        return this.source;
    }

}
