package net.casual.arcade.util.mixins.bugfixes;

import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;

@Mixin(value = RecordCodecBuilder.class, remap = false)
public interface RecordCodecBuilderAccessor<O, F> {
    @Accessor("getter") Function<O, F> getter();

    @Accessor("encoder") Function<O, MapEncoder<F>> encoder();

    @Accessor("decoder") MapDecoder<F> decoder();

    @Invoker("<init>")
    static <O, F> RecordCodecBuilder<O, F> create(final Function<O, F> getter, final Function<O, MapEncoder<F>> encoder, final MapDecoder<F> decoder) {
        throw new AssertionError();
    }
}
