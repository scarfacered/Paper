package io.papermc.generator.rewriter.types.simple;

import com.google.common.collect.HashBiMap;
import io.papermc.generator.rewriter.types.EnumRegistryRewriter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.datafix.fixes.BannerPatternFormatFix;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.bukkit.block.banner.PatternType;

import java.util.Map;

import static io.papermc.generator.utils.Formatting.quoted;

public class PatternTypeRewriter extends EnumRegistryRewriter<BannerPattern, PatternType> {

    // DataConverter(V3818): banner pattern code -> asset id map is not accessible so get it from DFU
    @Deprecated
    private static final Map<String, String> ASSETS_ID_TO_LEGACY_CODE = HashBiMap.create(BannerPatternFormatFix.PATTERN_ID_MAP).inverse();

    public PatternTypeRewriter(final String pattern) {
        super(PatternType.class, Registries.BANNER_PATTERN, pattern, true);
    }

    @Override
    protected String rewriteEnumValue(Holder.Reference<BannerPattern> reference) {
        return "%s, %s".formatted(
            quoted(ASSETS_ID_TO_LEGACY_CODE.get(reference.value().assetId().toString())),
            super.rewriteEnumValue(reference)
        );
    }
}
