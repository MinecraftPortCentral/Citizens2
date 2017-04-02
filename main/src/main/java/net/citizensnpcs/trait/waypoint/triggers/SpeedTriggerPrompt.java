package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.conv.ConversationContext;
import org.spongepowered.api.conv.NumericPrompt;
import org.spongepowered.api.conv.Prompt;

public class SpeedTriggerPrompt extends NumericPrompt implements WaypointTriggerPrompt {
    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        float speed = (float) Math.max(input.doubleValue(), 0);
        context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new SpeedTrigger(speed));
        return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return Messaging.tr(Messages.SPEED_TRIGGER_PROMPT);
    }
}
