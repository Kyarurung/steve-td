package kim.biryeong.semiontd.trait;

import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class SemionTrait {
    private final ResourceLocation id;
    private final Component displayName;
    private final List<Component> description;

    protected SemionTrait(ResourceLocation id, Component displayName, List<Component> description) {
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.description = List.copyOf(description == null ? List.of() : description);
    }

    public ResourceLocation id() {
        return id;
    }

    public Component displayName() {
        return displayName;
    }

    public List<Component> description() {
        return description;
    }

    public long modifyStartingMineral(TraitContext context, TraitSlot slot, long value) {
        return value;
    }

    public long modifyStartingGas(TraitContext context, TraitSlot slot, long value) {
        return value;
    }

    public long modifyStartingIncome(TraitContext context, TraitSlot slot, long value) {
        return value;
    }

    public long modifyStartingGasPerSec(TraitContext context, TraitSlot slot, long value) {
        return value;
    }

    public void onRoundStarted(TraitContext context, TraitSlot slot, int round) {
    }

    public void onRoundEnded(TraitContext context, TraitSlot slot, int round) {
    }
}
