package net.citizensnpcs.util;

import java.util.Collection;
import java.util.Random;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import net.citizensnpcs.api.event.NPCCollisionEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.property.entity.EyeLocationProperty;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Util {
    // Static class for small (emphasis small) utility methods
    private Util() {
    }

    public static void assumePose(Entity entity, float yaw, float pitch) {
        NMS.look(entity, yaw, pitch);
    }

    public static void callCollisionEvent(NPC npc, Entity entity) {
        Sponge.getEventManager().post(new NPCCollisionEvent(npc, entity));
    }

    public static NPCPushEvent callPushEvent(NPC npc, Vector3d vector, Cause cause) {
        NPCPushEvent event = new NPCPushEvent(npc, vector, cause);
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        Sponge.getEventManager().post(event);
        return event;
    }

    public static float clampYaw(float yaw) {
        while (yaw < -180.0F) {
            yaw += 360.0F;
        }

        while (yaw >= 180.0F) {
            yaw -= 360.0F;
        }
        return yaw;
    }

    public static void faceEntity(Entity entity, Entity at) {
        if (at == null || entity == null || entity.getWorld() != at.getWorld())
            return;
        if (at instanceof Living) {
            NMS.look(entity, at);
        } else {
            faceLocation(entity, at.getLocation());
        }
    }

    public static void faceLocation(Entity entity, Location<World> to) {
        faceLocation(entity, to, false);
    }

    public static void faceLocation(Entity entity, Location<World> to, boolean headOnly) {
        faceLocation(entity, to, headOnly, true);
    }

    public static void faceLocation(Entity entity, Location<World> to, boolean headOnly, boolean immediate) {
        if (to == null || entity.getWorld() != to.getExtent())
            return;
        NMS.look(entity, to, headOnly, immediate);
    }

    public static Location<World> getEyeLocation(Entity entity) {
        return entity instanceof Living ? new Location<>(entity.getWorld(), ((Living) entity).getProperty(EyeLocationProperty.class).get().getValue()) : entity.getLocation();
    }

    public static Random getFastRandom() {
        return new XORShiftRNG();
    }

    public static String getMinecraftRevision() {
        String raw = Sponge.getPlatform().getMinecraftVersion().getName();
        return raw.substring(raw.lastIndexOf('.') + 2);
    }

    public static boolean isAlwaysFlyable(EntityType type) {
        if (type.getName().toLowerCase().contains("vex")) // 1.11 compatibility
            return true;
        if (type == EntityTypes.BAT || type == EntityTypes.BLAZE || type == EntityTypes.GHAST
                || type == EntityTypes.ENDER_DRAGON || type == EntityTypes.WITHER) {
                return true;
        }
        return false;
    }

    public static boolean isLoaded(Location<World> location) {
        if (location.getExtent() == null)
            return false;
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return location.getExtent().getChunk(chunkX, 0, chunkZ).isPresent();
    }

    public static String listValuesPretty(Enum<?>[] values) {
        return "<e>" + Joiner.on("<a>, <e>").join(values).toLowerCase().replace('_', ' ');
    }

    public static boolean locationWithinRange(Location<World> current, Location<World> target, double range) {
        if (current == null || target == null)
            return false;
        if (current.getExtent() != target.getExtent())
            return false;
        return current.getPosition().distanceSquared(target.getPosition()) < Math.pow(range, 2);
    }

    public static EntityType matchEntityType(String toMatch) {
        return matchCatalogType(Sponge.getRegistry().getAllOf(EntityType.class), toMatch);
    }

    public static <T extends Enum<?>> T matchEnum(T[] values, String toMatch) {
        toMatch = toMatch.toLowerCase().replace('-', '_').replace(' ', '_');
        for (T check : values) {
            if (toMatch.equals(check.name().toLowerCase())
                    || (toMatch.equals("item") && check == EntityTypes.ITEM)) {
                return check; // check for an exact match first
            }
        }
        for (T check : values) {
            String name = check.name().toLowerCase();
            if (name.replace("_", "").equals(toMatch) || name.startsWith(toMatch)) {
                return check;
            }
        }
        return null;
    }
    public static <T extends CatalogType> T matchCatalogType(Collection<T> values, String toMatch) {
        toMatch = toMatch.toLowerCase().replace('-', '_').replace(' ', '_');
        for (T check : values) {
            if (toMatch.equals(check.getId().toLowerCase())
                    || (toMatch.equals("item") && check == EntityTypes.ITEM)) {
                return check; // check for an exact match first
            }
        }
        for (T check : values) {
            String name = check.getId().toLowerCase();
            if (name.indexOf(':') != -1) {
                name = name.substring(name.indexOf(':') + 1);
            }
            if (name.replace("_", "").equals(toMatch) || name.startsWith(toMatch)) {
                return check;
            }
        }
        return null;
    }

    public static boolean matchesItemInHand(Player player, String setting) {
        String parts = setting;
        if (parts.contains("*"))
            return true;
        for (String part : Splitter.on(',').split(parts)) {
            ItemStack item = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
            if (item != null && item.getItem().getName().equalsIgnoreCase(part)) {
                return true;
            }
        }
        return false;
    }

    public static String prettyEnum(Enum<?> e) {
        return e.name().toLowerCase().replace('_', ' ');
    }

    private static final Location<World> AT_LOCATION = new Location<>(null, 0, 0, 0);
}
