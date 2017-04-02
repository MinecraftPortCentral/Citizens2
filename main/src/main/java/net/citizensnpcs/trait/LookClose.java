package net.citizensnpcs.trait;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandConfigurable;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.util.Util;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@TraitName("lookclose")
public class LookClose extends Trait implements Toggleable, CommandConfigurable {
    private boolean enabled = Setting.DEFAULT_LOOK_CLOSE.asBoolean();
    private Player lookingAt;
    private double range = Setting.DEFAULT_LOOK_CLOSE_RANGE.asDouble();
    private boolean realisticLooking = Setting.DEFAULT_REALISTIC_LOOKING.asBoolean();

    public LookClose() {
        super("lookclose");
    }

    private boolean canSeeTarget() {
        return realisticLooking && npc.getEntity() instanceof Living
                ? ((Living) npc.getEntity()).hasLineOfSight(lookingAt) : true;
    }

    @Override
    public void configure(CommandContext args) {
        range = args.getFlagDouble("range", range);
        range = args.getFlagDouble("r", range);
        realisticLooking = args.hasFlag('r');
    }

    private void findNewTarget() {
        List<Entity> nearby = npc.getEntity().getNearbyEntities(range);
        Collections.sort(nearby, new Comparator<Entity>() {
            @Override
            public int compare(Entity o1, Entity o2) {
                Location<World> l1 = o1.getLocation();
                Location<World> l2 = o2.getLocation();
                if (!NPC_LOCATION.getExtent().equals(l1.getExtent()) || !NPC_LOCATION.getExtent().equals(l2.getExtent())) {
                    return -1;
                }
                return Double.compare(l1.getPosition().distanceSquared(NPC_LOCATION.getPosition()), l2.getPosition().distanceSquared(NPC_LOCATION.getPosition()));
            }
        });
        for (Entity entity : nearby) {
            if (entity.getType() != EntityType.PLAYER || ((Player) entity).getGameMode() == GameMode.SPECTATOR
                    || entity.getLocation(CACHE_LOCATION).getWorld() != NPC_LOCATION.getWorld()
                    || CitizensAPI.getNPCRegistry().getNPC(entity) != null)
                continue;
            lookingAt = (Player) entity;
            return;
        }
    }

    private boolean hasInvalidTarget() {
        if (lookingAt == null)
            return true;
        if (!lookingAt.isOnline() || lookingAt.getWorld() != npc.getEntity().getWorld()
                || lookingAt.getLocation().getPosition().distanceSquared(NPC_LOCATION.getPosition()) > range) {
            lookingAt = null;
        }
        return lookingAt == null;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        enabled = key.getBoolean("enabled", true);
        range = key.getDouble("range", range);
        realisticLooking = key.getBoolean("realisticlooking", key.getBoolean("realistic-looking"));
    }

    public void lookClose(boolean lookClose) {
        enabled = lookClose;
    }

    @Override
    public void onDespawn() {
        lookingAt = null;
    }

    @Override
    public void run() {
        if (!enabled || !npc.isSpawned() || npc.getNavigator().isNavigating())
            return;
        npc.getEntity().getLocation(NPC_LOCATION);
        if (hasInvalidTarget()) {
            findNewTarget();
        }
        if (lookingAt != null && canSeeTarget()) {
            Util.faceEntity(npc.getEntity(), lookingAt);
        }
    }

    @Override
    public void save(DataKey key) {
        key.setBoolean("enabled", enabled);
        key.setDouble("range", range);
        key.setBoolean("realisticlooking", realisticLooking);
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setRealisticLooking(boolean realistic) {
        this.realisticLooking = realistic;
    }

    @Override
    public boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    @Override
    public String toString() {
        return "LookClose{" + enabled + "}";
    }

    private static final Location<World> CACHE_LOCATION = new Location<World>(null, 0, 0, 0);
    private static final Location<World> CACHE_LOCATION2 = new Location<World>(null, 0, 0, 0);
    private static final Location<World> NPC_LOCATION = new Location<World>(null, 0, 0, 0);
    private static final Location<World> PLAYER_LOCATION = new Location<World>(null, 0, 0, 0);
}