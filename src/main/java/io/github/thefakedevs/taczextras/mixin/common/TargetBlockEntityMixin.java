package io.github.thefakedevs.taczextras.mixin.common;

import com.tacz.guns.block.TargetBlock;
import com.tacz.guns.block.entity.TargetBlockEntity;
import com.tacz.guns.config.common.OtherConfig;
import com.tacz.guns.init.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import io.github.thefakedevs.taczextras.config.TaczExtrasConfig;

import static com.tacz.guns.block.TargetBlock.OUTPUT_POWER;
import static com.tacz.guns.block.TargetBlock.STAND;

@Mixin(value = TargetBlockEntity.class, remap = false)
public abstract class TargetBlockEntityMixin {
    /**
     * @author TACZ Extras
     * @reason Expose the historical target-volume patch through a simple config value.
     */
    @Overwrite
    public void hit(Level level, BlockState state, BlockHitResult hit, boolean isUpperBlock) {
        if (!state.getValue(STAND)) {
            return;
        }

        BlockPos blockPos = hit.getBlockPos();
        if (isUpperBlock) {
            blockPos = blockPos.below();
            state = level.getBlockState(blockPos);
        }

        int redstoneStrength = TargetBlock.getRedstoneStrength(hit, isUpperBlock);
        level.setBlock(
                blockPos,
                state.setValue(STAND, false).setValue(OUTPUT_POWER, redstoneStrength),
                Block.UPDATE_ALL
        );
        level.scheduleTick(blockPos, state.getBlock(), 5 * 20);

        double divisor = TaczExtrasConfig.COMMON.enableTargetSoundPatch.get()
                ? TaczExtrasConfig.COMMON.targetSoundDistanceDivisor.get()
                : 16.0;
        float volume = Math.max((float) (OtherConfig.TARGET_SOUND_DISTANCE.get() / divisor), 0.0f);
        level.playSound(
                null,
                blockPos,
                ModSounds.TARGET_HIT.get(),
                SoundSource.BLOCKS,
                volume,
                level.random.nextFloat() * 0.1f + 0.9f
        );
    }
}
