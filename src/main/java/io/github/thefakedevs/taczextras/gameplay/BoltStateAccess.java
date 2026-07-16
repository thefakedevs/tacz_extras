package io.github.thefakedevs.taczextras.gameplay;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface BoltStateAccess {
    @Nullable
    ItemStack taczExtras$getBoltingGunItem();

    void taczExtras$setBoltingGunItem(@Nullable ItemStack itemStack);
}
