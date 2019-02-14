package org.anhcraft.enc;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.api.gem.Gem;
import org.anhcraft.enc.api.gem.GemAPI;
import org.anhcraft.enc.commands.AdminCommand;
import org.anhcraft.enc.enchantments.*;
import org.anhcraft.enc.listeners.AttackListener;
import org.anhcraft.enc.listeners.DeathDropListener;
import org.anhcraft.enc.listeners.KillListener;
import org.anhcraft.enc.listeners.gem.GemDropListener;
import org.anhcraft.enc.listeners.gem.GemMergeListener;
import org.anhcraft.enc.utils.FilePaths;
import org.anhcraft.spaciouslib.io.DirectoryManager;
import org.anhcraft.spaciouslib.io.FileManager;
import org.anhcraft.spaciouslib.utils.Chat;
import org.anhcraft.spaciouslib.utils.IOUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ENC extends JavaPlugin {
    private static final YamlConfiguration localeConfig = new YamlConfiguration();
    private static final YamlConfiguration generalConfig = new YamlConfiguration();
    private static final YamlConfiguration gemConfig = new YamlConfiguration();
    private static Chat chat;
    private static ENC instance;
    private static TaskChainFactory taskChainFactory;
    private static boolean KMLReady;

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

    public void reloadPlugin() throws Exception {
        // init files and directories
        new DirectoryManager(FilePaths.ROOT_FOLDER).mkdir();
        new DirectoryManager(FilePaths.LOCALE_FOLDER).mkdir();
        new DirectoryManager(FilePaths.ENCHANTMENT_FOLDER).mkdir();
        new FileManager(FilePaths.GENERAL_CONFIG_FILE).initFile(IOUtils.toByteArray(getResource("general.yml")));
        new FileManager(FilePaths.GEM_CONFIG_FILE).initFile(IOUtils.toByteArray(getResource("gems.yml")));
        // load configs
        generalConfig.load(FilePaths.GENERAL_CONFIG_FILE);
        gemConfig.load(FilePaths.GEM_CONFIG_FILE);
        File localeFile = new File(FilePaths.LOCALE_FOLDER, generalConfig.getString("plugin.locale_file"));
        new FileManager(localeFile).initFile(IOUtils.toByteArray(getClass().getResourceAsStream("/locale/"+generalConfig.getString("plugin.locale_file"))));
        localeConfig.load(localeFile);
        // init chat
        chat = new Chat(generalConfig.getString("plugin.prefix"));
        // reload enchantment config
        EnchantmentAPI.getRegisteredEnchantments().forEach(Enchantment::reloadConfig);
        // reload gem config
        GemAPI.getRegisteredGems().forEach(GemAPI::unregisterGem);
        gemConfig.getKeys(false).forEach(s -> {
            ConfigurationSection gem = gemConfig.getConfigurationSection(s);
            GemAPI.registerGem(new Gem(
                    s,
                    gem.getString("name"),
                    gem.getString("enchantment.id"),
                    gem.getInt("enchantment.level"),
                    gem.getDouble("success_rate.min"),
                    gem.getDouble("success_rate.max"),
                    gem.getDouble("protection_rate.min"),
                    gem.getDouble("protection_rate.max"),
                    gem.getDouble("drop_rate")
            ));
        });
        GemDropListener.init();
    }

    private void registerEnchants() {
        EnchantmentAPI.registerEnchantment(new ColouredSheep());
        EnchantmentAPI.registerEnchantment(new Wither());
        EnchantmentAPI.registerEnchantment(new Chef());
        EnchantmentAPI.registerEnchantment(new Freeze());
        EnchantmentAPI.registerEnchantment(new Blindness());
        EnchantmentAPI.registerEnchantment(new Poison());
        EnchantmentAPI.registerEnchantment(new Soulbound());
        EnchantmentAPI.registerEnchantment(new Dizziness());
        EnchantmentAPI.registerEnchantment(new Slowness());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new AttackListener(), this);
        getServer().getPluginManager().registerEvents(new KillListener(), this);
        getServer().getPluginManager().registerEvents(new GemMergeListener(), this);
        getServer().getPluginManager().registerEvents(new GemDropListener(), this);
        getServer().getPluginManager().registerEvents(KMLReady ? new DeathDropListener.KeepMyLife() : new DeathDropListener.Default(), this);
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
        // integrations
        if(KMLReady = getServer().getPluginManager().isPluginEnabled("KeepMyLife")){
            chat.sendConsole("&aHooked to KeepMyLife successfully!");
        }
        // register stuffs
        registerListeners();
        registerEnchants();
        registerCommand();

        chat.sendConsole("&eDonate me if you like this plugin <3");
        chat.sendConsole("&ehttps://paypal.me/anhcraft");
    }
}
