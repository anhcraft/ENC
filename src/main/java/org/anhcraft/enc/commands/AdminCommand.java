package org.anhcraft.enc.commands;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.utils.ArgUtils;
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
        public void run(CommandBuilder commandBuilder, CommandSender commandSender, int i, String[] strings, int i1, String s) {
            if(commandSender.hasPermission("enc.admin.enchant.list")) {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("list_registered_enchantments"), commandSender);
                if(ENC.getGeneralConfig().getBoolean("commands.use_enchantment_by_id")) {
                    ENC.getPluginChat().sendCommandSenderNoPrefix(String.join(", ",
                            ENC.getApi().getRegisteredEnchantmentIds()), commandSender);
                } else{
                    // we do not color the string here
                    commandSender.sendMessage(String.join(", ", ENC.getApi().getRegisteredEnchantmentNames()));
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), commandSender);
            }
        }
    }).build();

    private static final Argument[] ADD_ENCHANTMENT_CMD = new ChildCommandBuilder().path("enchant add").var("name", ArgumentType.ANYTHING).var("level", new CommandCallback() {
        @Override
        public void run(CommandBuilder commandBuilder, CommandSender commandSender, int i, String[] strings, int i1, String s) {
            if(commandSender.hasPermission("enc.admin.enchant.add")) {
                if(commandSender instanceof Player){
                    Player player = (Player) commandSender;
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(InventoryUtils.isNull(item)){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_hold_item"), commandSender);
                        return;
                    }
                    Group<Enchantment, Integer> enchantment = ArgUtils.enchantAndLevel(strings, 1);
                    if(enchantment.getA() == null){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("enchantment_not_found"), commandSender);
                        return;
                    }
                    if(enchantment.getB() < 1){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("invalid_enchantment_level"), commandSender);
                        return;
                    }
                    if(!ENC.getGeneralConfig().getBoolean("commands.unsafe_enchantment")){
                        if(!enchantment.getA().isEnabled()){
                            ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("enchantment_not_enabled"), commandSender);
                            return;
                        }
                        if(enchantment.getB() > enchantment.getA().getMaxLevel()){
                            ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("over_limited_enchantment_level"), commandSender);
                            return;
                        }
                        if(!enchantment.getA().canEnchantItem(item)){
                            ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("unsuitable_item"), commandSender);
                            return;
                        }
                    }
                    ENC.getApi().addEnchantment(item, enchantment.getA(), enchantment.getB());
                } else {
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_be_player"), commandSender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), commandSender);
            }
        }
    }, ArgumentType.POSITIVE_INTEGER).build();

    private static final Argument[] REMOVE_ENCHANTMENT_CMD = new ChildCommandBuilder().path("enchant remove").var("name", new CommandCallback() {
        @Override
        public void run(CommandBuilder commandBuilder, CommandSender commandSender, int i, String[] strings, int i1, String s) {
            if(commandSender.hasPermission("enc.admin.enchant.remove")) {
                if(commandSender instanceof Player){
                    Player player = (Player) commandSender;
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(InventoryUtils.isNull(item)){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_hold_item"), commandSender);
                        return;
                    }
                    Enchantment enchantment = ArgUtils.onlyEnchant(strings, 1);
                    if(enchantment == null){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("enchantment_not_found"), commandSender);
                        return;
                    }
                    ENC.getApi().removeEnchantment(item, enchantment);
                } else {
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_be_player"), commandSender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), commandSender);
            }
        }
    }, ArgumentType.ANYTHING).build();

    private static final Argument[] REMOVE_ALL_ENCHANTMENT_CMD = new ChildCommandBuilder().path("enchant removeall", new CommandCallback() {
        @Override
        public void run(CommandBuilder builder, CommandSender commandSender, int command, String[] args, int arg, String value) {
            if(commandSender.hasPermission("enc.admin.enchant.removeall")) {
                if(commandSender instanceof Player){
                    Player player = (Player) commandSender;
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if(InventoryUtils.isNull(item)){
                        ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_hold_item"), commandSender);
                        return;
                    }
                    ENC.getApi().removeEnchantments(item);
                } else {
                    ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("must_be_player"), commandSender);
                }
            } else {
                ENC.getPluginChat().sendCommandSender(ENC.getLocaleConfig().getString("not_have_permission"), commandSender);
            }
        }
    }).build();

    private static final Argument[] RELOAD_CMD = new ChildCommandBuilder().path("reload", new CommandCallback() {
        @Override
        public void run(CommandBuilder commandBuilder, CommandSender commandSender, int i, String[] strings, int i1, String s) {
            if(commandSender.hasPermission("enc.admin.reload")) {
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
        .addChild("adds an enchantment", ADD_ENCHANTMENT_CMD)
        .addChild("removes an existing enchantment", REMOVE_ENCHANTMENT_CMD)
        .addChild("removes all existing enchantments", REMOVE_ALL_ENCHANTMENT_CMD)
        .addChild("reloads the plugin", RELOAD_CMD)
        .addAlias("encadmin")
        .build(ENC.getInstance());
    }
}
