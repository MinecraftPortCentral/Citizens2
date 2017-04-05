package net.citizensnpcs.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import com.flowpowered.math.vector.Vector3d;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.minecraft.block.Block;
import org.spongepowered.api.block.tileentity.Skull;
import org.spongepowered.api.boss.BossBar;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.monster.Wither;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class NMS {
    private NMS() {
        // util class
    }

    public static boolean addEntityToWorld(org.spongepowered.api.entity.Entity entity, Cause cause) {
        return BRIDGE.addEntityToWorld(entity, cause);
    }

    public static void addOrRemoveFromPlayerList(org.spongepowered.api.entity.Entity entity, boolean remove) {
        BRIDGE.addOrRemoveFromPlayerList(entity, remove);
    }

    public static void attack(Living attacker, Living bukkitTarget) {
        BRIDGE.attack(attacker, bukkitTarget);
    }

    /*
     * Yggdrasil's default implementation of this method silently fails instead of throwing
     * an Exception like it should.
     */
    public static GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Exception {
        return BRIDGE.fillProfileProperties(profile, requireSecure);
    }

    public static BlockBreaker getBlockBreaker(Entity entity, Block targetBlock, BlockBreakerConfiguration config) {
        return BRIDGE.getBlockBreaker(entity, targetBlock, config);
    }

    public static BossBar getBossBar(org.spongepowered.api.entity.Entity entity) {
        return BRIDGE.getBossBar(entity);
    }

    public static BoundingBox getBoundingBox(org.spongepowered.api.entity.Entity handle) {
        return BRIDGE.getBoundingBox(handle);
    }

    public static Field getField(Class<?> clazz, String field) {
        if (clazz == null)
            return null;
        Field f = null;
        try {
            f = clazz.getDeclaredField(field);
            f.setAccessible(true);
        } catch (Exception e) {
            Messaging.logTr(Messages.ERROR_GETTING_FIELD, field, e.getLocalizedMessage());
        }
        return f;
    }

    public static GameProfileRepository getGameProfileRepository() {
        return BRIDGE.getGameProfileRepository();
    }

    public static float getHeadYaw(org.spongepowered.api.entity.Entity entity) {
        return BRIDGE.getHeadYaw(entity);
    }

    public static float getHorizontalMovement(org.spongepowered.api.entity.Entity bukkitEntity) {
        return BRIDGE.getHorizontalMovement(bukkitEntity);
    }

    public static NPC getNPC(Entity entity) {
        return BRIDGE.getNPC(entity);
    }

    public static List<org.spongepowered.api.entity.Entity> getPassengers(org.spongepowered.api.entity.Entity entity) {
        return BRIDGE.getPassengers(entity);
    }

    public static GameProfile getProfile(Skull meta) {
        return BRIDGE.getProfile(meta);
    }

    public static String getSound(String flag) throws CommandException {
        return BRIDGE.getSound(flag);
    }

    public static float getSpeedFor(NPC npc) {
        return BRIDGE.getSpeedFor(npc);
    }

    public static float getStepHeight(org.spongepowered.api.entity.Entity entity) {
        return BRIDGE.getStepHeight(entity);
    }

    public static MCNavigator getTargetNavigator(Entity entity, Iterable<Vector3d> dest, NavigatorParameters params) {
        return BRIDGE.getTargetNavigator(entity, dest, params);
    }

    public static MCNavigator getTargetNavigator(Entity entity, Location dest, NavigatorParameters params) {
        return BRIDGE.getTargetNavigator(entity, dest, params);
    }

    public static TargetNavigator getTargetNavigator(org.spongepowered.api.entity.Entity entity, org.spongepowered.api.entity.Entity target,
            NavigatorParameters parameters) {
        return BRIDGE.getTargetNavigator(entity, target, parameters);
    }

    public static org.spongepowered.api.entity.Entity getVehicle(org.spongepowered.api.entity.Entity entity) {
        return BRIDGE.getVehicle(entity);
    }

    public static float getVerticalMovement(org.spongepowered.api.entity.Entity bukkitEntity) {
        return BRIDGE.getVerticalMovement(bukkitEntity);
    }

    public static boolean isOnGround(org.spongepowered.api.entity.Entity entity) {
        return BRIDGE.isOnGround(entity);
    }

    public static void load(CommandManager commands) {
        BRIDGE.load(commands);
    }

    public static void loadBridge(String rev) throws Exception {
        BRIDGE = (NMSBridge) Class.forName("net.citizensnpcs.nms.v" + rev + ".util.NMSImpl").getConstructor()
                .newInstance();
    }

    public static void loadPlugins() {
        BRIDGE.loadPlugins();
    }

    public static void look(Entity entity, float yaw, float pitch) {
        BRIDGE.look(entity, yaw, pitch);
    }

    public static void look(org.spongepowered.api.entity.Entity entity, Location<World> to, boolean headOnly, boolean immediate) {
        BRIDGE.look(entity, to, headOnly, immediate);
    }

    public static void look(org.spongepowered.api.entity.Entity bhandle, org.spongepowered.api.entity.Entity btarget) {
        BRIDGE.look(bhandle, btarget);
    }

    public static void mount(org.spongepowered.api.entity.Entity entity, org.spongepowered.api.entity.Entity passenger) {
        BRIDGE.mount(entity, passenger);
    }

    public static void openHorseScreen(Horse horse, Player equipper) {
        BRIDGE.openHorseScreen(horse, equipper);
    }

    public static void playAnimation(PlayerAnimation animation, Player player, int radius) {
        BRIDGE.playAnimation(animation, player, radius);
    }

    public static void registerEntityClass(Class<?> clazz) {
        BRIDGE.registerEntityClass(clazz);
    }

    public static void removeFromServerPlayerList(Player player) {
        BRIDGE.removeFromServerPlayerList(player);
    }

    public static void removeFromWorld(org.spongepowered.api.entity.Entity entity) {
        BRIDGE.removeFromWorld(entity);
    }

    public static void removeHookIfNecessary(NPCRegistry npcRegistry, FishHook entity) {
        BRIDGE.removeHookIfNecessary(npcRegistry, entity);
    }

    public static void replaceTrackerEntry(Player player) {
        BRIDGE.replaceTrackerEntry(player);
    }

    public static void sendPositionUpdate(Player excluding, org.spongepowered.api.entity.Entity from, Location<World> storedLocation) {
        BRIDGE.sendPositionUpdate(excluding, from, storedLocation);
    }

    public static void sendTabListAdd(Player recipient, Player listPlayer) {
        BRIDGE.sendTabListAdd(recipient, listPlayer);
    }

    public static void sendTabListRemove(Player recipient, Collection<? extends SkinnableEntity> skinnableNPCs) {
        BRIDGE.sendTabListRemove(recipient, skinnableNPCs);
    }

    public static void sendTabListRemove(Player recipient, Player listPlayer) {
        BRIDGE.sendTabListRemove(recipient, listPlayer);
    }

    public static void setDestination(org.spongepowered.api.entity.Entity entity, double x, double y, double z, float speed) {
        BRIDGE.setDestination(entity, x, y, z, speed);
    }

    public static void setHeadYaw(org.spongepowered.api.entity.Entity entity, float yaw) {
        BRIDGE.setHeadYaw(entity, yaw);
    }

    public static void setKnockbackResistance(Living entity, double d) {
        BRIDGE.setKnockbackResistance(entity, d);
    }

    public static void setNavigationTarget(org.spongepowered.api.entity.Entity handle, org.spongepowered.api.entity.Entity target,
            float speed) {
        BRIDGE.setNavigationTarget(handle, target, speed);
    }

    public static void setProfile(Skull meta, GameProfile profile) {
        BRIDGE.setProfile(meta, profile);
    }

    public static void setShouldJump(org.spongepowered.api.entity.Entity entity) {
        BRIDGE.setShouldJump(entity);
    }

    public static void setSitting(Tameable tameable, boolean sitting) {
        BRIDGE.setSitting(tameable, sitting);
    }

    public static void setStepHeight(org.spongepowered.api.entity.Entity entity, float height) {
        BRIDGE.setStepHeight(entity, height);
    }

    public static void setVerticalMovement(org.spongepowered.api.entity.Entity bukkitEntity, double d) {
        BRIDGE.setVerticalMovement(bukkitEntity, d);
    }

    public static void setWitherCharged(Wither wither, boolean charged) {
        BRIDGE.setWitherCharged(wither, charged);
    }

    public static boolean shouldJump(org.spongepowered.api.entity.Entity entity) {
        return BRIDGE.shouldJump(entity);
    }

    public static void shutdown() {
        BRIDGE.shutdown();
    }

    public static boolean tick(Entity next) {
        return BRIDGE.tick(next);
    }

    public static void trySwim(org.spongepowered.api.entity.Entity entity) {
        BRIDGE.trySwim(entity);
    }

    public static void trySwim(org.spongepowered.api.entity.Entity entity, float power) {
        BRIDGE.trySwim(entity, power);
    }

    public static void updateNavigationWorld(org.spongepowered.api.entity.Entity entity, org.spongepowered.api.world.World world) {
        BRIDGE.updateNavigationWorld(entity, world);
    }

    public static void updatePathfindingRange(NPC npc, float pathfindingRange) {
        BRIDGE.updatePathfindingRange(npc, pathfindingRange);
    }

    private static NMSBridge BRIDGE;
}
