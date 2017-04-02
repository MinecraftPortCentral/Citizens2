package net.citizensnpcs.trait;

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Anchor;
import net.citizensnpcs.util.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@TraitName("anchors")
public class Anchors extends Trait {
    private final List<Anchor> anchors = new ArrayList<Anchor>();

    public Anchors() {
        super("anchors");
    }

    public boolean addAnchor(String name, Location<World> location) {
        Anchor newAnchor = new Anchor(name, location);
        if (anchors.contains(newAnchor))
            return false;
        anchors.add(newAnchor);
        return true;
    }

    @Listener
    public void checkWorld(LoadWorldEvent event) {
        for (Anchor anchor : anchors)
            if (!anchor.isLoaded())
                anchor.load();
    }

    public Anchor getAnchor(String name) {
        for (Anchor anchor : anchors)
            if (anchor.getName().equalsIgnoreCase(name))
                return anchor;
        return null;
    }

    public List<Anchor> getAnchors() {
        return anchors;
    }

    @Override
    public void load(DataKey key) throws NPCLoadException {
        for (DataKey sub : key.getRelative("list").getIntegerSubKeys()) {
            String[] parts = sub.getString("").split(";");
            Location<World> location;
            try {
                location = new Location<World>(Sponge.getServer().getWorld(parts[1]).get(), Double.valueOf(parts[2]),
                        Double.valueOf(parts[3]), Double.valueOf(parts[4]));
                anchors.add(new Anchor(parts[0], location));
            } catch (NumberFormatException e) {
                Messaging.logTr(Messages.SKIPPING_INVALID_ANCHOR, sub.name(), e.getMessage());
            } catch (NullPointerException e) {
                // Invalid world/location/etc. Still enough data to build an
                // unloaded anchor
                anchors.add(new Anchor(parts[0], sub.getString("").split(";", 2)[1]));
            }
        }
    }

    public boolean removeAnchor(Anchor anchor) {
        if (anchors.contains(anchor)) {
            anchors.remove(anchor);
            return true;
        }
        return false;
    }

    @Override
    public void save(DataKey key) {
        key.removeKey("list");
        for (int i = 0; i < anchors.size(); i++)
            key.setString("list." + String.valueOf(i), anchors.get(i).stringValue());
    }

}
