package net.citizensnpcs.trait;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@TraitName("location")
public class CurrentLocation extends Trait {
    @Persist(value = "", required = true)
    private Location<World> location = new Location<World>(null, 0, 0, 0);

    public CurrentLocation() {
        super("location");
    }

    public Location<World> getLocation() {
        return location.getExtent() == null ? null : location;
    }

    @Override
    public void run() {
        if (!npc.isSpawned())
            return;
        location = npc.getEntity().getLocation();
    }

    public void setLocation(Location<World> loc) {
        this.location = loc;
    }

    @Override
    public String toString() {
        return "CurrentLocation{" + location + "}";
    }
}