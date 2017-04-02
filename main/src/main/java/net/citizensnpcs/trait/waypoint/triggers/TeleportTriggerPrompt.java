package net.citizensnpcs.trait.waypoint.triggers;

import java.util.regex.Pattern;

import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class TeleportTriggerPrompt extends RegexPrompt implements WaypointTriggerPrompt {
    public TeleportTriggerPrompt() {
        super(PATTERN);
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        input = input.trim();
        if (input.equalsIgnoreCase("back"))
            return (Prompt) context.getSessionData("previous");
        if (input.equalsIgnoreCase("here")) {
            Player player = (Player) context.getForWhom();
            context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY,
                    new TeleportTrigger(player.getLocation()));
            return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
        }
        String[] parts = Iterables.toArray(Splitter.on(':').split(input), String.class);
        String worldName = parts[0];
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Messaging.sendErrorTr((CommandSource) context.getForWhom(), Messages.WORLD_NOT_FOUND);
            return this;
        }
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);

        context.setSessionData(WaypointTriggerPrompt.CREATED_TRIGGER_KEY, new Location<World>(world, x, y, z));
        return (Prompt) context.getSessionData(WaypointTriggerPrompt.RETURN_PROMPT_KEY);
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return Messaging.tr(Messages.WAYPOINT_TRIGGER_TELEPORT_PROMPT);
    }

    private static final Pattern PATTERN = Pattern.compile("here|back|[\\p{L}]+?:[0-9]+?:[0-9]+?:[0-9]+?",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
}
