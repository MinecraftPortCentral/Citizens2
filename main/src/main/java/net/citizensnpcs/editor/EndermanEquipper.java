package net.citizensnpcs.editor;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.monster.Enderman;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;

public class EndermanEquipper implements Equipper {
    @Override
    public void equip(Player equipper, NPC npc) {
        ItemStack hand = equipper.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        if (hand != null && !hand.getItem().getBlock().isPresent()) {
            Messaging.sendErrorTr(equipper, Messages.EQUIPMENT_EDITOR_INVALID_BLOCK);
            return;
        }

        BlockType blockType = hand.getItem().getBlock().get();
        MaterialData carried = ((Enderman) npc.getEntity()).getCarriedMaterial();
        if (carried.getItemType() == BlockTypes.AIR) {
            if (blockType == BlockTypes.AIR) {
                Messaging.sendErrorTr(equipper, Messages.EQUIPMENT_EDITOR_INVALID_BLOCK);
                return;
            }
        } else {
            equipper.getWorld().dropItemNaturally(npc.getEntity().getLocation(), carried.toItemStack(1));
            ((Enderman) npc.getEntity()).setCarriedMaterial(hand.getData());
        }

        ItemStack set = hand.copy();
        if (set.getType() != BlockTypes.AIR) {
            set.setAmount(1);
            hand.setAmount(hand.getAmount() - 1);
            equipper.getInventory().setItemInMainHand(hand);
        }
        npc.getTrait(Equipment.class).set(0, set);
    }
}
