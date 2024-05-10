package io.papermc.generator.rewriter.types.simple;

import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.types.RegistryFieldRewriter;
import io.papermc.generator.utils.BlockStateMapping;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import org.bukkit.block.data.BlockData;

public class BlockTypeRewriter extends RegistryFieldRewriter<Block> {

    private static final ClassNamed BLOCK_TYPE = ClassNamed.of("org.bukkit.block", "BlockType");
    private static final ClassNamed BLOCK_TYPE_TYPED = ClassNamed.of("org.bukkit.block", "BlockType$Typed");

    public BlockTypeRewriter(final String pattern) {
        super(BLOCK_TYPE, Registries.BLOCK, pattern, true, "getBlockType");
    }

    @Override
    protected String rewriteFieldType(Holder.Reference<Block> reference) {
        Class<?> blockData = BlockStateMapping.getBestSuitedApiClass(reference.value().getClass());
        if (blockData == null) {
            blockData = BlockData.class;
        }

        return "%s<%s>".formatted(BLOCK_TYPE_TYPED.dottedNestedName(), blockData.getSimpleName());
    }
}
