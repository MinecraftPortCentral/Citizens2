package net.citizensnpcs.trait.waypoint.triggers;

import java.util.Collection;
import java.util.List;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.util.PlayerAnimation;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class AnimationTrigger implements WaypointTrigger {
    @Persist(required = true)
    private List<PlayerAnimation> animations;

    public AnimationTrigger() {
    }

    public AnimationTrigger(Collection<PlayerAnimation> collection) {
        animations = Lists.newArrayList(collection);
    }

    @Override
    public String description() {
        return String.format("Animation Trigger [animating %s]", Joiner.on(", ").join(animations));
    }

    @Override
    public void onWaypointReached(NPC npc, Location<World> waypoint) {
        if (npc.getEntity().getType() != EntityTypes.PLAYER)
            return;
        Player player = (Player) npc.getEntity();
        for (PlayerAnimation animation : animations) {
            animation.play(player);
        }
    }
}
