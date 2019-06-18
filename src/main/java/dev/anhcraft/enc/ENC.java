package dev.anhcraft.enc;

import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.slikey.effectlib.EffectManager;
import dev.anhcraft.craftkit.common.utils.SpigetApiUtil;
import dev.anhcraft.craftkit.kits.chat.Chat;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.gem.Gem;
import dev.anhcraft.enc.api.gem.GemAPI;
import dev.anhcraft.enc.commands.AdminCommand;
import dev.anhcraft.enc.commands.PlayerCommand;
import dev.anhcraft.enc.enchantments.*;
import dev.anhcraft.enc.listeners.*;
import dev.anhcraft.enc.listeners.gem.GemDropListener;
import dev.anhcraft.enc.listeners.gem.GemMergeListener;
import dev.anhcraft.jvmkit.utils.FileUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static dev.anhcraft.enc.api.EnchantmentAPI.getRegisteredEnchantments;
import static dev.anhcraft.enc.api.EnchantmentAPI.registerEnchantment;

public final class ENC extends JavaPlugin {
    private static final File ROOT_FOLDER = new File("plugins/ENC/");
    private static final File LOCALE_FOLDER = new File(ROOT_FOLDER, "locale/");
    public static final File ENCHANTMENT_FOLDER = new File(ROOT_FOLDER, "enchantment/");
    private static final File GENERAL_CONFIG_FILE = new File(ROOT_FOLDER, "general.yml");
    private static final File OLD_GENERAL_CONFIG_FILE = new File(ROOT_FOLDER, "general.old.yml");
    private static final File GEM_CONFIG_FILE = new File(ROOT_FOLDER, "gems.yml");
    private static final YamlConfiguration localeConfig = new YamlConfiguration();
    private static final YamlConfiguration generalConfig = new YamlConfiguration();
    private static final YamlConfiguration gemConfig = new YamlConfiguration();
    private static JsonObject systemConfig;
    private static Chat chat;
    private static ENC instance;
    private static TaskChainFactory taskChainFactory;
    private static EffectManager effectManager;
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

    public static EffectManager getEffectManager() {
        return effectManager;
    }

    private void updateGeneralCof() {
        int currentVersion = systemConfig.getAsJsonPrimitive("config_version").getAsInt();
        if(generalConfig.getInt("config_version", 0) < currentVersion){
            try {
                getLogger().warning("BE WARNED THAT YOUR CONFIGURATION IS OUTDATED!");
                getLogger().info("We are going to update your current configuration!");
                getLogger().info(">> Creating the backup for the old one...");
                FileUtil.copy(GENERAL_CONFIG_FILE, OLD_GENERAL_CONFIG_FILE);
                getLogger().info(">> Updating the configuration to be latest...");
                FileUtil.write(GENERAL_CONFIG_FILE, getResource("general.yml"));
                generalConfig.load(GENERAL_CONFIG_FILE);
                getLogger().info("Updated successfully!");
            } catch(Exception e) {
                getLogger().warning("Failed to upgrade the configuration!");
                getLogger().warning("For security reasons, the plugin will be disabled!");
                e.printStackTrace();
                taskChainFactory.newChain().delay(60).sync(() -> getServer().getPluginManager().disablePlugin(this)).execute();
            }
        }
    }

    private void updateLocaleConf(File localeFile){
        var mainLocale = YamlConfiguration.loadConfiguration(new InputStreamReader(getClass().getResourceAsStream("/locale/en-us.yml")));
        var needSave = false;
        for(String s : mainLocale.getKeys(true)){
            if(!localeConfig.isSet(s)) {
                localeConfig.set(s, mainLocale.get(s));
                needSave = true;
            }
        }
        if(needSave) {
            try {
                localeConfig.save(localeFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reloadPlugin() throws Exception {
        // init files and directories
        ROOT_FOLDER.mkdir();
        LOCALE_FOLDER.mkdir();
        ENCHANTMENT_FOLDER.mkdir();
        FileUtil.init(GENERAL_CONFIG_FILE, getResource("general.yml"));
        FileUtil.init(GEM_CONFIG_FILE, getResource("gems.yml"));

        // load main configs
        systemConfig = new Gson().fromJson(
                new String(getResource("system.json").readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);
        generalConfig.load(GENERAL_CONFIG_FILE);
        // update general config
        updateGeneralCof();

        // load other configs
        gemConfig.load(GEM_CONFIG_FILE);
        var localeFile = new File(LOCALE_FOLDER, generalConfig.getString("plugin.locale_file"));
        FileUtil.init(localeFile, getClass().getResourceAsStream("/locale/"+generalConfig.getString("plugin.locale_file")));
        localeConfig.load(localeFile);
        // update locale config
        updateLocaleConf(localeFile);

        // init chat
        chat = new Chat(generalConfig.getString("plugin.prefix"));
        // reload enchantment config
        getRegisteredEnchantments().forEach(Enchantment::reloadConfig);
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
        registerEnchantment(new ColouredSheep());
        registerEnchantment(new Wither());
        registerEnchantment(new Chef());
        registerEnchantment(new Freeze());
        registerEnchantment(new Blindness());
        registerEnchantment(new Poison());
        registerEnchantment(new Soulbound());
        registerEnchantment(new Dizziness());
        registerEnchantment(new Slowness());
        registerEnchantment(new Vampire());
        registerEnchantment(new Collapse());
        registerEnchantment(new Digger());
        registerEnchantment(new Starvation());
        registerEnchantment(new Spray());
        registerEnchantment(new Illuminati());
        registerEnchantment(new AntiGravity());
        registerEnchantment(new Antidote());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new AttackListener(), this);
        getServer().getPluginManager().registerEvents(new KillListener(), this);
        getServer().getPluginManager().registerEvents(new JumpListener(), this);
        getServer().getPluginManager().registerEvents(new EquipListener(), this);
        getServer().getPluginManager().registerEvents(new UnequipListener(), this);
        getServer().getPluginManager().registerEvents(new EquipChangeListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);

        getServer().getPluginManager().registerEvents(new GemMergeListener(), this);
        getServer().getPluginManager().registerEvents(new GemDropListener(), this);

        getServer().getPluginManager().registerEvents(KMLReady ? new DeathDropListener.KeepMyLife() : new DeathDropListener.Default(), this);
    }

    private void registerCommand() {
        var cm = new PaperCommandManager(this);
        cm.registerCommand(new PlayerCommand());
        cm.registerCommand(new AdminCommand());
    }

    @Override
    public void onEnable() {
        instance = this;
        taskChainFactory = BukkitTaskChainFactory.create(this);
        effectManager = new EffectManager(this);
        // init plugin
        try {
            reloadPlugin();
        } catch(Exception e) {
            e.printStackTrace();
        }
        // integrations
        if(KMLReady = getServer().getPluginManager().isPluginEnabled("KeepMyLife"))
            chat.messageConsole("&aHooked to KeepMyLife successfully!");
        // register stuffs
        registerListeners();
        registerEnchants();
        registerCommand();

        chat.messageConsole("&eDonate me if you like this plugin <3");
        chat.messageConsole("&ehttps://paypal.me/anhcraft");

        if(generalConfig.getBoolean("plugin.allow_check_update")){
            new BukkitRunnable() {
                @Override
                public void run() {
                    var expect = SpigetApiUtil.getResourceLatestVersion("64871").chars().sum();
                    var current = getDescription().getVersion().chars().sum();
                    if(current < expect) chat.messageConsole("&cENC is outdated! Please consider updating xD");
                }
            }.runTaskLater(this, 60);
        }
    }
}
