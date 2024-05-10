package io.papermc.generator;

import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.logging.LogUtils;
import io.papermc.generator.types.SourceGenerator;
import io.papermc.generator.types.craftblockdata.CraftBlockDataGenerators;
import io.papermc.generator.utils.experimental.TagCollector;
import io.papermc.generator.utils.experimental.TagResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.flag.FeatureFlags;
import org.apache.commons.io.file.PathUtils;
import org.slf4j.Logger;

public final class Main {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final RegistryAccess.Frozen REGISTRY_ACCESS;
    public static final TagResult EXPERIMENTAL_TAGS;
    public static Path generatedPath;
    public static Path generatedServerPath;

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Bootstrap.validate();

        final PackRepository resourceRepository = ServerPacksSource.createVanillaTrustedRepository();
        resourceRepository.reload();
        final MultiPackResourceManager resourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, resourceRepository.getAvailablePacks().stream().map(Pack::open).toList());
        LayeredRegistryAccess<RegistryLayer> layers = RegistryLayer.createRegistryAccess();
        layers = WorldLoader.loadAndReplaceLayer(resourceManager, layers, RegistryLayer.WORLDGEN, RegistryDataLoader.WORLDGEN_REGISTRIES);
        REGISTRY_ACCESS = layers.compositeAccess().freeze();
        final ReloadableServerResources datapack = ReloadableServerResources.loadResources(resourceManager, layers, FeatureFlags.REGISTRY.allFlags(), Commands.CommandSelection.DEDICATED, 0, MoreExecutors.directExecutor(), MoreExecutors.directExecutor()).join();
        datapack.updateRegistryTags();
        EXPERIMENTAL_TAGS = TagCollector.grabExperimental(resourceManager);
    }

    private Main() {
    }

    public static void main(final String[] args) {
        LOGGER.info("Running API generators...");

        Main.generatedPath = Path.of(args[0]); // todo remove
        Main.generatedServerPath = Path.of(args[2]); // todo remove
        try {
            generate(Main.generatedPath, Generators.API);
            apply(Path.of(args[1]), Generators.API_REWRITE);

            generateCraftBlockData(Main.generatedServerPath);
            apply(Path.of(args[3]), Generators.SERVER_REWRITE);
        } catch (final RuntimeException ex) {
            throw ex;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void generate(Path output, SourceGenerator[] generators) throws IOException {
        if (Files.exists(output)) {
            PathUtils.deleteDirectory(output);
        }

        apply(output, generators);
        LOGGER.info("Files written to {}", output.toAbsolutePath());
    }

    private static void apply(Path output, SourceWriter[] writers) throws IOException {
        for (final SourceWriter writer : writers) {
            writer.writeToFile(output);
        }
    }

    private static void generateCraftBlockData(Path output) throws IOException {
        if (Files.exists(output)) {
            PathUtils.deleteDirectory(output);
        }

        CraftBlockDataGenerators.generate(output);
        LOGGER.info("Files written to {}", output.toAbsolutePath());
    }
}
