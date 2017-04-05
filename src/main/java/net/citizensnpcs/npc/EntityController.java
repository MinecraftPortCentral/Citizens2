package net.citizensnpcs.npc;

import net.citizensnpcs.api.npc.NPC;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface EntityController {
    Entity getBukkitEntity();

    void remove();

    void spawn(Location<World> at, NPC npc);
}
