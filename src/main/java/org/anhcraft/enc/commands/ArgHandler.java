package org.anhcraft.enc.commands;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.enc.api.EnchantmentAPI;
import org.anhcraft.enc.api.rune.Rune;
import org.anhcraft.enc.api.rune.RuneAPI;
import org.anhcraft.enc.api.rune.RuneItem;
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

    static Rune onlyRune(String[] args, int from){
        if(ENC.getGeneralConfig().getBoolean("commands.use_rune_by_id")){
            return RuneAPI.getRuneById(args[args.length-1]);
        } else {
            return RuneAPI.getRuneByName(String.join(" ", Arrays.copyOfRange(args, from, args.length)));
        }
    }

    static RuneItem runeAndRate(String[] args, int from){
        if(ENC.getGeneralConfig().getBoolean("commands.use_rune_by_id")){
            return new RuneItem(RuneAPI.getRuneById(args[args.length-3]), Double.parseDouble(args[args.length-2]), Double.parseDouble(args[args.length-1]));
        } else {
            return new RuneItem(RuneAPI.getRuneByName(String.join(" ", Arrays.copyOfRange(args, from, args.length-2))), Double.parseDouble(args[args.length-2]), Double.parseDouble(args[args.length-1]));
        }
    }
}
