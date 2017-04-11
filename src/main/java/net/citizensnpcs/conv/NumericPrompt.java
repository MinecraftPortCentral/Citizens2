package net.citizensnpcs.conv;

import org.apache.commons.lang3.math.NumberUtils;

public abstract class NumericPrompt extends ValidatingPrompt {

    @Override
    protected boolean isInputValid(ConversationContext context, String input) {
        return NumberUtils.isNumber(input);
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        try {
            return acceptValidatedInput(context, NumberUtils.createNumber(input));
        } catch (NumberFormatException e) {
            return acceptValidatedInput(context, 0);
        }
    }

    protected abstract Prompt acceptValidatedInput(ConversationContext context, Number createNumber);

}
