package org.nustaq.reallive.query;

import java.util.Arrays;

public class ArrayValue implements Value {

    Object[] value;
    QToken token;

    public ArrayValue(Object[] value, QToken token) {
        this.value = value;
        this.token = token;
    }

    @Override
    public QToken getToken() {
        return token;
    }

    @Override
    public double getDoubleValue() {
        return value.length;
    }

    @Override
    public long getLongValue() {
        return value.length;
    }

    @Override
    public String getStringValue() {
        return "[array]";
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public Value negate() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return value == null || value.length == 0;
    }

    public long size() {
        return value.length;
    }
}
