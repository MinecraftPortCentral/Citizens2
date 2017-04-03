package net.citizensnpcs;

import net.citizensnpcs.api.event.NPCEvent;
import net.citizensnpcs.api.npc.NPC;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class NPCNeedsRespawnEvent extends NPCEvent {
    private final Location<World> spawn;

    public NPCNeedsRespawnEvent(NPC npc, Location<World> at) {
        super(npc, Cause.of(NamedCause.source(npc)));
        this.spawn = at;
    }

    public Location<World> getSpawnLocation() {
        return spawn;
    }
}
