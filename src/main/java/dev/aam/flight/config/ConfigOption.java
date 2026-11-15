package dev.aam.flight.config;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

/**
 * 一个可配置参数的定义喵（注册表条目喵）：
 *  - 名字、读写器、取值范围、是否整数、是否互斥（同时只能有一个 att id 喵）
 */
public record ConfigOption(String name,
                           DoubleSupplier getter,
                           DoubleConsumer setter,
                           double min,
                           double max,
                           boolean integer,
                           boolean exclusive) {

    /** 读取当前全局值喵 */
    public double get() {
        return getter().getAsDouble();
    }

    /** 写入全局值（自动夹在范围内喵） */
    public void set(double value) {
        setter().accept(clamp(value));
    }

    public double clamp(double value) {
        return Math.max(min, Math.min(max, value));
    }

    /** 整数选项就显示成整数喵 */
    public String format(double value) {
        return (value == Math.floor(value)) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
