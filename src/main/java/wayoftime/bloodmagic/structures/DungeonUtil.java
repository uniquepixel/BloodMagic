package wayoftime.bloodmagic.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.structures.DungeonUtil}.
 */
public class DungeonUtil {
    public static Direction rotate(Mirror mirror, Rotation rotation, Direction original) {
        return rotation.rotate(mirror.mirror(original));
    }

    public static Direction reverseRotate(Mirror mirror, Rotation rotation, Direction original) {
        return mirror.mirror(getOppositeRotation(rotation).rotate(original));
    }

    public static Direction getFacingForSettings(StructurePlaceSettings settings, Direction original) {
        return rotate(settings.getMirror(), settings.getRotation(), original);
    }

    public static Rotation getOppositeRotation(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
    }

    public static void addRoom(Map<Direction, List<BlockPos>> doorMap, Direction facing, BlockPos offsetPos) {
        if (doorMap.containsKey(facing)) {
            doorMap.get(facing).add(offsetPos);
        } else {
            List<BlockPos> doorList = new ArrayList<>();
            doorList.add(offsetPos);
            doorMap.put(facing, doorList);
        }
    }
}
