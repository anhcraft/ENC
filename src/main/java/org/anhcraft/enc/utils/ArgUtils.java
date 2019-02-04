package org.anhcraft.enc.utils;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.spaciouslib.utils.Group;

import java.util.Arrays;

public class ArgUtils {
    public static Enchantment onlyEnchant(String[] args, int from){
        if(ENC.getGeneralConfig().getBoolean("commands.use_enchantment_by_id")){
            return ENC.getApi().getEnchantmentById(args[args.length-1]);
        } else {
            return ENC.getApi().getEnchantmentByName(String.join(" ", Arrays.copyOfRange(args, from, args.length)));
        }
    }

    public static Group<Enchantment, Integer> enchantAndLevel(String[] args, int from){
        if(ENC.getGeneralConfig().getBoolean("commands.use_enchantment_by_id")){
            return new Group<>(ENC.getApi().getEnchantmentById(args[args.length-2]),
                    Integer.parseInt(args[args.length-1]));
        } else {
            return new Group<>(ENC.getApi().getEnchantmentByName(String.join(" ", Arrays.copyOfRange(args, from, args.length-1))), Integer.parseInt(args[args.length-1]));
        }
    }
}
