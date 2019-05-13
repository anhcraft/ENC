package dev.anhcraft.enc.utils;

import java.io.File;

public class FilePaths {
    public static final File ROOT_FOLDER = new File("plugins/ENC/");
    public static final File LOCALE_FOLDER = new File(ROOT_FOLDER, "locale/");
    public static final File ENCHANTMENT_FOLDER = new File(ROOT_FOLDER, "enchantment/");
    public static final File GENERAL_CONFIG_FILE = new File(ROOT_FOLDER, "general.yml");
    public static final File OLD_GENERAL_CONFIG_FILE = new File(ROOT_FOLDER, "general.old.yml");
    public static final File GEM_CONFIG_FILE = new File(ROOT_FOLDER, "gems.yml");
}
