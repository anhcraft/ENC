package org.anhcraft.enc.commands;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.api.gem.Gem;
import org.anhcraft.enc.api.gem.GemAPI;
import org.anhcraft.enc.api.gem.GemItem;
import org.anhcraft.spaciouslib.builders.command.*;
import org.anhcraft.spaciouslib.utils.Group;
import org.anhcraft.spaciouslib.utils.InventoryUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AdminCommand implements Runnable {
    private static final Argument[] LIST_ENCHANTMENT_CMD = new ChildCommandBuilder().path("enchant list", new CommandCallback() {
        @Override
        public void run(CommandBuilder commandBuilder, CommandSender sender, int i, String[] args, int i1, String s) {
            if(sender.hasPermission("enc.command.enchant.list")) {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("list_available_enchantments"), sender);
                if(ENC.getGeneralConfig().getBoolean("commands.use_enchantment_by_id")) {
                    ENC.getPluginChat().sendCommandSenderNoPrefix(String.join(", ",
                            EnchantmentAPI.getAvailableEnchantmentIds()), sender);
                } else{
                    // we do not color the string here
                    sender.sendMessage(String.join(", ", EnchantmentAPI.getAvailableEnchantmentNames()));
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), sender);
            }
        }
    }).build();

    private static final Argument[] ADD_ENCHANTMENT_CMD = new ChildCommandBuilder().path("enchant add").var("name", ArgumentType.ANYTHING).var("level", new CommandCallback() {
        @Override
        public void run(CommandBuilder commandBuilder, CommandSender sender, int i, String[] args, int i1, String s) {
            if(sender.hasPermission("enc.command.enchant.add")) {
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(InventoryUtils.isNull(item)){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_hold_item"), sender);
                        return;
                    }
                    Group<Enchantment, Integer> enchantment = ArgHandler.enchantAndLevel(args, 1);
                    if(enchantment.getA() == null){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("enchantment_not_found"), sender);
                        return;
                    }
                    if(enchantment.getB() < 1){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("invalid_enchantment_level"), sender);
                        return;
                    }
                    if(!ENC.getGeneralConfig().getBoolean("commands.unsafe_enchantment")){
                        if(!enchantment.getA().isEnabled()){
                            ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("enchantment_not_enabled"), sender);
                            return;
                        }
                        if(enchantment.getB() > enchantment.getA().getMaxLevel()){
                            ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("over_limited_enchantment_level"), sender);
                            return;
                        }
                        if(!enchantment.getA().canEnchantItem(item)){
                            ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("unsuitable_item"), sender);
                            return;
                        }
                    }
                    EnchantmentAPI.addEnchantment(item, enchantment.getA(), enchantment.getB());
                } else {
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_be_player"), sender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), sender);
            }
        }
    }, ArgumentType.POSITIVE_INTEGER).build();

    private static final Argument[] REMOVE_ENCHANTMENT_CMD = new ChildCommandBuilder().path("enchant remove").var("name", new CommandCallback() {
        @Override
        public void run(CommandBuilder commandBuilder, CommandSender sender, int i, String[] args, int i1, String s) {
            if(sender.hasPermission("enc.command.enchant.remove")) {
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(InventoryUtils.isNull(item)){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_hold_item"), sender);
                        return;
                    }
                    Enchantment enchantment = ArgHandler.onlyEnchant(args, 1);
                    if(enchantment == null){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("enchantment_not_found"), sender);
                        return;
                    }
                    EnchantmentAPI.removeEnchantment(item, enchantment);
                } else {
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_be_player"), sender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), sender);
            }
        }
    }, ArgumentType.ANYTHING).build();

    private static final Argument[] REMOVE_ALL_ENCHANTMENT_CMD = new ChildCommandBuilder().path("enchant removeall", new CommandCallback() {
        @Override
        public void run(CommandBuilder builder, CommandSender sender, int command, String[] args, int arg, String value) {
            if(sender.hasPermission("enc.command.enchant.removeall")) {
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(InventoryUtils.isNull(item)){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_hold_item"), sender);
                        return;
                    }
                    EnchantmentAPI.removeEnchantments(item);
                } else {
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_be_player"), sender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), sender);
            }
        }
    }).build();

    private static final Argument[] LIST_GEM_CMD = new ChildCommandBuilder().path("gem list", new CommandCallback() {
        @Override
        public void run(CommandBuilder commandBuilder, CommandSender sender, int i, String[] args, int i1, String s) {
            if(sender.hasPermission("enc.command.gem.list")) {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("list_available_gems"), sender);
                if(ENC.getGeneralConfig().getBoolean("commands.use_gem_by_id")) {
                    ENC.getPluginChat().sendCommandSenderNoPrefix(String.join(", ",
                            GemAPI.getRegisteredGemIds()), sender);
                } else{
                    // we do not color the string here
                    sender.sendMessage(String.join(", ", GemAPI.getRegisteredGemNames()));
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), sender);
            }
        }
    }).build();

    private static final Argument[] ASSIGN_GEM_CMD = new ChildCommandBuilder().path("gem assign").var("name", new CommandCallback() {
        @Override
        public void run(CommandBuilder commandBuilder, CommandSender sender, int i, String[] args, int i1, String s) {
            if(sender.hasPermission("enc.command.gem.assign")) {
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(InventoryUtils.isNull(item)){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_hold_item"), sender);
                        return;
                    }
                    Gem gem = ArgHandler.onlyGem(args, 1);
                    if(gem == null){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("gem_not_found"), sender);
                        return;
                    }
                    GemAPI.assignGem(item, new GemItem(gem));
                } else {
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_be_player"), sender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), sender);
            }
        }
    }, ArgumentType.ANYTHING).var("successRate", ArgumentType.POSITIVE_REAL_NUMBER).var("protectionRate", new CommandCallback() {
        @Override
        public void run(CommandBuilder builder, CommandSender sender, int command, String[] args, int arg, String value) {
            if(sender.hasPermission("enc.command.gem.assign")) {
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(InventoryUtils.isNull(item)){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_hold_item"), sender);
                        return;
                    }
                    GemItem gem = ArgHandler.gemAndRate(args, 1);
                    if(gem.getGem() == null){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("gem_not_found"), sender);
                        return;
                    }
                    GemAPI.assignGem(item, gem);
                } else {
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_be_player"), sender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), sender);
            }
        }
    }, ArgumentType.POSITIVE_REAL_NUMBER).build();
    
    private static final Argument[] DETACH_GEM_CMD = new ChildCommandBuilder().path("gem detach", new CommandCallback() {
        @Override
        public void run(CommandBuilder builder, CommandSender sender, int command, String[] args, int arg, String value) {
            if(sender.hasPermission("enc.command.gem.detach")) {
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(InventoryUtils.isNull(item)){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_hold_item"), sender);
                        return;
                    }
                    GemAPI.detachGem(item);
                } else {
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_be_player"), sender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), sender);
            }
        }
    }).build();

    private static final Argument[] RELOAD_CMD = new ChildCommandBuilder().path("reload", new CommandCallback() {
        @Override
        public void run(CommandBuilder commandBuilder, CommandSender commandSender, int i, String[] strings, int i1, String s) {
            if(commandSender.hasPermission("enc.command.reload")) {
                if(ENC.getGeneralConfig().getBoolean("commands.async_reload")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                ENC.getInstance().reloadPlugin();
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                            ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("plugin_reloaded"), commandSender);
                        }
                    }.runTaskAsynchronously(ENC.getInstance());
                } else {
                    try {
                        ENC.getInstance().reloadPlugin();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("plugin_reloaded"), commandSender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), commandSender);
            }
        }
    }).build();

    @Override
    public void run() {
        new CommandBuilder("enca", new CommandCallback() {
            @Override
            public void run(CommandBuilder commandBuilder, CommandSender commandSender, int i, String[] strings, int i1, String s) {
                commandBuilder.sendHelpMessages(commandSender, true, false);
            }
        })
        .addChild("lists all available enchantments", LIST_ENCHANTMENT_CMD)
        .addChild("adds an enchantment to the holding item", ADD_ENCHANTMENT_CMD)
        .addChild("removes an existing enchantment out of the holding item", REMOVE_ENCHANTMENT_CMD)
        .addChild("removes all existing enchantments out of the holding item", REMOVE_ALL_ENCHANTMENT_CMD)
                .addChild("lists all available gems", LIST_GEM_CMD)
        .addChild("assigns a gem to the holding item", ASSIGN_GEM_CMD)
                .addChild("detaches the gem from the holding item", DETACH_GEM_CMD)
        .addChild("reloads the plugin", RELOAD_CMD)
        .addAlias("encadmin")
        .build(ENC.getInstance());
    }
}
