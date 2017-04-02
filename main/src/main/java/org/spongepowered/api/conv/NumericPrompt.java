package org.spongepowered.api.conv;

public abstract class NumericPrompt implements Prompt {

    public Prompt acceptInput(ConversationContext context, String input) {
        // TODO Auto-generated method stub
        return null;
    }

    protected abstract Prompt acceptValidatedInput(ConversationContext context, Number input);

    protected String getFailedValidationText(ConversationContext context, String input) {
        return null;
    }

    public abstract String getPromptText(ConversationContext context);

}
