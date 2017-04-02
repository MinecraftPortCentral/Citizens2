package net.citizensnpcs.util;

import java.util.Collection;
import java.util.List;

import com.flowpowered.math.vector.Vector3i;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;

import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.BlockBreaker;
import net.citizensnpcs.api.npc.BlockBreaker.BlockBreakerConfiguration;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.npc.ai.MCNavigationStrategy.MCNavigator;
import net.citizensnpcs.npc.ai.MCTargetStrategy.TargetNavigator;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.Skull;
import org.spongepowered.api.data.manipulator.mutable.entity.TameableData;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.monster.Wither;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface NMSBridge {
    public boolean addEntityToWorld(Entity entity, Cause cause);

    public void addOrRemoveFromPlayerList(Entity entity, boolean remove);

    public void attack(Living attacker, Living target);

    public GameProfile fillProfileProperties(GameProfile profile, boolean requireSecure) throws Exception;

    public BlockBreaker getBlockBreaker(Entity entity, BlockType targetBlock, BlockBreakerConfiguration config);

    //public BossBar getBossBar(Entity entity);

    public BoundingBox getBoundingBox(Entity handle);

    public GameProfileRepository getGameProfileRepository();

    public float getHeadYaw(Entity entity);

    public float getHorizontalMovement(Entity entity);

    public NPC getNPC(Entity entity);

    public List<Entity> getPassengers(Entity entity);

    public GameProfile getProfile(Skull meta);

    public String getSound(String flag) throws CommandException;

    public float getSpeedFor(NPC npc);

    public float getStepHeight(Entity entity);

    public TargetNavigator getTargetNavigator(Entity handle, Entity target, NavigatorParameters parameters);

    public MCNavigator getTargetNavigator(Entity entity, Iterable<Vector3i> dest, NavigatorParameters params);

    public MCNavigator getTargetNavigator(Entity entity, Location<World> dest, NavigatorParameters params);

    public Entity getVehicle(Entity entity);

    public float getVerticalMovement(Entity entity);

    public boolean isOnGround(Entity entity);

    public void load(CommandManager commands);

    public void loadPlugins();

    public void look(Entity from, Entity to);

    public void look(Entity entity, float yaw, float pitch);

    public void look(Entity entity, Location<World> to, boolean headOnly, boolean immediate);

    public void mount(Entity entity, Entity passenger);

    public void openHorseScreen(Horse horse, Player equipper);

    public void playAnimation(PlayerAnimation animation, Player player, int radius);

    public void registerEntityClass(Class<?> clazz);

    public void removeFromServerPlayerList(Player player);

    public void removeFromWorld(Entity entity);

    public void removeHookIfNecessary(NPCRegistry npcRegistry, FishHook entity);

    public void replaceTrackerEntry(Player player);

    public void sendPositionUpdate(Player excluding, Entity from, Location<World> storedLocation);

    public void sendTabListAdd(Player recipient, Player listPlayer);

    public void sendTabListRemove(Player recipient, Collection<? extends SkinnableEntity> skinnableNPCs);

    public void sendTabListRemove(Player recipient, Player listPlayer);

    public void setDestination(Entity entity, double x, double y, double z, float speed);

    public void setHeadYaw(Entity entity, float yaw);

    public void setKnockbackResistance(Living entity, double d);

    public void setNavigationTarget(Entity handle, Entity target, float speed);

    public void setProfile(Skull meta, GameProfile profile);

    public void setShouldJump(Entity entity);

    public void setSitting(TameableData tameable, boolean sitting);

    public void setStepHeight(Entity entity, float height);

    public void setVerticalMovement(Entity bukkitEntity, double d);

    public void setWitherCharged(Wither wither, boolean charged);

    public boolean shouldJump(Entity entity);

    public void shutdown();

    public boolean tick(Entity next);

    public void trySwim(Entity entity);

    public void trySwim(Entity entity, float power);

    public void updateNavigationWorld(Entity entity, World world);

    public void updatePathfindingRange(NPC npc, float pathfindingRange);
}
