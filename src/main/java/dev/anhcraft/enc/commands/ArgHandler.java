package dev.anhcraft.enc.commands;

import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.gem.Gem;
import dev.anhcraft.enc.api.gem.GemAPI;
import dev.anhcraft.enc.api.gem.GemItem;
import org.anhcraft.spaciouslib.utils.Group;

import java.util.Arrays;

class ArgHandler {
    static Enchantment onlyEnchant(String[] args, int from){
        if(ENC.getGeneralConfig().getBoolean("commands.use_enchantment_by_id")){
            return EnchantmentAPI.getEnchantmentById(args[args.length-1]);
        } else {
            return EnchantmentAPI.getEnchantmentByName(String.join(" ", Arrays.copyOfRange(args, from, args.length)));
        }
    }

    static Group<Enchantment, Integer> enchantAndLevel(String[] args, int from){
        if(ENC.getGeneralConfig().getBoolean("commands.use_enchantment_by_id")){
            return new Group<>(EnchantmentAPI.getEnchantmentById(args[args.length-2]),
                    Integer.parseInt(args[args.length-1]));
        } else {
            return new Group<>(EnchantmentAPI.getEnchantmentByName(String.join(" ", Arrays.copyOfRange(args, from, args.length-1))), Integer.parseInt(args[args.length-1]));
        }
    }

    static Gem onlyGem(String[] args, int from){
        if(ENC.getGeneralConfig().getBoolean("commands.use_gem_by_id")){
            return GemAPI.getGemById(args[args.length-1]);
        } else {
            return GemAPI.getGemByName(String.join(" ", Arrays.copyOfRange(args, from, args.length)));
        }
    }

    static GemItem gemAndRate(String[] args, int from){
        if(ENC.getGeneralConfig().getBoolean("commands.use_gem_by_id")){
            return new GemItem(GemAPI.getGemById(args[args.length-3]), Double.parseDouble(args[args.length-2]), Double.parseDouble(args[args.length-1]));
        } else {
            return new GemItem(GemAPI.getGemByName(String.join(" ", Arrays.copyOfRange(args, from, args.length-2))), Double.parseDouble(args[args.length-2]), Double.parseDouble(args[args.length-1]));
        }
    }
}
