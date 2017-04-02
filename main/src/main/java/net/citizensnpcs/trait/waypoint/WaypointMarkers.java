package net.citizensnpcs.trait.waypoint;

import java.util.Map;

import com.google.common.collect.Maps;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class WaypointMarkers {
    private final Map<Waypoint, Entity> waypointMarkers = Maps.newHashMap();
    private final World world;

    public WaypointMarkers(World world) {
        this.world = world;
    }

    public Entity createWaypointMarker(Waypoint waypoint) {
        Entity entity = spawnMarker(world, waypoint.getLocation().add(0, 1, 0));
        if (entity == null)
            return null;
        waypointMarkers.put(waypoint, entity);
        return entity;
    }

    public void destroyWaypointMarkers() {
        for (Entity entity : waypointMarkers.values()) {
            entity.remove();
        }
        waypointMarkers.clear();
    }

    public void removeWaypointMarker(Waypoint waypoint) {
        Entity entity = waypointMarkers.remove(waypoint);
        if (entity != null) {
            entity.remove();
        }
    }

    public Entity spawnMarker(World world, Location<World> at) {
        NPC npc = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore()).createNPC(EntityTypes.ENDER_SIGNAL,
                "");
        npc.spawn(at);
        return npc.getEntity();
    }
}