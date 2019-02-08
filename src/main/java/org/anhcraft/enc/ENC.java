package org.anhcraft.enc;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.api.rune.Rune;
import org.anhcraft.enc.api.rune.RuneAPI;
import org.anhcraft.enc.commands.AdminCommand;
import org.anhcraft.enc.enchantments.*;
import org.anhcraft.enc.listeners.AttackListener;
import org.anhcraft.enc.listeners.KillListener;
import org.anhcraft.enc.listeners.RuneApplyListener;
import org.anhcraft.spaciouslib.io.DirectoryManager;
import org.anhcraft.spaciouslib.io.FileManager;
import org.anhcraft.spaciouslib.utils.Chat;
import org.anhcraft.spaciouslib.utils.IOUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ENC extends JavaPlugin {
    private static final File ROOT_FOLDER = new File("plugins/ENC/");
    private static final File LOCALE_FOLDER = new File(ROOT_FOLDER, "locale/");
    private static final File ENCHANTMENT_FOLDER = new File(ROOT_FOLDER, "enchantment/");
    private static final File GENERAL_CONFIG_FILE = new File(ROOT_FOLDER, "general.yml");
    private static final File RUNE_CONFIG_FILE = new File(ROOT_FOLDER, "runes.yml");
    private static final YamlConfiguration localeConfig = new YamlConfiguration();
    private static final YamlConfiguration generalConfig = new YamlConfiguration();
    private static final YamlConfiguration runeConfig = new YamlConfiguration();
    private static EnchantmentAPI api;
    private static Chat chat;
    private static ENC instance;
    private static TaskChainFactory taskChainFactory;

    public static EnchantmentAPI getApi() {
        return api;
    }

    public static YamlConfiguration getLocaleConfig() {
        return localeConfig;
    }

    public static YamlConfiguration getGeneralConfig() {
        return generalConfig;
    }

    public static Chat getPluginChat() {
        return chat;
    }

    public static ENC getInstance(){
        return instance;
    }

    public static TaskChainFactory getTaskChainFactory(){
        return taskChainFactory;
    }

    private void reloadRune(){
        RuneAPI.getRegisteredRunes().forEach(RuneAPI::unregisterRune);
        runeConfig.getKeys(false).forEach(s -> {
            ConfigurationSection rune = runeConfig.getConfigurationSection(s);
            RuneAPI.registerRune(new Rune(
                s,
                rune.getString("name"),
                api.getEnchantmentById(rune.getString("enchantment.id")),
                rune.getInt("enchantment.level"),
                rune.getDouble("success_rate.min"),
                rune.getDouble("success_rate.max"),
                rune.getDouble("protection_rate.min"),
                rune.getDouble("protection_rate.max")
            ));
        });
    }

    public void reloadPlugin() throws Exception {
        // init files and directories
        new DirectoryManager(ROOT_FOLDER).mkdir();
        new DirectoryManager(LOCALE_FOLDER).mkdir();
        new DirectoryManager(ENCHANTMENT_FOLDER).mkdir();
        new FileManager(GENERAL_CONFIG_FILE).initFile(IOUtils.toByteArray(getResource("general.yml")));
        new FileManager(RUNE_CONFIG_FILE).initFile(IOUtils.toByteArray(getResource("runes.yml")));
        // load configs
        generalConfig.load(GENERAL_CONFIG_FILE);
        runeConfig.load(RUNE_CONFIG_FILE);
        File localeFile = new File(LOCALE_FOLDER, generalConfig.getString("plugin.locale_file"));
        new FileManager(localeFile).initFile(IOUtils.toByteArray(getClass().getResourceAsStream("/locale/"+generalConfig.getString("plugin.locale_file"))));
        localeConfig.load(localeFile);
        // init chat
        chat = new Chat(generalConfig.getString("plugin.prefix"));
        // reload enchantment/rune configs
        if(api != null){
            api.getRegisteredEnchantments().forEach(Enchantment::reloadConfig);
            reloadRune();
        }
    }

    private void registerEnchants() {
        api.registerEnchantment(new ColouredSheep());
        api.registerEnchantment(new Wither());
        api.registerEnchantment(new Chef());
        api.registerEnchantment(new Freeze());
        api.registerEnchantment(new Blindness());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new AttackListener(), this);
        getServer().getPluginManager().registerEvents(new KillListener(), this);
        getServer().getPluginManager().registerEvents(new RuneApplyListener(), this);
    }

    private void registerCommand() {
        new AdminCommand().run();
    }

    @Override
    public void onEnable() {
        instance = this;
        taskChainFactory = BukkitTaskChainFactory.create(this);
        // init plugin
        try {
            reloadPlugin();
        } catch(Exception e) {
            e.printStackTrace();
        }
        // init API
        api = new EnchantmentAPI(ENCHANTMENT_FOLDER);
        // register stuffs
        registerListeners();
        registerEnchants();
        registerCommand();
        // load runes
        reloadRune();
        chat.sendConsole("&aPlugin has been enabled!");
        chat.sendConsole("&eDonate me if you like this plugin <3");
        chat.sendConsole("&ehttps://paypal.me/anhcraft");
    }
}
