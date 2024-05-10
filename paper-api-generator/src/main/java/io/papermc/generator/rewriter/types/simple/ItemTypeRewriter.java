package io.papermc.generator.rewriter.types.simple;

import io.papermc.generator.rewriter.ClassNamed;
import io.papermc.generator.rewriter.types.RegistryFieldRewriter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
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
        if (reference.value() == Items.AIR) {
            return ITEM_TYPE.simpleName();
        }

        return "%s<%s>".formatted(ITEM_TYPE_TYPED.dottedNestedName(), ItemMeta.class.getSimpleName());
    }
}
