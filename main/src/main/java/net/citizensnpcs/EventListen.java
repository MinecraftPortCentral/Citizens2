package net.citizensnpcs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.common.base.Predicates;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.CitizensDeserialiseMetaEvent;
import net.citizensnpcs.api.event.CitizensPreReloadEvent;
import net.citizensnpcs.api.event.CitizensSerialiseMetaEvent;
import net.citizensnpcs.api.event.CommandSenderCreateNPCEvent;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.EntityTargetNPCEvent;
import net.citizensnpcs.api.event.NPCCombustByBlockEvent;
import net.citizensnpcs.api.event.NPCCombustByEntityEvent;
import net.citizensnpcs.api.event.NPCCombustEvent;
import net.citizensnpcs.api.event.NPCDamageByBlockEvent;
import net.citizensnpcs.api.event.NPCDamageByEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEntityEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.event.PlayerCreateNPCEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.npc.skin.SkinUpdateTracker;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.NMS;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.animal.Horse;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class EventListen {
    private final NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
    private final Map<String, NPCRegistry> registries;
    private final SkinUpdateTracker skinUpdateTracker;
    private final ListMultimap<ChunkCoord, NPC> toRespawn = ArrayListMultimap.create();

    EventListen(Map<String, NPCRegistry> registries) {
        this.registries = registries;
        this.skinUpdateTracker = new SkinUpdateTracker(npcRegistry, registries);
    }

    private void checkCreationEvent(CommandSenderCreateNPCEvent event) {
        if (event.getCreator().hasPermission("citizens.admin.avoid-limits"))
            return;
        int limit = Setting.DEFAULT_NPC_LIMIT.asInt();
        int maxChecks = Setting.MAX_NPC_LIMIT_CHECKS.asInt();
        for (int i = maxChecks; i >= 0; i--) {
            if (!event.getCreator().hasPermission("citizens.npc.limit." + i))
                continue;
            limit = i;
            break;
        }
        if (limit < 0)
            return;
        int owned = 0;
        for (NPC npc : npcRegistry) {
            if (!event.getNPC().equals(npc) && npc.hasTrait(Owner.class)
                    && npc.getTrait(Owner.class).isOwnedBy(event.getCreator()))
                owned++;
        }
        int wouldOwn = owned + 1;
        if (wouldOwn > limit) {
            event.setCancelled(true);
            event.setCancelReason(Messaging.tr(Messages.OVER_NPC_LIMIT, limit));
        }
    }

    private Iterable<NPC> getAllNPCs() {
        return Iterables.filter(Iterables.<NPC> concat(npcRegistry, Iterables.concat(registries.values())),
                Predicates.notNull());
    }

    @Listener(order = Order.POST)
    public void onChunkLoad(LoadChunkEvent event) {
        respawnAllFromCoord(toCoord(event.getTargetChunk()));
    }

    @Listener(order = Order.LAST)
    public void onChunkUnload(UnloadChunkEvent event) {
        ChunkCoord coord = toCoord(event.getTargetChunk());
        Location<World> loc = new Location<World>(null, 0, 0, 0);
        final Chunk chunk = event.getTargetChunk();
        for (NPC npc : getAllNPCs()) {
            if (npc == null || !npc.isSpawned())
                continue;
            loc = npc.getEntity().getLocation();
            boolean sameChunkCoordinates = coord.z == loc.getBlockZ() >> 4 && coord.x == loc.getBlockX() >> 4;
            if (!sameChunkCoordinates || !chunk.getWorld().equals(loc.getExtent()))
                continue;
            if (!npc.despawn(DespawnReason.CHUNK_UNLOAD)) {
                //event.setCancelled(true);
                ((net.minecraft.world.chunk.Chunk) chunk).unloaded = false;
                if (Messaging.isDebugging()) {
                    Messaging.debug("Cancelled chunk unload at [" + coord.x + "," + coord.z + "]");
                }
                respawnAllFromCoord(coord);
                return;
            }
            toRespawn.put(coord, npc);
            if (Messaging.isDebugging()) {
                Messaging.debug("Despawned id", npc.getId(),
                        "due to chunk unload at [" + coord.x + "," + coord.z + "]");
            }
        }
    }

    @Listener(order = Order.POST)
    public void onCitizensReload(CitizensPreReloadEvent event) {
        skinUpdateTracker.reset();
        toRespawn.clear();
    }

    @Listener
    public void onCommandSenderCreateNPC(CommandSenderCreateNPCEvent event) {
        checkCreationEvent(event);
    }

    /*
     * Entity events
     */
    @Listener
    public void onEntityCombust(DamageEntityEvent event, @First DamageSource source) {
        NPC npc = npcRegistry.getNPC(event.getTargetEntity());
        if (npc == null)
            return;
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        if (source instanceof EntityDamageSource) {
            Sponge.getEventManager().post(new NPCCombustByEntityEvent((EntityDamageSource) source, npc));
        } else if (source instanceof BlockDamageSource) {
            Sponge.getEventManager().post(new NPCCombustByBlockEvent((BlockDamageSource) source, npc));
        } else {
            Sponge.getEventManager().post(new NPCCombustEvent(source, npc));
        }
    }

    @Listener
    public void onEntityDamage(DamageEntityEvent event, @First DamageSource source) {
        NPC npc = npcRegistry.getNPC(event.getTargetEntity());

        if (npc == null) {
            if (source instanceof EntityDamageSource) {
                npc = npcRegistry.getNPC(((EntityDamageSource) source).getSource());
                if (npc == null)
                    return;
                event.setCancelled(!npc.data().get(NPC.DAMAGE_OTHERS_METADATA, true));
                NPCDamageEntityEvent damageEvent = new NPCDamageEntityEvent(npc, (EntityDamageSource) source);
                damageEvent.setCancelled(event.isCancelled());
                Sponge.getEventManager().post(damageEvent);
            }
            return;
        }
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
        if (source instanceof EntityDamageSource) {
            NPCDamageByEntityEvent damageEvent = new NPCDamageByEntityEvent(npc, (EntityDamageSource) source);
            Sponge.getEventManager().post(damageEvent);

            if (!damageEvent.isCancelled() || !(damageEvent.getDamager() instanceof Player))
                return;
            Player damager = (Player) damageEvent.getDamager();

            NPCLeftClickEvent leftClickEvent = new NPCLeftClickEvent(npc, damager);
            Sponge.getEventManager().post(leftClickEvent);
        } else if (event instanceof EntityDamageByBlockEvent) {
            Sponge.getEventManager().post(new NPCDamageByBlockEvent(npc, (EntityDamageByBlockEvent) event));
        } else {
            Sponge.getEventManager().post(new NPCDamageEvent(npc, event));
        }
    }

    @Listener
    public void onEntityDeath(DestructEntityEvent.Death event) {
        final NPC npc = npcRegistry.getNPC(event.getTargetEntity());
        if (npc == null) {
            return;
        }

        if (!npc.data().get(NPC.DROPS_ITEMS_METADATA, false)) {
            event.getDrops().clear();
        }

        final Location<World> location = npc.getEntity().getLocation();
        Sponge.getEventManager().post(new NPCDeathEvent(npc, event));
        npc.despawn(DespawnReason.DEATH);

        if (npc.data().has(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA)) {
            String teamName = npc.data().get(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA);
            Team team = Sponge.getServer().getServerScoreboard().get().getTeam(teamName).orElse(null);
            if (team != null) {
                team.unregister();
            }

            npc.data().remove(NPC.SCOREBOARD_FAKE_TEAM_NAME_METADATA);
        }

        if (npc.data().get(NPC.RESPAWN_DELAY_METADATA, -1) >= 0) {
            int delay = npc.data().get(NPC.RESPAWN_DELAY_METADATA, -1);
            Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(delay + 2).execute(new Runnable() {
                @Override
                public void run() {
                    if (!npc.isSpawned() && npc.getOwningRegistry().getByUniqueId(npc.getUniqueId()) == npc) {
                        npc.spawn(location);
                    }
                }
            }).submit(CitizensAPI.getPlugin());
        }
    }

    @Listener(order = Order.LAST)
    public void onEntitySpawn(SpawnEntityEvent event) {
        if (event.isCancelled() && npcRegistry.isNPC(event.getEntity())) {
            event.setCancelled(false);
        }
    }

    @Listener
    public void onEntityTarget(EntityTargetEvent event) {
        NPC npc = npcRegistry.getNPC(event.getTarget());
        if (npc == null)
            return;
        event.setCancelled(
                !npc.data().get(NPC.TARGETABLE_METADATA, !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true)));
        Sponge.getEventManager().post(new EntityTargetNPCEvent(event, npc));
    }

    @Listener
    public void onMetaDeserialise(CitizensDeserialiseMetaEvent event) {
        if (event.getKey().keyExists("skull")) {
            String owner = event.getKey().getString("skull.owner", "");
            UUID uuid = event.getKey().keyExists("skull.uuid") ? UUID.fromString(event.getKey().getString("skull.uuid"))
                    : null;
            if (owner.isEmpty() && uuid == null) {
                return;
            }
            GameProfile profile = new GameProfile(uuid, owner);
            for (DataKey sub : event.getKey().getRelative("skull.properties").getSubKeys()) {
                String propertyName = sub.name();
                for (DataKey property : sub.getIntegerSubKeys()) {
                    profile.getProperties().put(propertyName,
                            new Property(property.getString("name"), property.getString("value"),
                                    property.keyExists("signature") ? property.getString("signature") : null));
                }
            }
            SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
            NMS.setProfile(meta, profile);
            event.getItemStack().setItemMeta(meta);
        }
    }

    @Listener
    public void onMetaSerialise(CitizensSerialiseMetaEvent event) {
        if (!(event.getMeta() instanceof SkullMeta))
            return;
        SkullMeta meta = (SkullMeta) event.getMeta();
        GameProfile profile = NMS.getProfile(meta);
        if (profile == null)
            return;
        if (profile.getName() != null) {
            event.getKey().setString("skull.owner", profile.getName());
        }
        if (profile.getId() != null) {
            event.getKey().setString("skull.uuid", profile.getId().toString());
        }
        if (profile.getProperties() != null) {
            for (Entry<String, Collection<Property>> entry : profile.getProperties().asMap().entrySet()) {
                DataKey relative = event.getKey().getRelative("skull.properties." + entry.getKey());
                int i = 0;
                for (Property value : entry.getValue()) {
                    relative.getRelative(i).setString("name", value.getName());
                    if (value.getSignature() != null) {
                        relative.getRelative(i).setString("signature", value.getSignature());
                    }
                    relative.getRelative(i).setString("value", value.getValue());
                    i++;
                }
            }
        }
    }

    @Listener
    public void onNavigationBegin(NavigationBeginEvent event) {
        skinUpdateTracker.onNPCNavigationBegin(event.getNPC());
    }

    @Listener
    public void onNavigationComplete(NavigationCompleteEvent event) {
        skinUpdateTracker.onNPCNavigationComplete(event.getNPC());
    }

    @Listener
    public void onNeedsRespawn(NPCNeedsRespawnEvent event) {
        ChunkCoord coord = toCoord(event.getSpawnLocation());
        if (toRespawn.containsEntry(coord, event.getNPC()))
            return;
        Messaging.debug("Stored", event.getNPC().getId(), "for respawn from NPCNeedsRespawnEvent");
        toRespawn.put(coord, event.getNPC());
    }

    @Listener(order = Order.POST)
    public void onNPCDespawn(NPCDespawnEvent event) {
        if (event.getReason() == DespawnReason.PLUGIN || event.getReason() == DespawnReason.REMOVAL
                || event.getReason() == DespawnReason.RELOAD) {
            Messaging.debug("Preventing further respawns of " + event.getNPC().getId() + " due to DespawnReason."
                    + event.getReason().name());
            if (event.getNPC().getStoredLocation() != null) {
                toRespawn.remove(toCoord(event.getNPC().getStoredLocation()), event.getNPC());
            }
        } else {
            Messaging.debug("Removing " + event.getNPC().getId() + " from skin tracker due to DespawnReason."
                    + event.getReason().name());
        }
        skinUpdateTracker.onNPCDespawn(event.getNPC());
    }

    @Listener
    public void onNPCSpawn(NPCSpawnEvent event) {
        skinUpdateTracker.onNPCSpawn(event.getNPC());
    }

    @Listener
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (npcRegistry.getNPC(event.getPlayer()) == null)
            return;
        NMS.removeFromServerPlayerList(event.getPlayer());
        // on teleport, player NPCs are added to the server player list. this is
        // undesirable as player NPCs are not real players and confuse plugins.
    }

    @Listener(order = Order.POST)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        skinUpdateTracker.updatePlayer(event.getPlayer(), 20, true);
    }

    @Listener
    public void onPlayerCreateNPC(PlayerCreateNPCEvent event) {
        checkCreationEvent(event);
    }

    @Listener
    public void onPlayerFish(PlayerFishEvent event) {
        if (npcRegistry.isNPC(event.getCaught()) && npcRegistry.getNPC(event.getCaught()).isProtected()) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.POST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        NPC npc = npcRegistry.getNPC(event.getRightClicked());
        if (npc == null || event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        Player player = event.getPlayer();
        NPCRightClickEvent rightClickEvent = new NPCRightClickEvent(npc, player);
        Sponge.getEventManager().post(rightClickEvent);
    }

    @Listener(order = Order.POST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        skinUpdateTracker.updatePlayer(event.getPlayer(), 6 * 20, true);
    }

    @Listener
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event) {
        NPC npc = npcRegistry.getNPC(event.getEntity());
        if (npc == null) {
            return;
        }
        if (npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true)) {
            event.setCancelled(true);
        }
    }

    // recalculate player NPCs the first time a player moves and every time
    // a player moves a certain distance from their last position.
    @Listener(order = Order.POST)
    public void onPlayerMove(final MoveEntityEvent event, @First Player player) {
        skinUpdateTracker.onPlayerMove(player);
    }

    @Listener(order = Order.POST)
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event, @First Player player) {
        Editor.leave(player);
        if (player.isInsideVehicle()) {
            NPC npc = npcRegistry.getNPC(player.getVehicle());
            if (npc != null) {
                player.leaveVehicle();
            }
        }
        skinUpdateTracker.removePlayer(player.getUniqueId());
    }

    @Listener(order = Order.POST)
    public void onPlayerRespawn(RespawnPlayerEvent event, @First Player player) {
        skinUpdateTracker.updatePlayer(player, 15, true);
    }

    @Listener
    public void onPlayerTeleport(final MoveEntityEvent.Teleport event, @First Player player) {
        if (event.getCause() == TeleportCause.PLUGIN && !event.getTargetEntity().hasMetadata("citizens-force-teleporting")
                && npcRegistry.getNPC(Setting.TELEPORT_DELAY.asInt() > 0)) {
            event.setCancelled(true);
            Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(Setting.TELEPORT_DELAY.asInt()).execute(new Runnable() {
                @Override
                public void run() {
                    player.setMetadata("citizens-force-teleporting",
                            new FixedMetadataValue(CitizensAPI.getPlugin(), true));
                    player.teleport(event.getTo());
                    player.removeMetadata("citizens-force-teleporting", CitizensAPI.getPlugin());
                }
            }).submit(CitizensAPI.getPlugin());
        }
        skinUpdateTracker.updatePlayer(player, 15, true);
    }

    @Listener
    public void onProjectileHit(final CollideEntityEvent.Impact event) {
        if (!(event.getEntity() instanceof FishHook))
            return;
        NMS.removeHookIfNecessary(npcRegistry, (FishHook) event.getEntity());
        Sponge.getGame().getScheduler().createTaskBuilder().interval(1, unit).execute(new Runnable() {
            int n = 0;

            @Override
            public void run() {
                if (n++ > 5) {
                    cancel();
                }
                NMS.removeHookIfNecessary(npcRegistry, (FishHook) event.getEntity());
            }
        }).submit(CitizensAPI.getPlugin());
    }

    @Listener
    public void onVehicleDestroy(DestructEntityEvent event) {
        NPC npc = npcRegistry.getNPC(event.getVehicle());
        if (npc == null) {
            return;
        }
        event.setCancelled(npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true));
    }

    @Listener
    public void onVehicleEnter(MoveEntityEvent event) {
        if (!npcRegistry.isNPC(event.getEntered()))
            return;
        NPC npc = npcRegistry.getNPC(event.getEntered());
        if ((npc.getEntity() instanceof Horse || npc.getEntity().getType() == EntityTypes.BOAT
                || npc.getEntity() instanceof Minecart) && !npc.getTrait(Controllable.class).isEnabled()) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onWorldLoad(LoadWorldEvent event) {
        for (ChunkCoord chunk : toRespawn.keySet()) {
            if (!chunk.worldName.equals(event.getWorld().getName())
                    || !event.getWorld().isChunkLoaded(chunk.x, chunk.z))
                continue;
            respawnAllFromCoord(chunk);
        }
    }

    @Listener(order = Order.POST)
    public void onWorldUnload(UnloadWorldEvent event) {
        for (NPC npc : getAllNPCs()) {
            if (npc == null || !npc.isSpawned() || !npc.getEntity().getWorld().equals(event.getWorld()))
                continue;
            boolean despawned = npc.despawn(DespawnReason.WORLD_UNLOAD);
            if (event.isCancelled() || !despawned) {
                for (ChunkCoord coord : toRespawn.keySet()) {
                    if (event.getWorld().getName().equals(coord.worldName)) {
                        respawnAllFromCoord(coord);
                    }
                }
                event.setCancelled(true);
                return;
            }
            if (npc.isSpawned()) {
                storeForRespawn(npc);
                Messaging.debug("Despawned", npc.getId() + "due to world unload at", event.getWorld().getName());
            }
        }
    }

    private void respawnAllFromCoord(ChunkCoord coord) {
        List<NPC> ids = toRespawn.get(coord);
        for (int i = 0; i < ids.size(); i++) {
            NPC npc = ids.get(i);
            boolean success = spawn(npc);
            if (!success) {
                if (Messaging.isDebugging()) {
                    Messaging.debug("Couldn't respawn id", npc.getId(),
                            "during chunk event at [" + coord.x + "," + coord.z + "]");
                }
                continue;
            }
            ids.remove(i--);
            if (Messaging.isDebugging()) {
                Messaging.debug("Spawned id", npc.getId(), "due to chunk event at [" + coord.x + "," + coord.z + "]");
            }
        }
    }

    private boolean spawn(NPC npc) {
        Location<World> spawn = npc.getTrait(CurrentLocation.class).getLocation();
        if (spawn == null) {
            if (Messaging.isDebugging()) {
                Messaging.debug("Couldn't find a spawn location for despawned NPC id", npc.getId());
            }
            return false;
        }
        return npc.spawn(spawn);
    }

    private void storeForRespawn(NPC npc) {
        toRespawn.put(toCoord(npc.getEntity().getLocation()), npc);
    }

    private ChunkCoord toCoord(Chunk chunk) {
        return new ChunkCoord(chunk);
    }

    private ChunkCoord toCoord(Location<World> loc) {
        return new ChunkCoord(loc.getExtent().getName(), loc.getBlockX() >> 4, loc.getBlockZ() >> 4);
    }

    private static class ChunkCoord {
        private final String worldName;
        private final int x;
        private final int z;

        private ChunkCoord(Chunk chunk) {
            this(chunk.getWorld().getName(), chunk.getPosition().getX(), chunk.getPosition().getZ());
        }

        private ChunkCoord(String worldName, int x, int z) {
            this.x = x;
            this.z = z;
            this.worldName = worldName;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ChunkCoord other = (ChunkCoord) obj;
            if (worldName == null) {
                if (other.worldName != null) {
                    return false;
                }
            } else if (!worldName.equals(other.worldName)) {
                return false;
            }
            return x == other.x && z == other.z;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * (prime * (prime + ((worldName == null) ? 0 : worldName.hashCode())) + x) + z;
        }
    }
}
