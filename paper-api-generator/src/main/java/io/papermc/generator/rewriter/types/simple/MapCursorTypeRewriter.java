package io.papermc.generator.rewriter.types.simple;

import io.papermc.generator.rewriter.types.EnumRegistryRewriter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.bukkit.map.MapCursor;

public class MapCursorTypeRewriter extends EnumRegistryRewriter<MapDecorationType, MapCursor.Type> {

    public MapCursorTypeRewriter(final String pattern) {
        super(MapCursor.Type.class, Registries.MAP_DECORATION_TYPE, pattern, true);
    }

    @Override
    protected String rewriteEnumValue(Holder.Reference<MapDecorationType> reference) {
        return "%d, %s".formatted(BuiltInRegistries.MAP_DECORATION_TYPE.getId(reference.value()), super.rewriteEnumValue(reference));
    }
}
