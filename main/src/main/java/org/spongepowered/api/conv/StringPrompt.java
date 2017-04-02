package org.spongepowered.api.conv;

public abstract class StringPrompt implements Prompt {

    public abstract Prompt acceptInput(ConversationContext context, String input);

    public abstract String getPromptText(ConversationContext context);

}
