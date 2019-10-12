package dev.anhcraft.enc.cmd;

import dev.anhcraft.enc.ENC;
import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import dev.anhcraft.enc.api.gem.Gem;
import dev.anhcraft.enc.api.gem.GemAPI;
import dev.anhcraft.enc.api.gem.GemItem;
import kotlin.Pair;

import java.util.Arrays;

class ArgHandler {
    static Enchantment onlyEnchant(String s){
        if(ENC.getGeneralConfig().getBoolean("commands.use_enchantment_by_id")) return EnchantmentAPI.getEnchantmentById(s);
        else return EnchantmentAPI.getEnchantmentByName(s);
    }

    static Pair<Enchantment, Integer> enchantAndLevel(String[] args){
        if(ENC.getGeneralConfig().getBoolean("commands.use_enchantment_by_id")) return new Pair<>(EnchantmentAPI.getEnchantmentById(args[args.length-2]), Integer.parseInt(args[args.length-1]));
        else return new Pair<>(EnchantmentAPI.getEnchantmentByName(String.join(" ", Arrays.copyOfRange(args, 0, args.length-1))), Integer.parseInt(args[args.length-1]));
    }

    static GemItem gemAndRate(String[] args){
        if(ENC.getGeneralConfig().getBoolean("commands.use_gem_by_id")) {
            Gem g = GemAPI.getGemById(args[args.length-3]);
            return g == null ? null : new GemItem(g, Double.parseDouble(args[args.length-2]), Double.parseDouble(args[args.length-1]));
        } else {
            Gem g = GemAPI.getGemByName(String.join(" ", Arrays.copyOfRange(args, 0, args.length-2)));
            return g == null ? null : new GemItem(g, Double.parseDouble(args[args.length-2]), Double.parseDouble(args[args.length-1]));
        }
    }
}
