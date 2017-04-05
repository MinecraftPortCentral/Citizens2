package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.animal.Sheep;
import org.spongepowered.api.event.Listener;

@TraitName("sheeptrait")
public class SheepTrait extends Trait {
    @Persist("color")
    private DyeColor color = DyeColors.WHITE;
    @Persist("sheared")
    private boolean sheared = false;

    public SheepTrait() {
        super("sheeptrait");
    }

    @Listener
    public void onPlayerShearEntityEvent(PlayerShearEntityEvent event) {
        if (npc != null && npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onSpawn() {
    }

    @Override
    public void run() {
        if (npc.getEntity() instanceof Sheep) {
            Sheep sheep = (Sheep) npc.getEntity();
            sheep.setSheared(sheared);
            sheep.setColor(color);
        }
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }

    public void setSheared(boolean sheared) {
        this.sheared = sheared;
    }

    public boolean toggleSheared() {
        return sheared = !sheared;
    }
}
