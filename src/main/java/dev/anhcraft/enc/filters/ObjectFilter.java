package dev.anhcraft.enc.filters;

public abstract class ObjectFilter<T> {
    public abstract boolean check(T object);
}
