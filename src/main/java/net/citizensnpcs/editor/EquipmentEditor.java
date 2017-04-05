package net.citizensnpcs.editor;

import java.util.Map;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Tristate;
import com.google.common.collect.Maps;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

public class EquipmentEditor extends Editor {
    private final NPC npc;
    private final Player player;

    public EquipmentEditor(Player player, NPC npc) {
        this.player = player;
        this.npc = npc;
    }

    @Override
    public void begin() {
        Messaging.sendTr(player, Messages.EQUIPMENT_EDITOR_BEGIN);
    }

    @Override
    public void end() {
        Messaging.sendTr(player, Messages.EQUIPMENT_EDITOR_END);
    }

    @Listener
    public void onPlayerChat(final MessageChannelEvent.Chat event, @First Player eventPlayer) {
        EquipmentSlot slot = null;
        if (event.getMessage().equals("helmet")
                && eventPlayer.hasPermission("citizens.npc.edit.equip.any-helmet")) {
            slot = EquipmentSlot.HELMET;
        }
        if (event.getMessage().equals("offhand")
                && eventPlayer.hasPermission("citizens.npc.edit.equip.offhand")) {
            slot = EquipmentSlot.OFF_HAND;
        }
        if (slot == null) {
            return;
        }
        final EquipmentSlot finalSlot = slot;
        Sponge.getGame().getScheduler().createTaskBuilder().execute(new Runnable() {
            @Override
            public void run() {
                ItemStack hand = eventPlayer.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
                if (hand == null) {
                    return;
                }
                ItemStack old = npc.getTrait(Equipment.class).get(finalSlot);
                if (old != null && old.getType() != BlockTypes.AIR) {
                    eventPlayer.getWorld().dropItemNaturally(eventPlayer.getLocation(), old);
                }
                ItemStack newStack = hand.copy();
                newStack.setAmount(1);
                npc.getTrait(Equipment.class).set(finalSlot, newStack);
                hand.setAmount(hand.getAmount() - 1);
                eventPlayer.setItemInHand(HandTypes.MAIN_HAND, hand);
            }
        }).submit(CitizensAPI.getPlugin());
        event.setCancelled(true);
    }

    @Listener
    public void onPlayerInteract(InteractBlockEvent.Secondary event, @First Player player) {
        if (event.getTargetBlock().getState().getType() == BlockTypes.AIR && Editor.hasEditor(player)) {
            event.setUseItemResult(Tristate.FALSE);
        }
    }

    @Listener
    public void onPlayerInteractEntity(InteractEntityEvent event, @First Player eventPlayer) {
        if (!npc.isSpawned() || !eventPlayer.equals(this.player)
                || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND
                || !npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())))
            return;

        Equipper equipper = EQUIPPERS.get(npc.getEntity().getType());
        if (equipper == null) {
            equipper = new GenericEquipper();
        }
        equipper.equip(eventPlayer, npc);
        event.setCancelled(true);
    }

    private static final Map<EntityType, Equipper> EQUIPPERS = Maps.newHashMap();

    static {
        EQUIPPERS.put(EntityTypes.PIG, new PigEquipper());
        EQUIPPERS.put(EntityTypes.SHEEP, new SheepEquipper());
        EQUIPPERS.put(EntityTypes.ENDERMAN, new EndermanEquipper());
        EQUIPPERS.put(EntityTypes.HORSE, new HorseEquipper());
        try {
            // TODO
            /*EQUIPPERS.put(EntityType.valueOf("ZOMBIE_HORSE"), new HorseEquipper());
            EQUIPPERS.put(EntityType.valueOf("LLAMA"), new HorseEquipper());
            EQUIPPERS.put(EntityType.valueOf("DONKEY"), new HorseEquipper());
            EQUIPPERS.put(EntityType.valueOf("MULE"), new HorseEquipper());
            EQUIPPERS.put(EntityType.valueOf("SKELETON_HORSE"), new HorseEquipper());*/
        } catch (IllegalArgumentException ex) {
        }
    }
}