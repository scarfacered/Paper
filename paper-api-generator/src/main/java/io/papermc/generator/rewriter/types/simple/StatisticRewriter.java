package io.papermc.generator.rewriter.types.simple;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import io.papermc.generator.rewriter.types.EnumRegistryRewriter;
import io.papermc.generator.rewriter.types.Types;
import io.papermc.generator.utils.ClassHelper;
import io.papermc.generator.utils.Formatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.bukkit.Statistic;

import static io.papermc.generator.utils.Formatting.quoted;

@Deprecated(forRemoval = true)
public class StatisticRewriter {

    private static final Map<String, String> FIELD_RENAMES = ImmutableMap.<String, String>builder()
        .put("DROP", "DROP_COUNT")
        .put("DROPPED", "DROP")
        .put("PICKED_UP", "PICKUP")
        .put("PLAY_TIME", "PLAY_ONE_MINUTE")
        .put("CROUCH_TIME", "SNEAK_TIME")
        .put("MINED", "MINE_BLOCK")
        .put("USED", "USE_ITEM")
        .put("BROKEN", "BREAK_ITEM")
        .put("CRAFTED", "CRAFT_ITEM")
        .put("KILLED", "KILL_ENTITY")
        .put("KILLED_BY", "ENTITY_KILLED_BY")
        .put("EAT_CAKE_SLICE", "CAKE_SLICES_EATEN")
        .put("FILL_CAULDRON", "CAULDRON_FILLED")
        .put("USE_CAULDRON", "CAULDRON_USED")
        .put("CLEAN_ARMOR", "ARMOR_CLEANED")
        .put("CLEAN_BANNER", "BANNER_CLEANED")
        .put("INTERACT_WITH_BREWINGSTAND", "BREWINGSTAND_INTERACTION")
        .put("INTERACT_WITH_BEACON", "BEACON_INTERACTION")
        .put("INSPECT_DROPPER", "DROPPER_INSPECTED")
        .put("INSPECT_HOPPER", "HOPPER_INSPECTED")
        .put("INSPECT_DISPENSER", "DISPENSER_INSPECTED")
        .put("PLAY_NOTEBLOCK", "NOTEBLOCK_PLAYED")
        .put("TUNE_NOTEBLOCK", "NOTEBLOCK_TUNED")
        .put("POT_FLOWER", "FLOWER_POTTED")
        .put("TRIGGER_TRAPPED_CHEST", "TRAPPED_CHEST_TRIGGERED")
        .put("OPEN_ENDERCHEST", "ENDERCHEST_OPENED")
        .put("ENCHANT_ITEM", "ITEM_ENCHANTED")
        .put("PLAY_RECORD", "RECORD_PLAYED")
        .put("INTERACT_WITH_FURNACE", "FURNACE_INTERACTION")
        .put("INTERACT_WITH_CRAFTING_TABLE", "CRAFTING_TABLE_INTERACTION")
        .put("OPEN_CHEST", "CHEST_OPENED")
        .put("OPEN_SHULKER_BOX", "SHULKER_BOX_OPENED").build();

    public static class Custom extends EnumRegistryRewriter<ResourceLocation, Statistic> {

        public Custom(final String pattern) {
            super(Statistic.class, Registries.CUSTOM_STAT, pattern, false);
        }

        @Override
        protected String rewriteEnumName(Holder.Reference<ResourceLocation> reference) {
            String internalName = super.rewriteEnumName(reference);
            return FIELD_RENAMES.getOrDefault(internalName, internalName);
        }
    }

    public static class CraftCustom extends EnumRegistryRewriter<ResourceLocation, Statistic> {

        private static final Map<String, String> INTERNAL_FIELD_RENAMES = Map.of(
            "SNEAK_TIME", "CROUCH_TIME"
        );

        public CraftCustom(final String pattern) {
            super(Types.CRAFT_STATISTIC, Registries.CUSTOM_STAT, pattern, true);
        }

        @Override
        protected String rewriteEnumName(Holder.Reference<ResourceLocation> reference) {
            String internalName = super.rewriteEnumName(reference);
            return FIELD_RENAMES.getOrDefault(internalName, internalName);
        }

        @Override
        protected String rewriteEnumValue(Holder.Reference<ResourceLocation> reference) {
            String internalName = Formatting.formatKeyAsField(reference.key().location().getPath());
            return "%s.%s".formatted(Stats.class.getSimpleName(), INTERNAL_FIELD_RENAMES.getOrDefault(internalName, internalName));
        }
    }

    public static class Type extends EnumRegistryRewriter<StatType<?>, Statistic> {

        private static final Map<Class<?>, String> TYPE_MAPPING = Map.of(
            Item.class, "ITEM",
            Block.class, "BLOCK",
            EntityType.class, "ENTITY"
        );

        private static final Map<StatType<?>, Class<?>> FIELD_GENERIC_TYPE;
        static {
            final Map<StatType<?>, Class<?>> map = new HashMap<>();

            try {
                for (final Field field : Stats.class.getDeclaredFields()) {
                    if (field.getType() != StatType.class) {
                        continue;
                    }

                    int mod = field.getModifiers();
                    if (Modifier.isPublic(mod) & Modifier.isStatic(mod) & Modifier.isFinal(mod)) {
                        java.lang.reflect.Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        map.put((StatType<?>) field.get(null), ClassHelper.eraseType(genericType));
                    }
                }
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException(ex);
            }
            FIELD_GENERIC_TYPE = Collections.unmodifiableMap(map);
        }

        public Type(final String pattern) {
            super(Statistic.class, Registries.STAT_TYPE, pattern, true);
        }

        @Override
        protected Iterable<Holder.Reference<StatType<?>>> getValues() {
            return BuiltInRegistries.STAT_TYPE.holders().filter(reference -> reference.value() != Stats.CUSTOM)
                .sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).toList();
        }

        @Override
        protected String rewriteEnumName(Holder.Reference<StatType<?>> reference) {
            String internalName = super.rewriteEnumName(reference);
            return FIELD_RENAMES.getOrDefault(internalName, internalName);
        }

        @Override
        protected String rewriteEnumValue(Holder.Reference<StatType<?>> reference) {
            Class<?> genericType = FIELD_GENERIC_TYPE.get(reference.value());
            if (!TYPE_MAPPING.containsKey(genericType)) {
                throw new IllegalStateException("Unable to translate stat type generic " + genericType.getCanonicalName() + " into the api!");
            }
            return "%s.%s".formatted(Statistic.Type.class.getSimpleName(), TYPE_MAPPING.get(genericType)); // find a more direct way?
        }
    }

    public static class CraftType extends EnumRegistryRewriter<StatType<?>, Statistic> {

        public CraftType(final String pattern) {
            super(Types.CRAFT_STATISTIC, Registries.STAT_TYPE, pattern, true);
        }

        @Override
        protected Iterable<Holder.Reference<StatType<?>>> getValues() {
            return BuiltInRegistries.STAT_TYPE.holders().filter(reference -> reference.value() != Stats.CUSTOM)
                .sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).toList();
        }

        @Override
        protected String rewriteEnumName(Holder.Reference<StatType<?>> reference) {
            String internalName = super.rewriteEnumName(reference);
            return FIELD_RENAMES.getOrDefault(internalName, internalName);
        }

        @Override
        protected String rewriteEnumValue(Holder.Reference<StatType<?>> reference) {
            return "new %s(%s)".formatted(ResourceLocation.class.getSimpleName(), quoted(reference.key().location().getPath()));
        }
    }
}
