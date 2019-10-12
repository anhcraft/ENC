package dev.anhcraft.enc.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.anhcraft.craftkit.utils.ItemUtil;
import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.gem.GemAPI;
import dev.anhcraft.enc.api.gem.GemItem;
import dev.anhcraft.jvmkit.lang.enumeration.RegEx;
import kotlin.Pair;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

@CommandAlias("enca|encadmin|/enc")
public class AdminCommand extends BaseCommand {
    @Default
    @CatchUnknown
    public void root(CommandSender sender) {
        ENC.getPluginChat()
                .message(sender, "//enc: show all commands for admin")
                .message(sender, "//enc enchant list: list all available enchantments")
                .message(sender, "//enc enchant add <id/name> <level>: add an enchantment")
                .message(sender, "//enc enchant remove <id/name>: remove an enchantment")
                .message(sender, "//enc enchant removeall: remove all enchantments")
                .message(sender, "//enc gem list: lists all available gems")
                .message(sender, "//enc gem assign <id/name> <successRate> <protectionRate>: assigns the item in your hand to be a gem")
                .message(sender, "//enc gem detach: detaches the gem from the item in your hand")
                .message(sender, "//enc reload: reload the plugin")
                .message(sender, "Aliases: /enca, /encadmin");
    }

    @Subcommand("enchant list")
    @CommandPermission("enc.command.enchant.list")
    public void listEnchants(CommandSender sender){
        ENC.getPluginChat().message(sender, ENC.getLocaleConfig().getString("list_available_enchantments"));
        if(ENC.getGeneralConfig().getBoolean("commands.use_enchantment_by_id"))
            ENC.getPluginChat().message(sender, String.join(", ", EnchantmentAPI.getAvailableEnchantmentIds()));
        else sender.sendMessage(String.join(", ", EnchantmentAPI.getAvailableEnchantmentNames()));
    }

    @Subcommand("enchant add")
    @CommandPermission("enc.command.enchant.add")
    public void addEnchant(Player player, String[] args){
        if(args.length < 2) {
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("missing_required_arguments"));
            return;
        }
        if(!RegEx.INTEGER.valid(args[args.length-1])) {
            ENC.getPluginChat().message(player,
                    String.format(ENC.getLocaleConfig().getString("arg_must_be_integer"), "level"));
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if(ItemUtil.isNull(item)){
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("must_hold_item"));
            return;
        }
        Pair<Enchantment, Integer> enchantment = ArgHandler.enchantAndLevel(args);
        if(enchantment.getFirst() == null){
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("enchantment_not_found"));
            return;
        }
        if(enchantment.getSecond() < 1){
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("too_small_enchantment_level"));
            return;
        }
        if(!ENC.getGeneralConfig().getBoolean("commands.unsafe_enchantment")){
            if(!enchantment.getFirst().isEnabled()){
                ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("enchantment_not_enabled"));
                return;
            }
            if(enchantment.getSecond() > enchantment.getFirst().getMaxLevel()){
                ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("over_limited_enchantment_level"));
                return;
            }
            if(!enchantment.getFirst().canEnchantItem(item)){
                ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("unsuitable_item"));
                return;
            }
        }
        EnchantmentAPI.addEnchantment(item, enchantment.getFirst(), enchantment.getSecond());
    }

    @Subcommand("enchant remove")
    @CommandPermission("enc.command.enchant.remove")
    public void removeEnchant(Player player, @Single String enchant){
        ItemStack item = player.getInventory().getItemInMainHand();
        if(ItemUtil.isNull(item)){
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("must_hold_item"));
            return;
        }
        Enchantment enchantment = ArgHandler.onlyEnchant(enchant);
        if(enchantment == null){
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("enchantment_not_found"));
            return;
        }
        EnchantmentAPI.removeEnchantment(item, enchantment);
    }

    @Subcommand("enchant removeall")
    @CommandPermission("enc.command.enchant.removeall")
    public void removeAllEnchant(Player player){
        ItemStack item = player.getInventory().getItemInMainHand();
        if(ItemUtil.isNull(item)){
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("must_hold_item"));
            return;
        }
        EnchantmentAPI.removeEnchantments(item);
    }

    @Subcommand("gem list")
    @CommandPermission("enc.command.gem.list")
    public void listGems(CommandSender sender){
        ENC.getPluginChat().message(sender, ENC.getLocaleConfig().getString("list_available_gems"));
        if(ENC.getGeneralConfig().getBoolean("commands.use_gem_by_id"))
            ENC.getPluginChat().message(sender, String.join(", ", GemAPI.getRegisteredGemIds()));
        else sender.sendMessage(String.join(", ", GemAPI.getRegisteredGemNames()));
    }


    @Subcommand("gem assign")
    @CommandPermission("enc.command.gem.assign")
    public void assignGem(Player player, String[] args){
        if(args.length < 3) {
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("missing_required_arguments"));
            return;
        }
        if(!RegEx.DECIMAL.valid(args[args.length-2])) {
            ENC.getPluginChat().message(player,
                    String.format(ENC.getLocaleConfig().getString("arg_must_be_real_number"), "successRate"));
            return;
        }
        if(!RegEx.DECIMAL.valid(args[args.length-1])) {
            ENC.getPluginChat().message(player,
                    String.format(ENC.getLocaleConfig().getString("arg_must_be_real_number"), "protectionRate"));
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if(ItemUtil.isNull(item)){
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("must_hold_item"));
            return;
        }
        GemItem gem = ArgHandler.gemAndRate(args);
        if(gem == null){
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("gem_not_found"));
            return;
        }
        GemAPI.assignGem(item, gem);
    }

    @Subcommand("gem detach")
    @CommandPermission("enc.command.gem.detach")
    public void detachGem(Player player){
        ItemStack item = player.getInventory().getItemInMainHand();
        if(ItemUtil.isNull(item)){
            ENC.getPluginChat().message(player, ENC.getLocaleConfig().getString("must_hold_item"));
            return;
        }
        GemAPI.detachGem(item);
    }

    @Subcommand("reload")
    @CommandPermission("enc.command.reload")
    public void reload(CommandSender sender){
        if(ENC.getGeneralConfig().getBoolean("commands.async_reload")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    doReload(sender);
                }
            }.runTaskAsynchronously(ENC.getInstance());
        } else doReload(sender);
    }

    private void doReload(CommandSender sender){
        try {
            ENC.getInstance().reloadPlugin();
        } catch(Exception e) {
            e.printStackTrace();
        }
        ENC.getPluginChat().message(sender, ENC.getLocaleConfig().getString("plugin_reloaded"));
    }
}
