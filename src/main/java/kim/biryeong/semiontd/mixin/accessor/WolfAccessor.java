package kim.biryeong.semiontd.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Wolf.class)
public interface WolfAccessor {
    @Accessor("DATA_VARIANT_ID")
    static EntityDataAccessor<Holder<WolfVariant>> semiontd$dataVariantId() {
        throw new AssertionError();
    }

    @Accessor("DATA_SOUND_VARIANT_ID")
    static EntityDataAccessor<Holder<WolfSoundVariant>> semiontd$dataSoundVariantId() {
        throw new AssertionError();
    }

    @Accessor("DATA_COLLAR_COLOR")
    static EntityDataAccessor<Integer> semiontd$dataCollarColor() {
        throw new AssertionError();
    }
}
