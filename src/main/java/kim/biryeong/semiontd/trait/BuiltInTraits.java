package kim.biryeong.semiontd.trait;

import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class BuiltInTraits {
    public static final ResourceLocation NONE_ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "none");
    public static final ResourceLocation STARTER_MINERAL_TRAINING_ID = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "starter_mineral_training");

    private static boolean registered;

    private BuiltInTraits() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        TraitRegistry.registerIfAbsent(new SemionTrait(
                NONE_ID,
                Component.literal("선택 안 함"),
                List.of(Component.literal("효과가 없는 기본 특성입니다."))
        ) {
        });
        TraitRegistry.registerIfAbsent(new SemionTrait(
                STARTER_MINERAL_TRAINING_ID,
                Component.literal("채굴 훈련"),
                List.of(Component.literal("시작 다이아를 100% 기준 +100 증가시킵니다."))
        ) {
            @Override
            public long modifyStartingMineral(TraitContext context, TraitSlot slot, long value) {
                return TraitScaling.scaleDelta(value, value + 100L, slot);
            }
        });
        registered = true;
    }
}
