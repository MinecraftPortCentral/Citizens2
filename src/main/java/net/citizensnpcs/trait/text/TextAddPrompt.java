package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.conv.ConversationContext;
import net.citizensnpcs.conv.Prompt;
import net.citizensnpcs.conv.StringPrompt;
import net.citizensnpcs.util.Messages;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.entity.living.player.Player;

public class TextAddPrompt extends StringPrompt {
    private final Text text;

    public TextAddPrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        text.add(input);
        Messaging.sendTr((Player) context.getForWhom(), Messages.TEXT_EDITOR_ADDED_ENTRY, input);
        return new TextStartPrompt(text);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return TextFormatting.GREEN + Messaging.tr(Messages.TEXT_EDITOR_ADD_PROMPT);
    }
}