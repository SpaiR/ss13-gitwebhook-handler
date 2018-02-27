package io.github.spair.service;

public interface DataGenerator<T, V> {

    V generate(T generateFromObject);
}
