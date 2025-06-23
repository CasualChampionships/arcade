/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.util.codec;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.casual.arcade.util.mixins.bugfixes.RecordCodecBuilderAccessor;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public final class OrderedRecordCodecBuilderInstance<O> implements Applicative<RecordCodecBuilder.Mu<O>, OrderedRecordCodecBuilderInstance.Mu<O>> {
    @SuppressWarnings("unused")
    public static final class Mu<O> implements Applicative.Mu {}

    @SuppressWarnings("unchecked")
    public static <O, F> RecordCodecBuilderAccessor<O, F> access(final App<RecordCodecBuilder.Mu<O>, F> box) {
        return ((RecordCodecBuilderAccessor<O, F>) box);
    }

    @Override
    public <A> App<RecordCodecBuilder.Mu<O>, A> point(final A a) {
        return RecordCodecBuilder.point(a);
    }

    @Override
    public <A, R> Function<App<RecordCodecBuilder.Mu<O>, A>, App<RecordCodecBuilder.Mu<O>, R>> lift1(final App<RecordCodecBuilder.Mu<O>, Function<A, R>> function) {
        return fa -> {
            final RecordCodecBuilderAccessor<O, Function<A, R>> f = access(function);
            final RecordCodecBuilderAccessor<O, A> a = access(fa);

            return RecordCodecBuilderAccessor.create(
                o -> f.getter().apply(o).apply(a.getter().apply(o)),
                o -> {
                    final MapEncoder<Function<A, R>> fEnc = f.encoder().apply(o);
                    final MapEncoder<A> aEnc = a.encoder().apply(o);
                    final A aFromO = a.getter().apply(o);

                    return new MapEncoder.Implementation<R>() {
                        @Override
                        public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                            fEnc.encode(a1 -> input, ops, prefix);
                            aEnc.encode(aFromO, ops, prefix);
                            return prefix;
                        }

                        @Override
                        public <T> Stream<T> keys(final DynamicOps<T> ops) {
                            return Stream.concat(aEnc.keys(ops), fEnc.keys(ops));
                        }

                        @Override
                        public String toString() {
                            return fEnc + " * " + aEnc;
                        }
                    };
                },

                new MapDecoder.Implementation<R>() {
                    @Override
                    public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                        return a.decoder().decode(ops, input).flatMap(ar ->
                            f.decoder().decode(ops, input).map(fr ->
                                fr.apply(ar)
                            )
                        );
                    }

                    @Override
                    public <T> Stream<T> keys(final DynamicOps<T> ops) {
                        return Stream.concat(a.decoder().keys(ops), f.decoder().keys(ops));
                    }

                    @Override
                    public String toString() {
                        return f.decoder() + " * " + a.decoder();
                    }
                }
            );
        };
    }

    @Override
    public <A, B, R> App<RecordCodecBuilder.Mu<O>, R> ap2(final App<RecordCodecBuilder.Mu<O>, BiFunction<A, B, R>> func, final App<RecordCodecBuilder.Mu<O>, A> a, final App<RecordCodecBuilder.Mu<O>, B> b) {
        final RecordCodecBuilderAccessor<O, BiFunction<A, B, R>> function = access(func);
        final RecordCodecBuilderAccessor<O, A> fa = access(a);
        final RecordCodecBuilderAccessor<O, B> fb = access(b);

        return RecordCodecBuilderAccessor.create(
            o -> function.getter().apply(o).apply(fa.getter().apply(o), fb.getter().apply(o)),
            o -> {
                final MapEncoder<BiFunction<A, B, R>> fEncoder = function.encoder().apply(o);
                final MapEncoder<A> aEncoder = fa.encoder().apply(o);
                final A aFromO = fa.getter().apply(o);
                final MapEncoder<B> bEncoder = fb.encoder().apply(o);
                final B bFromO = fb.getter().apply(o);

                return new MapEncoder.Implementation<>() {
                    @Override
                    public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                        fEncoder.encode((a1, b1) -> input, ops, prefix);
                        aEncoder.encode(aFromO, ops, prefix);
                        bEncoder.encode(bFromO, ops, prefix);
                        return prefix;
                    }

                    @Override
                    public <T> Stream<T> keys(final DynamicOps<T> ops) {
                        return Stream.of(
                            fEncoder.keys(ops),
                            aEncoder.keys(ops),
                            bEncoder.keys(ops)
                        ).flatMap(Function.identity());
                    }

                    @Override
                    public String toString() {
                        return fEncoder + " * " + aEncoder + " * " + bEncoder;
                    }
                };
            },
            new MapDecoder.Implementation<>() {
                @Override
                public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                    return DataResult.unbox(DataResult.instance().ap2(
                        function.decoder().decode(ops, input),
                        fa.decoder().decode(ops, input),
                        fb.decoder().decode(ops, input)
                    ));
                }

                @Override
                public <T> Stream<T> keys(final DynamicOps<T> ops) {
                    return Stream.of(
                        function.decoder().keys(ops),
                        fa.decoder().keys(ops),
                        fb.decoder().keys(ops)
                    ).flatMap(Function.identity());
                }

                @Override
                public String toString() {
                    return function.decoder() + " * " + fa.decoder() + " * " + fb.decoder();
                }
            }
        );
    }

    @Override
    public <T1, T2, T3, R> App<RecordCodecBuilder.Mu<O>, R> ap3(final App<RecordCodecBuilder.Mu<O>, Function3<T1, T2, T3, R>> func, final App<RecordCodecBuilder.Mu<O>, T1> t1, final App<RecordCodecBuilder.Mu<O>, T2> t2, final App<RecordCodecBuilder.Mu<O>, T3> t3) {
        final RecordCodecBuilderAccessor<O, Function3<T1, T2, T3, R>> function = access(func);
        final RecordCodecBuilderAccessor<O, T1> f1 = access(t1);
        final RecordCodecBuilderAccessor<O, T2> f2 = access(t2);
        final RecordCodecBuilderAccessor<O, T3> f3 = access(t3);

        return RecordCodecBuilderAccessor.create(
            o -> function.getter().apply(o).apply(
                f1.getter().apply(o),
                f2.getter().apply(o),
                f3.getter().apply(o)
            ),
            o -> {
                final MapEncoder<Function3<T1, T2, T3, R>> fEncoder = function.encoder().apply(o);
                final MapEncoder<T1> e1 = f1.encoder().apply(o);
                final T1 v1 = f1.getter().apply(o);
                final MapEncoder<T2> e2 = f2.encoder().apply(o);
                final T2 v2 = f2.getter().apply(o);
                final MapEncoder<T3> e3 = f3.encoder().apply(o);
                final T3 v3 = f3.getter().apply(o);

                return new MapEncoder.Implementation<>() {
                    @Override
                    public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                        fEncoder.encode((t1, t2, t3) -> input, ops, prefix);
                        e1.encode(v1, ops, prefix);
                        e2.encode(v2, ops, prefix);
                        e3.encode(v3, ops, prefix);
                        return prefix;
                    }

                    @Override
                    public <T> Stream<T> keys(final DynamicOps<T> ops) {
                        return Stream.of(
                            fEncoder.keys(ops),
                            e1.keys(ops),
                            e2.keys(ops),
                            e3.keys(ops)
                        ).flatMap(Function.identity());
                    }

                    @Override
                    public String toString() {
                        return fEncoder + " * " + e1 + " * " + e2 + " * " + e3;
                    }
                };
            },
            new MapDecoder.Implementation<>() {
                @Override
                public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                    return DataResult.unbox(DataResult.instance().ap3(
                        function.decoder().decode(ops, input),
                        f1.decoder().decode(ops, input),
                        f2.decoder().decode(ops, input),
                        f3.decoder().decode(ops, input)
                    ));
                }

                @Override
                public <T> Stream<T> keys(final DynamicOps<T> ops) {
                    return Stream.of(
                        function.decoder().keys(ops),
                        f1.decoder().keys(ops),
                        f2.decoder().keys(ops),
                        f3.decoder().keys(ops)
                    ).flatMap(Function.identity());
                }

                @Override
                public String toString() {
                    return function.decoder() + " * " + f1.decoder() + " * " + f2.decoder() + " * " + f3.decoder();
                }
            }
        );
    }

    @Override
    public <T1, T2, T3, T4, R> App<RecordCodecBuilder.Mu<O>, R> ap4(final App<RecordCodecBuilder.Mu<O>, Function4<T1, T2, T3, T4, R>> func, final App<RecordCodecBuilder.Mu<O>, T1> t1, final App<RecordCodecBuilder.Mu<O>, T2> t2, final App<RecordCodecBuilder.Mu<O>, T3> t3, final App<RecordCodecBuilder.Mu<O>, T4> t4) {
        final RecordCodecBuilderAccessor<O, Function4<T1, T2, T3, T4, R>> function = access(func);
        final RecordCodecBuilderAccessor<O, T1> f1 = access(t1);
        final RecordCodecBuilderAccessor<O, T2> f2 = access(t2);
        final RecordCodecBuilderAccessor<O, T3> f3 = access(t3);
        final RecordCodecBuilderAccessor<O, T4> f4 = access(t4);

        return RecordCodecBuilderAccessor.create(
            o -> function.getter().apply(o).apply(
                f1.getter().apply(o),
                f2.getter().apply(o),
                f3.getter().apply(o),
                f4.getter().apply(o)
            ),
            o -> {
                final MapEncoder<Function4<T1, T2, T3, T4, R>> fEncoder = function.encoder().apply(o);
                final MapEncoder<T1> e1 = f1.encoder().apply(o);
                final T1 v1 = f1.getter().apply(o);
                final MapEncoder<T2> e2 = f2.encoder().apply(o);
                final T2 v2 = f2.getter().apply(o);
                final MapEncoder<T3> e3 = f3.encoder().apply(o);
                final T3 v3 = f3.getter().apply(o);
                final MapEncoder<T4> e4 = f4.encoder().apply(o);
                final T4 v4 = f4.getter().apply(o);

                return new MapEncoder.Implementation<>() {
                    @Override
                    public <T> RecordBuilder<T> encode(final R input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
                        fEncoder.encode((t1, t2, t3, t4) -> input, ops, prefix);
                        e1.encode(v1, ops, prefix);
                        e2.encode(v2, ops, prefix);
                        e3.encode(v3, ops, prefix);
                        e4.encode(v4, ops, prefix);
                        return prefix;
                    }

                    @Override
                    public <T> Stream<T> keys(final DynamicOps<T> ops) {
                        return Stream.of(
                            fEncoder.keys(ops),
                            e1.keys(ops),
                            e2.keys(ops),
                            e3.keys(ops),
                            e4.keys(ops)
                        ).flatMap(Function.identity());
                    }

                    @Override
                    public String toString() {
                        return fEncoder + " * " + e1 + " * " + e2 + " * " + e3 + " * " + e4;
                    }
                };
            },
            new MapDecoder.Implementation<>() {
                @Override
                public <T> DataResult<R> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                    return DataResult.unbox(DataResult.instance().ap4(
                        function.decoder().decode(ops, input),
                        f1.decoder().decode(ops, input),
                        f2.decoder().decode(ops, input),
                        f3.decoder().decode(ops, input),
                        f4.decoder().decode(ops, input)
                    ));
                }

                @Override
                public <T> Stream<T> keys(final DynamicOps<T> ops) {
                    return Stream.of(
                        function.decoder().keys(ops),
                        f1.decoder().keys(ops),
                        f2.decoder().keys(ops),
                        f3.decoder().keys(ops),
                        f4.decoder().keys(ops)
                    ).flatMap(Function.identity());
                }

                @Override
                public String toString() {
                    return function.decoder() + " * " + f1.decoder() + " * " + f2.decoder() + " * " + f3.decoder() + " * " + f4.decoder();
                }
            }
        );
    }

    @Override
    public <T, R> App<RecordCodecBuilder.Mu<O>, R> map(final Function<? super T, ? extends R> func, final App<RecordCodecBuilder.Mu<O>, T> ts) {
        final RecordCodecBuilderAccessor<O, T> unbox = access(ts);
        final Function<O, T> getter = unbox.getter();
        return RecordCodecBuilderAccessor.create(
            getter.andThen(func),
            o -> new MapEncoder.Implementation<>() {
                private final MapEncoder<T> encoder = unbox.encoder().apply(o);

                @Override
                public <U> RecordBuilder<U> encode(final R input, final DynamicOps<U> ops, final RecordBuilder<U> prefix) {
                    return this.encoder.encode(getter.apply(o), ops, prefix);
                }

                @Override
                public <U> Stream<U> keys(final DynamicOps<U> ops) {
                    return this.encoder.keys(ops);
                }

                @Override
                public String toString() {
                    return this.encoder + "[mapped]";
                }
            },
            unbox.decoder().map(func)
        );
    }
}