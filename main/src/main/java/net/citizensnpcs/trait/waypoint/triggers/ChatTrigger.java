package net.citizensnpcs.trait.waypoint.triggers;

import java.util.Collection;
import java.util.List;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.util.Messaging;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ChatTrigger implements WaypointTrigger {
    @Persist(required = true)
    private List<String> lines;
    @Persist
    private double radius = -1;

    public ChatTrigger() {
    }

    public ChatTrigger(double radius, Collection<String> chatLines) {
        this.radius = radius;
        lines = Lists.newArrayList(chatLines);
    }

    @Override
    public String description() {
        return String.format("Chat Trigger [radius %d, %s]", radius, Joiner.on(", ").join(lines));
    }

    @Override
    public void onWaypointReached(NPC npc, Location<World> waypoint) {
        if (radius < 0) {
            for (Player player : npc.getEntity().getWorld().getPlayers()) {
                for (String line : lines)
                    Messaging.send(player, line);
            }
        } else {
            for (Entity entity : npc.getEntity().getNearbyEntities(radius)) {
                if (!(entity instanceof Player))
                    continue;
                for (String line : lines)
                    Messaging.send((Player) entity, line);
            }
        }
    }
}
