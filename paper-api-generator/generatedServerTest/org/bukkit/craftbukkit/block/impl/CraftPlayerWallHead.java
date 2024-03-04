package org.bukkit.craftbukkit.block.impl;

import io.papermc.paper.generated.GeneratedFrom;
import net.minecraft.world.level.block.PlayerWallHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.SkullWall;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

@GeneratedFrom("1.20.4")
@SuppressWarnings("unused")
public class CraftPlayerWallHead extends CraftBlockData implements SkullWall {
    private static final DirectionProperty FACING = PlayerWallHeadBlock.FACING;

    private static final BooleanProperty POWERED = PlayerWallHeadBlock.POWERED;

    public CraftPlayerWallHead(BlockState state) {
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
    public boolean isPowered() {
        return this.get(POWERED);
    }

    @Override
    public void setPowered(final boolean powered) {
        this.set(POWERED, powered);
    }
}