package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.conv.ConversationContext;
import org.spongepowered.api.conv.Prompt;
import org.spongepowered.api.conv.StringPrompt;

public class TextEditPrompt extends StringPrompt {
    private final Text text;

    public TextEditPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        int index = (Integer) context.getSessionData("index");
        text.edit(index, input);
        Messaging.sendTr((CommandSource) context.getForWhom(), Messages.TEXT_EDITOR_EDITED_TEXT, index, input);
        return new TextStartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return TextFormatting.GREEN + Messaging.tr(Messages.TEXT_EDITOR_EDIT_PROMPT);
    }
}