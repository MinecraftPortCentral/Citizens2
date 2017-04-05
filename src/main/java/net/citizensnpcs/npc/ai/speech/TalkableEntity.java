package net.citizensnpcs.npc.ai.speech;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.ai.speech.Talkable;
import net.citizensnpcs.api.ai.speech.VocalChord;
import net.citizensnpcs.api.ai.speech.event.SpeechBystanderEvent;
import net.citizensnpcs.api.ai.speech.event.SpeechTargetedEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;

public class TalkableEntity implements Talkable {
    Entity entity;

    public TalkableEntity(Entity entity) {
        this.entity = entity;
    }

    public TalkableEntity(NPC npc) {
        this.entity = npc.getEntity();
    }

    public TalkableEntity(Player player) {
        this.entity = player;
    }

    /**
     * Used to compare a LivingEntity to this TalkableEntity
     *
     * @return 0 if the Entities are the same, 1 if they are not, -1 if the object compared is not a valid LivingEntity
     */
    @Override
    public int compareTo(Object o) {
        // If not living entity, return -1
        if (!(o instanceof Entity)) {
            return -1;
            // If NPC and matches, return 0
        } else if (CitizensAPI.getNPCRegistry().isNPC((Entity) o) && CitizensAPI.getNPCRegistry().isNPC(this.entity)
                && CitizensAPI.getNPCRegistry().getNPC((Entity) o).getUniqueId()
                        .equals(CitizensAPI.getNPCRegistry().getNPC(this.entity).getUniqueId())) {
            return 0;
        } else if (this.entity.equals(o)) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public String getName() {
        if (CitizensAPI.getNPCRegistry().isNPC(this.entity)) {
            return CitizensAPI.getNPCRegistry().getNPC(this.entity).getName();
        } else if (this.entity instanceof Player) {
            return ((Player) this.entity).getName();
        } else {
            return this.entity.getType().getId().replace("_", " ");
        }
    }

    private void talk(String message) {
        if (this.entity instanceof Player && !CitizensAPI.getNPCRegistry().isNPC(this.entity))
            Messaging.send((Player) this.entity, message);
    }

    @Override
    public void talkNear(SpeechContext context, String text, VocalChord vocalChord) {
        SpeechBystanderEvent event = new SpeechBystanderEvent(this, context, text, vocalChord);
        Sponge.getEventManager().post(event);
        if (event.isCancelled())
            return;
        talk(event.getMessage());
    }

    @Override
    public void talkTo(SpeechContext context, String text, VocalChord vocalChord) {
        SpeechTargetedEvent event = new SpeechTargetedEvent(this, context, text, vocalChord);
        Sponge.getEventManager().post(event);
        if (event.isCancelled())
            return;
        talk(event.getMessage());
    }

}
