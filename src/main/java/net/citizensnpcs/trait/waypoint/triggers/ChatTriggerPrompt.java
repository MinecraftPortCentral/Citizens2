package net.citizensnpcs.trait.waypoint.triggers;

import com.google.common.collect.Lists;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.conv.ConversationContext;
import net.citizensnpcs.conv.Prompt;
import net.citizensnpcs.conv.StringPrompt;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.command.CommandSource;

import java.util.List;

public class ChatTriggerPrompt extends StringPrompt implements WaypointTriggerPrompt {
    private final List<String> lines = Lists.newArrayList();
    private double radius = -1;

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (input.equalsIgnoreCase("back"))
            return (Prompt) context.getSessionData("previous");
        if (input.startsWith("radius")) {
            try {
                radius = Double.parseDouble(input.split(" ")[1]);
            } catch (NumberFormatException e) {
                Messaging.sendErrorTr((CommandSource) context.getForWhom(),
                        Messages.WAYPOINT_TRIGGER_CHAT_INVALID_RADIUS);
            } catch (IndexOutOfBoundsException e) {
                Messaging.sendErrorTr((CommandSource) context.getForWhom(), Messages.WAYPOINT_TRIGGER_CHAT_NO_RADIUS);
            }
            return this;
        }
        if (input.equalsIgnoreCase("finish")) {
            context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new ChatTrigger(radius, lines));
            return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
        }
        lines.add(input);
        return this;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        Messaging.sendTr((CommandSource) context.getForWhom(), Messages.CHAT_TRIGGER_PROMPT);
        return "";
    }
}
