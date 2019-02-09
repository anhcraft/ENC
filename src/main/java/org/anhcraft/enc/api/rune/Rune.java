package org.anhcraft.enc.api.rune;

import org.anhcraft.enc.ENC;
import org.anhcraft.enc.api.Enchantment;
import org.anhcraft.spaciouslib.builders.EqualsBuilder;
import org.anhcraft.spaciouslib.builders.HashCodeBuilder;
import org.anhcraft.spaciouslib.utils.ExceptionThrower;

/**
 * Represents a rune.
 */
public class Rune {
    private String id;
    private String name;
    private String enchantmentId;
    private int enchantmentLevel;
    private double minSuccessRate;
    private double maxSuccessRate;
    private double minProtectionRate;
    private double maxProtectionRate;
    private double minDropRate;
    private double maxDropRate;

    /**
     * Creates an instance of Rune.
     * @param id the id of rune
     * @param name the name of rune
     * @param enchantmentId the enchantment which is contained in this rune
     * @param enchantmentLevel the enchantment level
     * @param minSuccessRate the minimum success rate
     * @param maxSuccessRate the maximum success rate
     * @param minProtectionRate the minimum protection rate
     * @param maxProtectionRate the maximum protection rate
     * @param minDropRate the minimum drop rate
     * @param maxDropRate the maximum drop rate
     */
    public Rune(String id, String name, String enchantmentId, int enchantmentLevel, double minSuccessRate, double maxSuccessRate, double minProtectionRate, double maxProtectionRate, double minDropRate, double maxDropRate) {
        ExceptionThrower.ifFalse(id.matches("^[\\w]+$"), new Exception("the rune id must only contain A-Z, 0-9 and underscore"));
        ExceptionThrower.ifTrue(enchantmentId == null, new Exception("enchantment id must not be null"));
        this.id = id;
        this.name = name;
        this.enchantmentId = enchantmentId;
        this.enchantmentLevel = Math.max(enchantmentLevel, 1);
        this.minSuccessRate = Math.max(minSuccessRate, 0);
        this.maxSuccessRate = Math.min(maxSuccessRate, 100);
        this.minProtectionRate = Math.max(minProtectionRate, 0);
        this.maxProtectionRate = Math.min(maxProtectionRate, 100);
        this.minDropRate = Math.max(minDropRate, 0);
        this.maxDropRate = Math.min(maxDropRate, 100);
    }

    /**
     * Returns the id of this rune.
     * @return rune's id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of this rune.
     * @return rune's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id of enchantment which is contained in this rune.
     * @return enchantment's id
     */
    public String getEnchantmentId() {
        return enchantmentId;
    }

    /**
     * Returns the enchantment which is contained in this rune.
     * @return enchantment
     */
    public Enchantment getEnchantment() {
        return ENC.getApi().getEnchantmentById(enchantmentId);
    }

    /**
     * Returns the level of the enchantment.
     * @return enchantment's level
     */
    public int getEnchantmentLevel() {
        return enchantmentLevel;
    }

    /**
     * Returns the minimum success rate.
     * @return minimum success rate
     */
    public double getMinSuccessRate() {
        return minSuccessRate;
    }

    /**
     * Returns the maximum success rate.
     * @return maximum success rate
     */
    public double getMaxSuccessRate() {
        return maxSuccessRate;
    }

    /**
     * Returns the minimum protection rate.
     * @return minimum protection rate
     */
    public double getMinProtectionRate() {
        return minProtectionRate;
    }

    /**
     * Returns the maximum protection rate.
     * @return maximum protection rate
     */
    public double getMaxProtectionRate() {
        return maxProtectionRate;
    }

    /**
     * Returns the minimum drop rate.
     * @return minimum drop rate
     */
    public double getMinDropRate() {
        return minDropRate;
    }

    /**
     * Returns the maximum drop rate.
     * @return maximum drop rate
     */
    public double getMaxDropRate() {
        return maxDropRate;
    }

    @Override
    public boolean equals(Object object){
        if(object != null && object.getClass() == this.getClass()){
            Rune r = (Rune) object;
            return new EqualsBuilder()
                    .append(r.id, this.id)
                    .append(r.name, this.name)
                    .build();
        }
        return false;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 23)
                .append(this.id)
                .append(this.name)
                .append(this.enchantmentId)
                .append(this.enchantmentLevel)
                .append(this.minSuccessRate)
                .append(this.maxSuccessRate)
                .append(this.minProtectionRate)
                .append(this.maxProtectionRate).build();
    }

    /**
     * Result of applying a rune to a stack of items.
     */
    public enum ApplyResult{
        /**
         * There was a success in moving the enchantment of the rune into the stack.
         */
        SUCCESS,
        /**
         * The rune rejected the stack but luckily its strong did not break up the stack.
         */
        FAILURE,
        /**
         * The rune rejected the stack and it broken up the stack.
         */
        BROKEN
    }
}
