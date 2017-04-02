package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.waypoint.Waypoint;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.conv.Conversation;
import org.spongepowered.api.conv.ConversationContext;
import org.spongepowered.api.conv.ConversationFactory;
import org.spongepowered.api.conv.Prompt;
import org.spongepowered.api.conv.StringPrompt;
import org.spongepowered.api.entity.living.player.Player;

public class TriggerEditPrompt extends StringPrompt {
    private final WaypointEditor editor;

    public TriggerEditPrompt(WaypointEditor editor) {
        this.editor = editor;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        input = input.toLowerCase().trim();
        if (input.contains("add")) {
            context.setSessionData("said", false);
            return new TriggerAddPrompt(editor);
        }
        if (input.contains("remove")) {
            context.setSessionData("said", false);
            return new TriggerRemovePrompt(editor);
        }
        return this;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        context.setSessionData("previous", this);
        if (context.getSessionData("said") == Boolean.TRUE)
            return "";
        context.setSessionData("said", true);
        String base = Messaging.tr(Messages.WAYPOINT_TRIGGER_EDITOR_PROMPT);
        if (editor.getCurrentWaypoint() != null) {
            Waypoint waypoint = editor.getCurrentWaypoint();
            for (WaypointTrigger trigger : waypoint.getTriggers()) {
                base += "\n    - " + trigger.description();
            }
        }
        Messaging.send((CommandSource) context.getForWhom(), base);
        return "";
    }

    public static Conversation start(Player player, WaypointEditor editor) {
        final Conversation conversation = new ConversationFactory(CitizensAPI.getPlugin()).withLocalEcho(false)
                .withEscapeSequence("exit").withEscapeSequence("triggers").withEscapeSequence("/npc path")
                .withModality(false).withFirstPrompt(new TriggerEditPrompt(editor)).buildConversation(player);
        conversation.begin();
        return conversation;
    }
}
