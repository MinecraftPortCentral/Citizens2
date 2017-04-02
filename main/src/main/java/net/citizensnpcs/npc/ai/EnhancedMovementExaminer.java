package net.citizensnpcs.npc.ai;

import java.util.List;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;

import net.citizensnpcs.api.astar.pathfinder.BlockSource;
import net.citizensnpcs.api.astar.pathfinder.NeighbourGeneratorBlockExaminer;
import net.citizensnpcs.api.astar.pathfinder.PathPoint;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class EnhancedMovementExaminer implements NeighbourGeneratorBlockExaminer {
    @Override
    public float getCost(Location<World> source, PathPoint point) {
        return 0;
    }

    @Override
    public List<PathPoint> getNeighbours(Location<World> source, PathPoint point) {
        Vector3d location = point.getVector();
        List<PathPoint> neighbours = Lists.newArrayList();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0)
                        continue;
                    if (x != 0 && z != 0)
                        continue;
                    Vector3d mod = location.clone().add(new Vector3d(x, y, z));
                    if (mod.equals(location))
                        continue;
                    neighbours.add(point.createAtOffset(mod));
                }
            }
        }
        return null;
    }

    @Override
    public PassableState isPassable(Location<World> source, PathPoint point) {
        return null;
    }
}
