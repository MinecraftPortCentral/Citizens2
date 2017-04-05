package net.citizensnpcs.npc.ai.speech;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.citizensnpcs.api.ai.speech.SpeechFactory;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;
import com.google.common.base.Preconditions;
import org.spongepowered.api.entity.Entity;

public class CitizensSpeechFactory implements SpeechFactory {
    Map<String, Class<? extends VocalChord>> registered = new HashMap<String, Class<? extends VocalChord>>();

    @Override
    public VocalChord getVocalChord(Class<? extends VocalChord> clazz) {
        // Return a new instance of the VocalChord specified
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public VocalChord getVocalChord(String name) {
        // Check if VocalChord name is a registered type
        if (isRegistered(name))
            // Return a new instance of the VocalChord specified
            try {
                return this.registered.get(name.toLowerCase()).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        return null;
    }

    @Override
    public String getVocalChordName(Class<? extends VocalChord> clazz) {
        // Get the name of a VocalChord class that has been registered
        for (Entry<String, Class<? extends VocalChord>> vocalChord : this.registered.entrySet())
            if (vocalChord.getValue() == clazz)
                return vocalChord.getKey();

        return null;
    }

    @Override
    public boolean isRegistered(String name) {
        return this.registered.containsKey(name.toLowerCase());
    }

    @Override
    public Talkable newTalkableEntity(Entity entity) {
        if (entity == null)
            return null;
        return new TalkableEntity(entity);
    }

    @Override
    public void register(Class<? extends VocalChord> clazz, String name) {
        Preconditions.checkNotNull(name, "info cannot be null");
        Preconditions.checkNotNull(clazz, "vocalchord cannot be null");
        if (this.registered.containsKey(name.toLowerCase()))
            throw new IllegalArgumentException("vocalchord name already registered");
        this.registered.put(name.toLowerCase(), clazz);
    }

}
