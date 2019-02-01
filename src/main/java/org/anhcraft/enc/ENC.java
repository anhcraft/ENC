package org.anhcraft.enc;

import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.commands.AdminCommand;
import org.anhcraft.enc.enchantments.ColouredSheep;
import org.anhcraft.enc.listeners.AttackListener;
import org.anhcraft.enc.utils.DelayedRunnable;
import org.anhcraft.spaciouslib.io.DirectoryManager;
import org.anhcraft.spaciouslib.io.FileManager;
import org.anhcraft.spaciouslib.utils.Chat;
import org.anhcraft.spaciouslib.utils.IOUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public final class ENC extends JavaPlugin {
    private static final File ROOT_FOLDER = new File("plugins/ENC/");
    private static final File LOCALE_FOLDER = new File(ROOT_FOLDER, "locale/");
    private static final File ENCHANT_CONFIG_FILE = new File(ROOT_FOLDER, "enchantments.yml");
    private static final File MAIN_CONFIG_FILE = new File(ROOT_FOLDER, "config.yml");
    private static final  FileConfiguration enchantConfig = new YamlConfiguration();
    public final FileConfiguration mainConfig = new YamlConfiguration();
    public final FileConfiguration localeConfig = new YamlConfiguration();
    private static DelayedRunnable enchantConfigSaver;
    private static EnchantmentAPI api;
    public Chat chat;

    public static EnchantmentAPI getApi() {
        return api;
    }

    private void initPlugin() throws Exception {
        // init files and directories
        new DirectoryManager(LOCALE_FOLDER).mkdirs();
        new FileManager(ENCHANT_CONFIG_FILE).create();
        new FileManager(MAIN_CONFIG_FILE).initFile(IOUtils.toByteArray(getResource("config.yml")));
        // load configs
        enchantConfig.load(ENCHANT_CONFIG_FILE);
        mainConfig.load(MAIN_CONFIG_FILE);
        File localeFile = new File(LOCALE_FOLDER, mainConfig.getString("general.locale_file"));
        new FileManager(localeFile).initFile(IOUtils.toByteArray(getClass().getResourceAsStream("/locale/"+mainConfig.getString("general.locale_file"))));
        localeConfig.load(localeFile);
        // init chat
        chat = new Chat(mainConfig.getString("general.prefix"));
        // init enchant-config saver
        if(enchantConfigSaver == null){
            enchantConfigSaver = new DelayedRunnable(this, () -> {
                try {
                    enchantConfig.save(ENCHANT_CONFIG_FILE);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }, mainConfig.getLong("enchant_config_saver.delay_duration"), mainConfig.getBoolean("enchant_config_saver.use_async"));
        } else {
            enchantConfigSaver.setAsync(mainConfig.getBoolean("enchant_config_saver.use_async"));
            enchantConfigSaver.setDuration(mainConfig.getLong("enchant_config_saver.delay_duration"));
            // apply enchantment config
            api.applyEnchantmentConfigs();
        }
    }

    public void reloadPlugin() {
        if(mainConfig.getBoolean("general.reload_async", true)){
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        initPlugin();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(this);
        } else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        initPlugin();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }.runTask(this);
        }
    }

    private void registerEnchants() {
        api.registerEnchantment(new ColouredSheep());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new AttackListener(), this);
    }

    private void registerCommand() {
        new AdminCommand(this).run();
    }

    @Override
    public void onEnable() {
        // init plugin
        try {
            initPlugin();
        } catch(Exception e) {
            e.printStackTrace();
        }
        // init API
        api = new EnchantmentAPI(enchantConfig, enchantConfigSaver);
        // register stuffs
        registerListeners();
        registerEnchants();
        registerCommand();
        chat.sendConsole("&aPlugin has been enabled!");
        chat.sendConsole("&eDonate me if you like this plugin <3");
        chat.sendConsole("&ehttps://paypal.me/anhcraft");
    }
}
