package net.citizensnpcs.trait.text;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.conv.ConversationContext;
import net.citizensnpcs.conv.NumericPrompt;
import net.citizensnpcs.conv.Prompt;
import net.citizensnpcs.util.Messages;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.entity.living.player.Player;

public class PageChangePrompt extends NumericPrompt {
    private final Text text;

    public PageChangePrompt(Text text) {
        this.text = text;
    }

    @Override
    public Prompt acceptValidatedInput(ConversationContext context, Number input) {
        Player player = (Player) context.getForWhom();
        if (!text.sendPage(player, input.intValue())) {
            Messaging.sendErrorTr(player, Messages.TEXT_EDITOR_INVALID_PAGE);
            return new TextStartPrompt(text);
        }
        return (Prompt) context.getSessionData("previous");
    }

    @Override
    public String getFailedValidationText(ConversationContext context, String input) {
        return TextFormatting.RED + Messaging.tr(Messages.TEXT_EDITOR_INVALID_PAGE);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return Messaging.tr(Messages.TEXT_EDITOR_PAGE_PROMPT);
    }
}