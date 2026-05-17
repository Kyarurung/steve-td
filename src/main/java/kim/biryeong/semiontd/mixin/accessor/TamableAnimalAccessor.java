package kim.biryeong.semiontd.mixin.accessor;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TamableAnimal.class)
public interface TamableAnimalAccessor {
    @Accessor("DATA_FLAGS_ID")
    static EntityDataAccessor<Byte> semiontd$dataFlagsId() {
        throw new AssertionError();
    }
}
