package kim.biryeong.semiontd.ui.dialog.body;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.polymer.core.api.other.PolymerMapCodec;
import kim.biryeong.semiontd.util.TextUncenterer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.body.DialogBody;
import net.minecraft.server.dialog.body.PlainMessage;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Optional;

public record HeaderMessage(Component contents, int width) implements DialogBody {
    private static final ResourceLocation DEFAULT_FONT_ID = ResourceLocation.withDefaultNamespace("default");
    private static final int FONT_SIZE = 8;

    public static final MapCodec<HeaderMessage> MAP_CODEC = PolymerMapCodec.ofDialogBody(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ComponentSerialization.CODEC.fieldOf("contents").forGetter(HeaderMessage::contents),
                    Dialog.WIDTH_CODEC.optionalFieldOf("width", 310).forGetter(HeaderMessage::width)
            ).apply(instance, HeaderMessage::new)),
            HeaderMessage::asVanillaBody
    );

    @Override
    public MapCodec<? extends DialogBody> mapCodec() {
        return MAP_CODEC;
    }

    public Component asVanillaComponent() {
        Component title = Component.literal(" ")
                .append(this.contents)
                .append(" ");

        int sideWidth = Math.max(0, (this.width - getWidth(title) - 23) / 2);

        MutableComponent side = TextUncenterer.filler(sideWidth)
                .copy()
                .withStyle(style -> style.withStrikethrough(true).withShadowColor(0));

        return Component.empty()
                .append(side)
                .append(title)
                .append(side.copy());
    }

    public PlainMessage asVanillaBody(PacketContext context) {
        return new PlainMessage(asVanillaComponent(), this.width);
    }

    public static Component dividerComponent(int width) {
        return TextUncenterer.filler(Math.max(0, width))
                .copy()
                .withStyle(style -> style.withColor(0xFFFFFF).withStrikethrough(true).withShadowColor(0));
    }

    public static PlainMessage divider(int width) {
        return new PlainMessage(dividerComponent(width), width);
    }

    private static int getWidth(Component text) {
        final int[] width = {0};

        text.visit((style, string) -> {
            if (!string.isEmpty()) {
                width[0] += getTextWidth(style, string);
            }
            return Optional.empty();
        }, Style.EMPTY);

        return width[0];
    }

    private static int getTextWidth(Style style, String string) {
        ResourceLocation fontId = style.getFont() == null ? DEFAULT_FONT_ID : style.getFont();
        return DefaultFonts.REGISTRY.getDefaultedFont(fontId).getTextWidth(string, FONT_SIZE);
    }
}