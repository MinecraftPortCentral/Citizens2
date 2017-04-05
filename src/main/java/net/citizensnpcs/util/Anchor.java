package net.citizensnpcs.util;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/*
 * Anchor object which holds a Location with a name to identify.
 */

public class Anchor {
    private Location<World> location;
    private final String name;

    // Needed for Anchors defined that can't currently have a valid 'Location'
    private final String unloaded_value;

    // Allow construction of anchor for unloaded worlds
    public Anchor(String name, String unloaded_value) {
        this.location = null;
        this.unloaded_value = unloaded_value;
        this.name = name;
    }

    public Anchor(String name, Location<World> location) {
        this.location = location;
        this.name = name;
        this.unloaded_value = location.getExtent().getName() + ';' + location.getX() + ';' + location.getY() + ';'
                + location.getZ();
    }

    public boolean isLoaded() {
        return location != null;
    }

    public boolean load() {
        try {
            String[] parts = getUnloadedValue();
            this.location = new Location<World>(Sponge.getServer().getWorld(parts[0]).get(), Double.valueOf(parts[1]), Double.valueOf(parts[2]),
                    Double.valueOf(parts[3]));
        } catch (Exception e) {
            // Still not able to be loaded
        }
        return location != null;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;
        if (object == this)
            return true;
        if (object.getClass() != getClass())
            return false;

        Anchor op = (Anchor) object;
        return new EqualsBuilder().append(name, op.name).isEquals();
    }

    public Location<World> getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns a String[] of the 'world_name, x, y, z' information needed to create the Location that is associated with
     * the Anchor, in that order.
     *
     * @return a String array of the anchor's location data
     */
    public String[] getUnloadedValue() {
        return unloaded_value.split(";");
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 21).append(name).toHashCode();
    }

    // A friendly representation for use in saves.yml
    public String stringValue() {
        return name + ';' + unloaded_value;
    }

    @Override
    public String toString() {
        String[] parts = getUnloadedValue();
        return "Anchor{Name='" + name + "';World='" + parts[0] + "';Location='" + parts[1] + ',' + parts[2] + ','
                + parts[3] + "';}";
    }

}