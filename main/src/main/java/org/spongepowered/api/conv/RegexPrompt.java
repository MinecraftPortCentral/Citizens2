package org.spongepowered.api.conv;

import java.util.regex.Pattern;

public abstract class RegexPrompt implements Prompt {

    public RegexPrompt(Pattern pattern) {
        // TODO Auto-generated constructor stub
    }

    protected abstract Prompt acceptValidatedInput(ConversationContext context, String input);

    public abstract String getPromptText(ConversationContext context);

}
