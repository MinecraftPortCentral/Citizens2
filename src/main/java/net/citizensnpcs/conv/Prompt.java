package net.citizensnpcs.conv;

public interface Prompt {

    String getPromptText(ConversationContext context);

    Prompt acceptInput(ConversationContext context, String input);

}
