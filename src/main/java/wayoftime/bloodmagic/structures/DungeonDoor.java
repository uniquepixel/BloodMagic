package wayoftime.bloodmagic.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.structures.DungeonDoor}. A door's
 * {@code roomList} strings encode which room pool the next room should be pulled from: a plain
 * "namespace:path" resource location, a "#"-prefixed one meaning "only used if a special room of
 * this type is buffered up" (see {@link SpecialDungeonRoomPoolRegistry}), or a "$"-prefixed one
 * meaning "use this pool if this branch has hit its max depth and needs to dead-end".
 */
public class DungeonDoor {
    public BlockPos doorPos;
    public Direction doorDir;
    public String doorType;
    private final List<String> roomList; // List of room pools

    public AreaDescriptor descriptor;

    public DungeonDoor(BlockPos doorPos, Direction doorDir, String doorType, List<String> roomList, AreaDescriptor desc) {
        this.doorPos = doorPos;
        this.doorDir = doorDir;
        this.doorType = doorType;
        this.roomList = roomList;
        this.descriptor = desc;
    }

    public List<ResourceLocation> getRoomList() {
        List<ResourceLocation> rlRoomList = new ArrayList<>();
        for (String room : roomList) {
            if (!room.startsWith("#") && !room.startsWith("$")) {
                rlRoomList.add(ResourceLocation.parse(room));
            }
        }

        return rlRoomList;
    }

    public List<ResourceLocation> getSpecialRoomList() {
        List<ResourceLocation> rlRoomList = new ArrayList<>();
        for (String room : roomList) {
            if (room.startsWith("#")) {
                String[] splitString = room.split("#");
                rlRoomList.add(ResourceLocation.parse(splitString[1]));
            }
        }

        return rlRoomList;
    }

    public List<ResourceLocation> getDeadendRoomList() {
        List<ResourceLocation> rlRoomList = new ArrayList<>();
        for (String room : roomList) {
            if (room.startsWith("$")) {
                String[] splitString = room.split("\\$");
                rlRoomList.add(ResourceLocation.parse(splitString[1]));
            }
        }

        if (rlRoomList.isEmpty()) {
            rlRoomList.add(ModRoomPools.DEFAULT_DEADEND);
        }

        return rlRoomList;
    }

    public boolean isDeadend(int roomDepth, int maxRoomDepth) {
        return (roomDepth < maxRoomDepth - 1);
    }
}
