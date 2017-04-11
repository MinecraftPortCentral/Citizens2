package net.citizensnpcs.trait.waypoint;

import java.util.Iterator;

import net.citizensnpcs.api.event.CitizensEvent;
import org.spongepowered.api.event.cause.Cause;


public class LinearWaypointsCompleteEvent extends CitizensEvent {
    private Iterator<Waypoint> next;
    private final WaypointProvider provider;

    public LinearWaypointsCompleteEvent(WaypointProvider provider, Iterator<Waypoint> next, Cause cause) {
        super(cause);
        this.next = next;
        this.provider = provider;
    }

    public Iterator<Waypoint> getNextWaypoints() {
        return next;
    }

    public WaypointProvider getWaypointProvider() {
        return provider;
    }

    public void setNextWaypoints(Iterator<Waypoint> waypoints) {
        this.next = waypoints;
    }
}
