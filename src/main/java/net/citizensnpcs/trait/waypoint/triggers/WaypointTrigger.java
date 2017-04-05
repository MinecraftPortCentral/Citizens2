package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.npc.NPC;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface WaypointTrigger {
    String description();

    void onWaypointReached(NPC npc, Location<World> waypoint);
}
