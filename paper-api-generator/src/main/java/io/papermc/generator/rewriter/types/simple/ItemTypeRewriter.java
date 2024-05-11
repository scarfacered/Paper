package io.papermc.generator.rewriter.types.simple;

import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.types.RegistryFieldRewriter;
import io.papermc.generator.utils.experimental.ExperimentalHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.bukkit.inventory.meta.ItemMeta;

@Deprecated // bad generic
public class ItemTypeRewriter extends RegistryFieldRewriter<Item> {

    private static final ClassNamed ITEM_TYPE = ClassNamed.of("org.bukkit.inventory", "ItemType");
    private static final ClassNamed ITEM_TYPE_TYPED = ClassNamed.of("org.bukkit.inventory", "ItemType$Typed");

    public ItemTypeRewriter(final String pattern) {
        super(ITEM_TYPE, Registries.ITEM, pattern, true, "getItemType");
    }

    @Override
    protected String rewriteFieldType(Holder.Reference<Item> reference) {
        if (reference.value().equals(Items.AIR)) {
            return super.rewriteFieldType(reference);
        }

        return "%s<%s>".formatted(ITEM_TYPE_TYPED.dottedNestedName(), ItemMeta.class.getSimpleName());
    }

    @Override
    protected FeatureFlagSet getRequiredFeatures(Holder.Reference<Item> reference) {
        if (reference.value() instanceof BundleItem) {
            return ExperimentalHelper.FlagSets.BUNDLE.get(); // special case since the item is not locked itself just in the creative menu
        } else {
            return super.getRequiredFeatures(reference);
        }
    }
}
