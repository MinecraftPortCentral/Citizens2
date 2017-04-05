package org.spongepowered.api.conv;

import org.spongepowered.api.text.Text;

public abstract class ValidatingPrompt implements Prompt {

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (isInputValid(context, input)) {
            return acceptValidatedInput(context, input);
        }
        String message = getFailedValidationText(context, input);
        if (message != null) {
            context.getForWhom().sendMessage(Text.of(message));
        }
        return this;
    }

    protected abstract boolean isInputValid(ConversationContext context, String input);

    protected abstract Prompt acceptValidatedInput(ConversationContext context, String input);

    protected String getFailedValidationText(ConversationContext context, String input) {
        return null;
    }
}
