package kim.biryeong.semiontd.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Fox.class)
public interface FoxAccessor {
    @Accessor("DATA_TYPE_ID")
    static EntityDataAccessor<Integer> semiontd$dataTypeId() {
        throw new AssertionError();
    }
}
