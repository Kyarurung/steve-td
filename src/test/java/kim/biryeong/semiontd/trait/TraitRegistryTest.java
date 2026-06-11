package kim.biryeong.semiontd.trait;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import kim.biryeong.semiontd.SemionTd;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class TraitRegistryTest {
    @Test
    void builtInRegistrationIsIdempotentAndContainsNone() {
        BuiltInTraits.register();
        BuiltInTraits.register();

        assertTrue(TraitRegistry.find(BuiltInTraits.NONE_ID).isPresent());
        assertTrue(TraitRegistry.find(BuiltInTraits.STARTER_MINERAL_TRAINING_ID).isPresent());
    }

    @Test
    void duplicateTraitIdsAreRejected() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SemionTd.MOD_ID, "duplicate_test_trait");
        TraitRegistry.register(new SemionTrait(id, Component.literal("중복 테스트"), List.of()) {});

        assertThrows(IllegalArgumentException.class, () ->
                TraitRegistry.register(new SemionTrait(id, Component.literal("중복 테스트 2"), List.of()) {}));
    }
}
