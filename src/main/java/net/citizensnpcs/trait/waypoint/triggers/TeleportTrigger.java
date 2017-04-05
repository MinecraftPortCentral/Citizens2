package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class TeleportTrigger implements WaypointTrigger {
    @Persist(required = true)
    private Location<World> location;

    public TeleportTrigger() {
    }

    public TeleportTrigger(Location<World> location) {
        this.location = location;
    }

    @Override
    public String description() {
        return String.format("Teleport to [%s, %d, %d, %d]", location.getExtent().getName(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
    }

    @Override
    public void onWaypointReached(NPC npc, Location<World> waypoint) {
        if (location != null) {
            npc.teleport(waypoint, TeleportCause.PLUGIN);
        }
    }
}
