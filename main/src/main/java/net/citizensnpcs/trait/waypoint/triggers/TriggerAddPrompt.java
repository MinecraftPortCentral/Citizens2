package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.conv.ConversationContext;
import org.spongepowered.api.conv.Prompt;
import org.spongepowered.api.conv.StringPrompt;

public class TriggerAddPrompt extends StringPrompt {
    private final WaypointEditor editor;

    public TriggerAddPrompt(WaypointEditor editor) {
        this.editor = editor;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        input = input.toLowerCase().trim();
        if (input.equalsIgnoreCase("back")) {
            context.setSessionData("said", false);
            return (Prompt) context.getSessionData("previous");
        }
        Prompt prompt = WaypointTriggerRegistry.getTriggerPromptFrom(input);
        if (prompt == null) {
            Messaging.sendErrorTr((CommandSource) context.getForWhom(),
                    Messages.WAYPOINT_TRIGGER_EDITOR_INVALID_TRIGGER, input);
            context.setSessionData("said", false);
            return this;
        }
        return prompt;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        WaypointTrigger returned = (WaypointTrigger) context.getSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY);
        if (returned != null) {
            if (editor.getCurrentWaypoint() != null) {
                editor.getCurrentWaypoint().addTrigger(returned);
                context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, null);
                Messaging.sendTr((CommandSource) context.getForWhom(), Messages.WAYPOINT_TRIGGER_ADDED_SUCCESSFULLY,
                        returned.description());
            } else {
                Messaging.sendErrorTr((CommandSource) context.getForWhom(), Messages.WAYPOINT_TRIGGER_EDITOR_INACTIVE);
            }
        }
        if (context.getSessionData("said") == Boolean.TRUE)
            return "";
        context.setSessionData("said", true);
        context.setSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY, this);
        return Messaging.tr(Messages.WAYPOINT_TRIGGER_ADD_PROMPT, WaypointTriggerRegistry.describeValidTriggerNames());
    }
}
