package net.citizensnpcs.npc;

import java.util.List;
import java.util.UUID;

import net.citizensnpcs.Settings.Setting;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCSelectEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.editor.Editor;
import net.citizensnpcs.util.Util;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;
import com.google.common.collect.Lists;

public class NPCSelector implements net.citizensnpcs.api.npc.NPCSelector {
    private UUID consoleSelectedNPC;
    private final PluginContainer plugin;

    public NPCSelector(PluginContainer plugin) {
        this.plugin = plugin;
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Override
    public NPC getSelected(CommandSource sender) {
        if (sender instanceof Player) {
            return getSelectedFromMetadatable((Player) sender);
        } else if (sender instanceof CommandBlockSource) {
            return getSelectedFromMetadatable(((CommandBlockSource) sender).getBlock());
        } else if (sender instanceof ConsoleSource) {
            if (consoleSelectedNPC == null)
                return null;
            return CitizensAPI.getNPCRegistry().getByUniqueIdGlobal(consoleSelectedNPC);
        }
        return null;
    }

    private NPC getSelectedFromMetadatable(Metadatable sender) {
        List<MetadataValue> metadata = sender.getMetadata("selected");
        if (metadata.size() == 0)
            return null;
        return CitizensAPI.getNPCRegistry().getByUniqueIdGlobal((UUID) metadata.get(0).value());
    }

    @Listener
    public void onNPCRemove(NPCRemoveEvent event) {
        NPC npc = event.getNPC();
        List<String> selectors = npc.data().get("selectors");
        if (selectors == null)
            return;
        for (String value : selectors) {
            if (value.equals("console")) {
                consoleSelectedNPC = null;
            } else if (value.startsWith("@")) {
                String[] parts = value.substring(1, value.length()).split(":");
                World world = Sponge.getServer().getWorld(parts[0]).orElse(null);
                if (world != null) {
                    Block block = world.getBlockAt(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),
                            Integer.parseInt(parts[3]));
                    removeMetadata(block);
                }
            } else {
                Player search = Sponge.getServer().getPlayer(value).orElse(null);
                removeMetadata(search);
            }
        }
        npc.data().remove("selectors");
    }

    @Listener
    public void onNPCRightClick(NPCRightClickEvent event) {
        Player player = event.getClicker();
        NPC npc = event.getNPC();
        List<MetadataValue> selected = player.getMetadata("selected");
        if (selected == null || selected.size() == 0 || selected.get(0).asInt() != npc.getId()) {
            if (Util.matchesItemInHand(player, Setting.SELECTION_ITEM.asString())
                    && npc.getTrait(Owner.class).isOwnedBy(player)) {
                player.removeMetadata("selected", plugin);
                select(player, npc);
                Messaging.sendWithNPC(player, Setting.SELECTION_MESSAGE.asString(), npc);
                if (!Setting.QUICK_SELECT.asBoolean())
                    return;
            }
        }
    }

    private void removeMetadata(Metadatable metadatable) {
        if (metadatable != null) {
            metadatable.removeMetadata("selected", plugin);
        }
    }

    public void select(CommandSource sender, NPC npc) {
        // Remove existing selection if any
        List<String> selectors = npc.data().get("selectors");
        if (selectors == null) {
            selectors = Lists.newArrayList();
            npc.data().set("selectors", selectors);
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            setMetadata(npc, player);
            selectors.add(sender.getName());

            // Remove editor if the player has one
            Editor.leave(player);
        } else if (sender instanceof CommandBlockSource) {
            Block block = ((CommandBlockSource) sender).getBlock();
            setMetadata(npc, block);
            selectors.add(toName(block));
        } else if (sender instanceof ConsoleSource) {
            consoleSelectedNPC = npc.getUniqueId();
            selectors.add("console");
        }

        Sponge.getEventManager().post(new NPCSelectEvent(npc, sender));
    }

    private void setMetadata(NPC npc, Metadatable metadatable) {
        if (metadatable.hasMetadata("selected")) {
            metadatable.removeMetadata("selected", plugin);
        }
        metadatable.setMetadata("selected", new FixedMetadataValue(plugin, npc.getUniqueId()));
    }

    private String toName(Block block) {
        return '@' + block.getWorld().getName() + ":" + Integer.toString(block.getX()) + ":"
                + Integer.toString(block.getY()) + ":" + Integer.toString(block.getZ());
    }
}
