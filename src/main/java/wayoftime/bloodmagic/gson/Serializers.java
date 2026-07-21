package wayoftime.bloodmagic.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.lang.reflect.Type;

/**
 * Trimmed port of 1.20.1's {@code wayoftime.bloodmagic.gson.Serializers}. The original registered
 * adapters for Direction/ResourceLocation/ItemStack/BlockPos plus an entity-data serializer for a
 * Will-type enum; only the {@link BlockPos} and {@link Direction} adapters are actually needed by
 * anything ported from {@code structures/} (DungeonRoom's fields never round-trip a ResourceLocation
 * or ItemStack through Gson - those are plain Strings), so only those two are kept. Unlike upstream
 * (which only overrode {@code deserialize} for Direction and relied on Gson's default enum handling
 * for serialize, producing uppercase names via delegation), both directions are implemented
 * explicitly here for both types to avoid relying on Gson's delegate-adapter recursion semantics -
 * this still deserializes the exact JSON shape used by every copied {@code schematics/*.json} file
 * ({@code {"x":.,"y":.,"z":.}} for BlockPos, lowercase serialized name for Direction).
 */
public class Serializers {
    public static final JsonSerializer<BlockPos> BLOCKPOS_SERIALIZE = (src, typeOfSrc, context) -> {
        JsonObject object = new JsonObject();
        object.addProperty("x", src.getX());
        object.addProperty("y", src.getY());
        object.addProperty("z", src.getZ());
        return object;
    };

    public static final JsonDeserializer<BlockPos> BLOCKPOS_DESERIALIZE = (json, typeOfT, context) -> {
        JsonObject object = json.getAsJsonObject();
        int x = object.get("x").getAsInt();
        int y = object.get("y").getAsInt();
        int z = object.get("z").getAsInt();
        return new BlockPos(x, y, z);
    };

    public static final JsonSerializer<Direction> DIRECTION_SERIALIZE = (src, typeOfSrc, context) -> new com.google.gson.JsonPrimitive(src.getSerializedName());

    public static final JsonDeserializer<Direction> DIRECTION_DESERIALIZE = new JsonDeserializer<Direction>() {
        @Override
        public Direction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Direction.byName(json.getAsString());
        }
    };

    public static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(BlockPos.class, BLOCKPOS_SERIALIZE)
            .registerTypeAdapter(BlockPos.class, BLOCKPOS_DESERIALIZE)
            .registerTypeAdapter(Direction.class, DIRECTION_SERIALIZE)
            .registerTypeAdapter(Direction.class, DIRECTION_DESERIALIZE)
            .create();
}
