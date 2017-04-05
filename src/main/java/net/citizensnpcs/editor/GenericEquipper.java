package net.citizensnpcs.editor;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Equipment.EquipmentSlot;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

public class GenericEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC toEquip) {
        ItemStack hand = equipper.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        Equipment trait = toEquip.getTrait(Equipment.class);
        EquipmentSlot slot = EquipmentSlot.HAND;
        ItemType type = hand == null ? ItemTypes.NONE : hand.getItem();
        // First, determine the slot to edit
        if (type == ItemTypes.NONE) {
            if (equipper.isSneaking()) {
                for (int i = 0; i < 6; i++) {
                    if (trait.get(i) != null && trait.get(i).getType() != Material.AIR) {
                        equipper.getWorld().dropItemNaturally(toEquip.getEntity().getLocation(), trait.get(i));
                        trait.set(i, null);
                    }
                }
                Messaging.sendTr(equipper, Messages.EQUIPMENT_EDITOR_ALL_ITEMS_REMOVED, toEquip.getName());
            } else {
                return;
            }
        } else { if (type == ItemTypes.SKULL || type == ItemTypes.PUMPKIN || type == ItemTypes.SEA_LANTERN
                || type == ItemTypes.LEATHER_HELMET || type == ItemTypes.CHAINMAIL_HELMET || type == ItemTypes.GOLDEN_HELMET
                || type == ItemTypes.IRON_HELMET || type == ItemTypes.DIAMOND_HELMET) {
                if (!equipper.isSneaking()) {
                    slot = EquipmentSlot.HELMET;
                }
        } else if (type == ItemTypes.ELYTRA || type == ItemTypes.LEATHER_CHESTPLATE || type == ItemTypes.CHAINMAIL_CHESTPLATE
                || type == ItemTypes.GOLDEN_CHESTPLATE || type == ItemTypes.IRON_CHESTPLATE || type == ItemTypes.DIAMOND_CHESTPLATE) {
                if (!equipper.isSneaking()) {
                    slot = EquipmentSlot.CHESTPLATE;
                }
        } else if (type == ItemTypes.LEATHER_LEGGINGS || type == ItemTypes.CHAINMAIL_LEGGINGS
                || type == ItemTypes.GOLDEN_LEGGINGS || type == ItemTypes.IRON_LEGGINGS || type == ItemTypes.DIAMOND_LEGGINGS) {
                if (!equipper.isSneaking()) {
                    slot = EquipmentSlot.LEGGINGS;
                }
        } else if (type == ItemTypes.LEATHER_BOOTS || type == ItemTypes.CHAINMAIL_BOOTS
                || type == ItemTypes.GOLDEN_BOOTS || type == ItemTypes.IRON_BOOTS || type == ItemTypes.DIAMOND_BOOTS) {
                if (!equipper.isSneaking()) {
                    slot = EquipmentSlot.BOOTS;
                }
        }
        // Drop any previous equipment on the ground
        ItemStack equippedItem = trait.get(slot);
        if (equippedItem != null && equippedItem.getType() != Material.AIR) {
            equipper.getWorld().dropItemNaturally(toEquip.getEntity().getLocation(), equippedItem);
        }

        // Now edit the equipment based on the slot
        if (type != ItemTypes.NONE) {
            // Set the proper slot with one of the item
            ItemStack clone = hand.clone();
            clone.setAmount(1);
            trait.set(slot, clone);
            hand.setAmount(hand.getAmount() - 1);
            equipper.getInventory().setItemInMainHand(hand);
        }
    }
}
