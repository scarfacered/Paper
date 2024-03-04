package org.bukkit.craftbukkit.block.impl;

import io.papermc.paper.generated.GeneratedFrom;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.DecoratedPot;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

@GeneratedFrom("1.20.4")
@SuppressWarnings("unused")
public class CraftDecoratedPot extends CraftBlockData implements DecoratedPot {
    private static final BooleanProperty CRACKED = DecoratedPotBlock.CRACKED;

    private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public CraftDecoratedPot(BlockState state) {
        super(state);
    }

    @Override
    public boolean isCracked() {
        return this.get(CRACKED);
    }

    @Override
    public void setCracked(final boolean cracked) {
        this.set(CRACKED, cracked);
    }

    @Override
    public BlockFace getFacing() {
        return this.get(HORIZONTAL_FACING, BlockFace.class);
    }

    @Override
    public void setFacing(final BlockFace blockFace) {
        this.set(HORIZONTAL_FACING, blockFace);
    }

    @Override
    public boolean isWaterlogged() {
        return this.get(WATERLOGGED);
    }

    @Override
    public void setWaterlogged(final boolean waterlogged) {
        this.set(WATERLOGGED, waterlogged);
    }
}