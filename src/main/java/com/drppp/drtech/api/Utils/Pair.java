package com.drppp.drtech.api.Utils;

import scala.util.hashing.MurmurHash3;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Pair<A, B> implements Map.Entry<A, B> {

    Object[] pair = new Object[2];

    public Pair(Object[] pair) {
        this.pair = pair;
    }

    public Pair(A k, B v) {
        this.pair[0] = k;
        this.pair[1] = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;

        Pair<?, ?> pair1 = (Pair<?, ?>) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(this.pair, pair1.pair);
    }

//    @Override
//    public int hashCode() {
//        return MurmurHash3.murmurhash3_x86_32(
//                ByteBuffer.allocate(8).putInt(this.pair[0].hashCode()).putInt(this.pair[1].hashCode()).array(),
//                0,
//                8,
//                31);
//    }

    @Override
    public A getKey() {
        return (A) this.pair[0];
    }

    @Override
    public B getValue() {
        return (B) this.pair[1];
    }

    @Override
    public B setValue(Object value) {
        this.pair[1] = value;
        return (B) this.pair[1];
    }

    public Pair<A, B> copyWithNewValue(B value) {
        return new Pair<>((A) this.pair[0], value);
    }

    public Pair<A, B> replaceValue(B value) {
        this.setValue(value);
        return this;
    }
}