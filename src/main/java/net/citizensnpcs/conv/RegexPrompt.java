package net.citizensnpcs.conv;

import java.util.regex.Pattern;

public abstract class RegexPrompt extends ValidatingPrompt {

    private final Pattern pattern;

    public RegexPrompt(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    protected boolean isInputValid(ConversationContext context, String input) {
        return this.pattern.matcher(input).matches();
    }

    @Override
    public abstract String getPromptText(ConversationContext context);

}
