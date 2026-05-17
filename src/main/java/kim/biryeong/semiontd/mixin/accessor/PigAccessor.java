package kim.biryeong.semiontd.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.PigVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Pig.class)
public interface PigAccessor {
    @Accessor("DATA_VARIANT_ID")
    static EntityDataAccessor<Holder<PigVariant>> semiontd$dataVariantId() {
        throw new AssertionError();
    }
}
