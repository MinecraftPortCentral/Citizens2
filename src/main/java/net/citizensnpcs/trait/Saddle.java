package net.citizensnpcs.trait;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.spongepowered.api.entity.living.animal.Pig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;

@TraitName("saddle")
public class Saddle extends Trait implements Toggleable {
    private boolean pig;
    @Persist("")
    private boolean saddle;

    public Saddle() {
        super("saddle");
    }

    @Listener
    public void onPlayerInteractEntity(InteractEntityEvent event) {
        if (pig && npc.equals(CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked())))
            event.setCancelled(true);
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Pig) {
            ((Pig) npc.getEntity()).setSaddle(saddle);
            pig = true;
        } else
            pig = false;
    }

    @Override
    public boolean toggle() {
        saddle = !saddle;
        if (pig)
            ((Pig) npc.getEntity()).setSaddle(saddle);
        return saddle;
    }

    @Override
    public String toString() {
        return "Saddle{" + saddle + "}";
    }
}