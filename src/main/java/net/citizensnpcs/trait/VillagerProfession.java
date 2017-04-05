package net.citizensnpcs.trait;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.data.type.Professions;
import org.spongepowered.api.entity.living.Villager;

@TraitName("profession")
public class VillagerProfession extends Trait {
    private Profession profession = Professions.FARMER;

    public VillagerProfession() {
        super("profession");
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        try {
            profession = Profession.valueOf(key.getString(""));
            if (profession == Professions.NORMAL) {
                profession = Professions.FARMER;
            }
        } catch (IllegalArgumentException ex) {
            throw new NPCLoadException("Invalid profession.");
        }
    }

    @Override
    public void onSpawn() {
        if (npc.getEntity() instanceof Villager) {
            ((Villager) npc.getEntity()).setProfession(profession);
        }
    }

    @Override
    public void save(DataKey key) {
        key.setString("", profession.name());
    }

    public void setProfession(Profession profession) {
        if (profession == Professions.NORMAL) {
            profession = Professions.FARMER;
        }
        this.profession = profession;
        if (npc.getEntity() instanceof Villager) {
            ((Villager) npc.getEntity()).setProfession(profession);
        }
    }

    @Override
    public String toString() {
        return "Profession{" + profession + "}";
    }
}