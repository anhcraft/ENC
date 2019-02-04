package org.anhcraft.enc;

import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.commands.AdminCommand;
import org.anhcraft.enc.enchantments.ColouredSheep;
import org.anhcraft.enc.listeners.AttackListener;
import org.anhcraft.spaciouslib.io.DirectoryManager;
import org.anhcraft.spaciouslib.io.FileManager;
import org.anhcraft.spaciouslib.utils.Chat;
import org.anhcraft.spaciouslib.utils.IOUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class ENC extends JavaPlugin {
    private static final File ROOT_FOLDER = new File("plugins/ENC/");
    private static final File LOCALE_FOLDER = new File(ROOT_FOLDER, "locale/");
    private static final File ENCHANTMENT_FOLDER = new File(ROOT_FOLDER, "enchantment/");
    private static final File GENERAL_CONFIG_FILE = new File(ROOT_FOLDER, "general.yml");
    private static final YamlConfiguration localeConfig = new YamlConfiguration();
    private static final YamlConfiguration generalConfig = new YamlConfiguration();
    private static EnchantmentAPI api;
    public Chat chat;

    public static EnchantmentAPI getApi() {
        return api;
    }

    public static YamlConfiguration getLocaleConfig() {
        return localeConfig;
    }

    public static YamlConfiguration getGeneralConfig() {
        return generalConfig;
    }

    public void initPlugin() throws Exception {
        // init files and directories
        new DirectoryManager(ROOT_FOLDER).mkdir();
        new DirectoryManager(LOCALE_FOLDER).mkdir();
        new DirectoryManager(ENCHANTMENT_FOLDER).mkdir();
        new FileManager(GENERAL_CONFIG_FILE).initFile(IOUtils.toByteArray(getResource("general.yml")));
        // load configs
        generalConfig.load(GENERAL_CONFIG_FILE);
        File localeFile = new File(LOCALE_FOLDER, generalConfig.getString("plugin.locale_file"));
        new FileManager(localeFile).initFile(IOUtils.toByteArray(getClass().getResourceAsStream("/locale/"+generalConfig.getString("plugin.locale_file"))));
        localeConfig.load(localeFile);
        // init chat
        chat = new Chat(generalConfig.getString("plugin.prefix"));
        // reload enchantment configs (if possible)
        if(api != null){
            api.getRegisteredEnchantments().forEach(Enchantment::reloadConfig);
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
        api = new EnchantmentAPI(this, ENCHANTMENT_FOLDER);
        // register stuffs
        registerListeners();
        registerEnchants();
        registerCommand();
        chat.sendConsole("&aPlugin has been enabled!");
        chat.sendConsole("&eDonate me if you like this plugin <3");
        chat.sendConsole("&ehttps://paypal.me/anhcraft");
    }
}
