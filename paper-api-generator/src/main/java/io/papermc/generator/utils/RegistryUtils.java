package io.papermc.generator.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;
import io.papermc.generator.utils.experimental.CollectingContext;
import io.papermc.paper.registry.RegistryKey;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.UpdateOneTwentyOneRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.bukkit.MinecraftExperimental;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RegistryUtils {

    private static final Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryBootstrap<?>> VANILLA_REGISTRY_ENTRIES = VanillaRegistries.BUILDER.entries.stream()
        .collect(Collectors.toMap(RegistrySetBuilder.RegistryStub::key, RegistrySetBuilder.RegistryStub::bootstrap));

    private static final Map<ResourceKey<? extends Registry<?>>, RegistrySetBuilder.RegistryBootstrap<?>> EXPERIMENTAL_REGISTRY_ENTRIES = UpdateOneTwentyOneRegistries.BUILDER.entries.stream()
        .collect(Collectors.toMap(RegistrySetBuilder.RegistryStub::key, RegistrySetBuilder.RegistryStub::bootstrap));

    @SuppressWarnings("unchecked")
    public static <T> Set<ResourceKey<T>> collectExperimentalKeys(final Registry<T> registry) {
        final RegistrySetBuilder.@Nullable RegistryBootstrap<T> experimentalBootstrap = (RegistrySetBuilder.RegistryBootstrap<T>) EXPERIMENTAL_REGISTRY_ENTRIES.get(registry.key());
        if (experimentalBootstrap == null) {
            return Collections.emptySet();
        }
        final Set<ResourceKey<T>> experimental = Collections.newSetFromMap(new IdentityHashMap<>());
        final CollectingContext<T> experimentalCollector = new CollectingContext<>(experimental, registry);
        experimentalBootstrap.run(experimentalCollector);

        final RegistrySetBuilder.@Nullable RegistryBootstrap<T> vanillaBootstrap = (RegistrySetBuilder.RegistryBootstrap<T>) VANILLA_REGISTRY_ENTRIES.get(registry.key());
        if (vanillaBootstrap != null) {
            final Set<ResourceKey<T>> vanilla = Collections.newSetFromMap(new IdentityHashMap<>());
            final CollectingContext<T> vanillaCollector = new CollectingContext<>(vanilla, registry);
            vanillaBootstrap.run(vanillaCollector);
            return Sets.difference(experimental, vanilla);
        }
        return experimental;
    }

    public static final Map<RegistryKey<?>, String> REGISTRY_KEY_FIELD_NAMES;
    static {
        final Map<RegistryKey<?>, String> map = new IdentityHashMap<>();
        try {
            for (final Field field : RegistryKey.class.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers()) || field.getType() != RegistryKey.class) {
                    continue;
                }
                map.put((RegistryKey<?>) field.get(null), field.getName());
            }
            REGISTRY_KEY_FIELD_NAMES = Map.copyOf(map);
        } catch (final ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static FeatureFlag onlyOneFlag(final FeatureFlagSet featureFlags) {
        for (FeatureFlag flag : FeatureFlags.REGISTRY.names.values()) {
            if (featureFlags.contains(flag)) {
                return flag;
            }
        }

        throw new UnsupportedOperationException("Don't know how to compact two feature flags into one!");
    }

    public static MinecraftExperimental.Requires toBukkitAnnotationMember(FeatureFlag flag) {
        final MinecraftExperimental.Requires requires;
        if (flag == FeatureFlags.UPDATE_1_21) {
            requires = MinecraftExperimental.Requires.UPDATE_1_21;
        } else if (flag == FeatureFlags.BUNDLE) {
            requires = MinecraftExperimental.Requires.BUNDLE;
        } else if (flag == FeatureFlags.TRADE_REBALANCE) {
            requires = MinecraftExperimental.Requires.TRADE_REBALANCE;
        } else {
            throw new UnsupportedOperationException("Don't know that feature flag");
        }

        return requires;
    }

    public static FeatureFlag getFeatureFlag(final String name) {
        return FeatureFlags.REGISTRY.names.get(new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, name));
    }
}
