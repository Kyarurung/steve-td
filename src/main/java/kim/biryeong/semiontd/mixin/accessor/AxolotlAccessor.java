package kim.biryeong.semiontd.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Axolotl.class)
public interface AxolotlAccessor {
    @Accessor("DATA_VARIANT")
    static EntityDataAccessor<Integer> semiontd$dataVariant() {
        throw new AssertionError();
    }
}
