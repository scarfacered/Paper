package org.bukkit.craftbukkit.block.impl;

import io.papermc.paper.generated.GeneratedFrom;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.bukkit.block.data.type.Bamboo;
import org.bukkit.craftbukkit.block.data.CraftBlockData;

@GeneratedFrom("1.20.4")
@SuppressWarnings("unused")
public class CraftBambooStalk extends CraftBlockData implements Bamboo {
    private static final IntegerProperty AGE = BambooStalkBlock.AGE;

    private static final EnumProperty<BambooLeaves> LEAVES = BambooStalkBlock.LEAVES;

    private static final IntegerProperty STAGE = BambooStalkBlock.STAGE;

    public CraftBambooStalk(BlockState state) {
        super(state);
    }

    @Override
    public int getAge() {
        return this.get(AGE);
    }

    @Override
    public void setAge(final int age) {
        this.set(AGE, age);
    }

    @Override
    public Bamboo.Leaves getLeaves() {
        return this.get(LEAVES, Bamboo.Leaves.class);
    }

    @Override
    public void setLeaves(final Bamboo.Leaves leaves) {
        this.set(LEAVES, leaves);
    }

    @Override
    public int getStage() {
        return this.get(STAGE);
    }

    @Override
    public void setStage(final int stage) {
        this.set(STAGE, stage);
    }
}