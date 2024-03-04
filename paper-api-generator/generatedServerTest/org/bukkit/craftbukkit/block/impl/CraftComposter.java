package org.bukkit.craftbukkit.block.impl;

import io.papermc.paper.generated.GeneratedFrom;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.bukkit.block.data.Levelled;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

@GeneratedFrom("1.20.4")
@SuppressWarnings("unused")
public class CraftComposter extends CraftBlockData implements Levelled {
    private static final IntegerProperty LEVEL = ComposterBlock.LEVEL;

    public CraftComposter(BlockState state) {
        super(state);
    }

    @Override
    public int getLevel() {
        return this.get(LEVEL);
    }

    @Override
    public void setLevel(final int level) {
        this.set(LEVEL, level);
    }
}