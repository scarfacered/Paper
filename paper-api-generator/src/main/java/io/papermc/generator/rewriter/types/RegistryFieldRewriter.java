package io.papermc.generator.rewriter.types;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import io.papermc.generator.Main;
import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.replace.SearchMetadata;
import io.papermc.generator.rewriter.replace.SearchReplaceRewriter;
import io.papermc.generator.rewriter.utils.Annotations;
import io.papermc.generator.utils.Formatting;
import io.papermc.generator.utils.RegistryUtils;
import io.papermc.generator.utils.experimental.ExperimentalHelper.FlagSets;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static io.papermc.generator.utils.Formatting.quoted;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class RegistryFieldRewriter<T> extends SearchReplaceRewriter {

    private static final Map<Class<?>, String> REGISTRY_FIELD_NAMES;
    static {
        final Map<Class<?>, String> map = new IdentityHashMap<>();
        for (final Field field : org.bukkit.Registry.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(Deprecated.class) || field.getType() != org.bukkit.Registry.class) {
                continue;
            }

            int mod = field.getModifiers();
            if (Modifier.isPublic(mod) & Modifier.isStatic(mod) & Modifier.isFinal(mod)) {
                map.put((Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0], field.getName());
            }
        }
        REGISTRY_FIELD_NAMES = Collections.unmodifiableMap(map);
    }

    private final Registry<T> registry;
    private final Supplier<Set<ResourceKey<T>>> experimentalKeys;
    private final boolean isFilteredRegistry;
    private final boolean isInterface;
    private final String fetchMethod;

    private RegistryFieldRewriter(final ClassNamed rewriteClass, final ResourceKey<? extends Registry<T>> registryKey, final String pattern, final boolean isInterface, final @Nullable String fetchMethod, final Void dummy) {
        super(rewriteClass, pattern, false);
        this.registry = Main.REGISTRY_ACCESS.registryOrThrow(registryKey);
        this.experimentalKeys = Suppliers.memoize(() -> RegistryUtils.collectExperimentalKeys(this.registry));
        this.isFilteredRegistry = FeatureElement.FILTERED_REGISTRIES.contains(registryKey);
        this.isInterface = isInterface;
        this.fetchMethod = fetchMethod;
    }

    public RegistryFieldRewriter(final Class<?> rewriteClass, final ResourceKey<? extends Registry<T>> registryKey, final String pattern, final @Nullable String fetchMethod) {
        this(new ClassNamed(rewriteClass), registryKey, pattern, rewriteClass.isInterface() && !rewriteClass.isAnnotation(), fetchMethod, null);
    }

    public RegistryFieldRewriter(final ClassNamed rewriteClass, final ResourceKey<? extends Registry<T>> registryKey, final String pattern, final boolean isInterface, final @NotNull String fetchMethod) {
        this(rewriteClass, registryKey, pattern, isInterface, fetchMethod, null);
    }

    @Override
    protected void beginSearch() {
        if (this.fetchMethod == null) {
            return;
        }

        Preconditions.checkState(this.rewriteClass.knownClass() != null, "This rewriter can't check the integrity of the fetch method since it doesn't know the rewritten class!");
        try {
            this.rewriteClass.knownClass().getDeclaredMethod(this.fetchMethod, String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void insert(final SearchMetadata metadata, final StringBuilder builder) {
        Iterator<Holder.Reference<T>> referenceIterator = this.registry.holders().sorted(Formatting.alphabeticKeyOrder(reference -> reference.key().location().getPath())).iterator();

        while (referenceIterator.hasNext()) {
            Holder.Reference<T> reference = referenceIterator.next();
            ResourceKey<T> resourceKey = reference.key();
            String pathKey = resourceKey.location().getPath();

            FeatureFlagSet requiredFeatures = this.getRequiredFeatures(reference);
            if (requiredFeatures != null) {
                Annotations.experimentalAnnotations(builder, metadata, requiredFeatures);
            }

            builder.append(metadata.indent());
            if (!this.isInterface) {
                builder.append("%s %s %s ".formatted(PUBLIC, STATIC, FINAL));
            }

            builder.append(this.rewriteFieldType(reference)).append(' ').append(this.rewriteFieldName(reference));
            builder.append(" = ");
            if (this.fetchMethod == null) {
                builder.append("%s.%s.get(%s.minecraft(%s))".formatted(org.bukkit.Registry.class.getSimpleName(), REGISTRY_FIELD_NAMES.get(this.rewriteClass.knownClass()), NamespacedKey.class.getSimpleName(), quoted(pathKey)));
            } else {
                builder.append("%s(%s)".formatted(this.fetchMethod, quoted(pathKey)));
            }
            builder.append(';');

            builder.append('\n');
            if (referenceIterator.hasNext()) {
                builder.append('\n');
            }
        }
    }

    protected String rewriteFieldType(Holder.Reference<T> reference) {
        return this.rewriteClass.simpleName();
    }

    protected String rewriteFieldName(Holder.Reference<T> reference) {
        return Formatting.formatKeyAsField(reference.key().location().getPath());
    }

    @Nullable
    protected FeatureFlagSet getRequiredFeatures(Holder.Reference<T> reference) {
        if (this.isFilteredRegistry && reference.value() instanceof FeatureElement element && FeatureFlags.isExperimental(element.requiredFeatures())) {
            return element.requiredFeatures();
        }
        if (this.experimentalKeys.get().contains(reference.key())) {
            return FlagSets.NEXT_UPDATE.get();
        }
        return null;
    }
}
