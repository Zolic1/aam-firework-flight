package dev.aam.flight.config;

import dev.aam.flight.AamConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 存储层喵：把「注册表」里的全局值读写到 config/aam-flight.properties 喵。
 * 只负责磁盘 IO，不认识指令、也不认识物品 NBT 喵。
 */
public final class ConfigStore {

    private static final String FILE_NAME = "aam-flight.properties";
    private static final String PREFIX = "opt.";

    private ConfigStore() {}

    public static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    /** 读取全局值（文件不存在就先写一份默认喵） */
    public static void load() {
        Path path = file();
        if (!Files.exists(path)) {
            save();
            return;
        }

        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            props.load(reader);
        } catch (IOException e) {
            return;
        }

        for (ConfigOption option : AamConfig.options()) {
            String raw = props.getProperty(PREFIX + option.name());
            if (raw == null) continue;
            try {
                option.set(Double.parseDouble(raw.trim()));
            } catch (NumberFormatException ignored) {
                // 写坏了就跳过，保持默认值喵
            }
        }
    }

    /** 保存全局值喵 */
    public static void save() {
        Properties props = new Properties();
        for (ConfigOption option : AamConfig.options()) {
            props.setProperty(PREFIX + option.name(), String.valueOf(option.get()));
        }
        try (Writer writer = Files.newBufferedWriter(file())) {
            props.store(writer, "AAM flight config (global values)");
        } catch (IOException ignored) {
        }
    }
}
