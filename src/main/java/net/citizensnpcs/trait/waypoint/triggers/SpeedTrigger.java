package net.citizensnpcs.trait.waypoint.triggers;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class SpeedTrigger implements WaypointTrigger {
    @Persist
    private float speed = 1F;

    public SpeedTrigger() {
    }

    public SpeedTrigger(float speed) {
        this.speed = speed;
    }

    @Override
    public String description() {
        return String.format("Speed change to %f", speed);
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public void onWaypointReached(NPC npc, Location<World> waypoint) {
        npc.getNavigator().getDefaultParameters().speedModifier(speed);
    }
}
