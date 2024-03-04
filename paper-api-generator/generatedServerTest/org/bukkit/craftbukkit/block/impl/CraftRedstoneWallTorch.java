package org.bukkit.craftbukkit.block.impl;

import io.papermc.paper.generated.GeneratedFrom;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.RedstoneWallTorch;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

@GeneratedFrom("1.20.4")
@SuppressWarnings("unused")
public class CraftRedstoneWallTorch extends CraftBlockData implements RedstoneWallTorch {
    private static final DirectionProperty FACING = RedstoneWallTorchBlock.FACING;

    private static final BooleanProperty LIT = RedstoneWallTorchBlock.LIT;

    public CraftRedstoneWallTorch(BlockState state) {
        super(state);
    }

    @Override
    public BlockFace getFacing() {
        return this.get(FACING, BlockFace.class);
    }

    @Override
    public void setFacing(final BlockFace blockFace) {
        this.set(FACING, blockFace);
    }

    @Override
    public boolean isLit() {
        return this.get(LIT);
    }

    @Override
    public void setLit(final boolean lit) {
        this.set(LIT, lit);
    }
}