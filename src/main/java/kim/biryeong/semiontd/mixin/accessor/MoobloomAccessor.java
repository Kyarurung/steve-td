package kim.biryeong.semiontd.mixin.accessor;

import com.faboslav.friendsandfoes.common.entity.MoobloomEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MoobloomEntity.class)
public interface MoobloomAccessor {
    @Accessor("VARIANT")
    static EntityDataAccessor<String> semiontd$dataVariant() {
        throw new AssertionError();
    }
}
