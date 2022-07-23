package com.feyl.extension;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * value中存储真正实例
 *
 * @author Feyl
 */
public class Holder<T> {

    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
