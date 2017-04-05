package net.citizensnpcs.editor;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.trait.CurrentLocation;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class CopierEditor extends Editor {
    private final String name;
    private final NPC npc;
    private final Player player;

    public CopierEditor(Player player, NPC npc) {
        this.player = player;
        this.npc = npc;
        this.name = npc.getFullName();
    }

    @Override
    public void begin() {
        Messaging.sendTr(player, Messages.COPIER_EDITOR_BEGIN);
    }

    @Override
    public void end() {
        Messaging.sendTr(player, Messages.COPIER_EDITOR_END);
    }

    @Listener
    public void onBlockClick(InteractBlockEvent event) {
        NPC copy = npc.clone();
        if (!copy.getFullName().equals(name)) {
            copy.setName(name);
        }

        if (copy.isSpawned() && player.isOnline()) {
            Location<World> location = player.getLocation();
            location.getExtent().loadChunk(location.getChunkPosition(), false);
            copy.teleport(location, TeleportCause.PLUGIN);
            copy.getTrait(CurrentLocation.class).setLocation(location);
        }

        Messaging.sendTr(player, Messages.NPC_COPIED, npc.getName());
    }
}
