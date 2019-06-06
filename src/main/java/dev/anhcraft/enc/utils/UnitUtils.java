package dev.anhcraft.enc.utils;

public class UnitUtils {
    public static double tick2ms(double ticks){
        return ticks*50;
    }
    
    public static double ms2tick(double ms){
        return ms/50;
    }

    public static double tick2s(double ticks){
        return ticks/20;
    }

    public static double s2tick(double s){
        return s*20;
    }
}
