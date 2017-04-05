package net.citizensnpcs.npc.ai;

import java.util.List;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.ai.AbstractPathStrategy;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.TargetType;
import net.citizensnpcs.api.ai.event.CancelReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.util.NMS;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class MCNavigationStrategy extends AbstractPathStrategy {
    private final Entity handle;
    private final MCNavigator navigator;
    private final NavigatorParameters parameters;
    private final Location<World> target;

    MCNavigationStrategy(final NPC npc, Iterable<Vector3d> path, NavigatorParameters params) {
        super(TargetType.LOCATION);
        List<Vector3d> list = Lists.newArrayList(path);
        this.target = list.get(list.size() - 1).toLocation(npc.getStoredLocation().getExtent());
        this.parameters = params;
        handle = npc.getEntity();
        this.navigator = NMS.getTargetNavigator(npc.getEntity(), list, params);
    }

    MCNavigationStrategy(final NPC npc, Location dest, NavigatorParameters params) {
        super(TargetType.LOCATION);
        this.target = dest;
        this.parameters = params;
        handle = npc.getEntity();
        this.navigator = NMS.getTargetNavigator(npc.getEntity(), dest, params);
    }

    private double distanceSquared() {
        return handle.getLocation().getPosition().distanceSquared(target.getPosition());
    }

    @Override
    public Iterable<Vector3d> getPath() {
        return navigator.getPath();
    }

    @Override
    public Location<World> getTargetAsLocation() {
        return target;
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.LOCATION;
    }

    @Override
    public void stop() {
        navigator.stop();
    }

    @Override
    public String toString() {
        return "MCNavigationStrategy [target=" + target + "]";
    }

    @Override
    public boolean update() {
        if (navigator.getCancelReason() != null) {
            setCancelReason(navigator.getCancelReason());
        }
        if (getCancelReason() != null)
            return true;
        boolean wasFinished = navigator.update();
        parameters.run();
        if (distanceSquared() < parameters.distanceMargin()) {
            stop();
            return true;
        }
        return wasFinished;
    }

    public static interface MCNavigator {
        CancelReason getCancelReason();

        Iterable<Vector3d> getPath();

        void stop();

        boolean update();
    }

    private static final Location<World> HANDLE_LOCATION = new Location<World>(null, 0, 0, 0);
}
