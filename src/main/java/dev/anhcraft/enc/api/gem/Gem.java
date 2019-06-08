package dev.anhcraft.enc.api.gem;

import dev.anhcraft.enc.api.Enchantment;
import dev.anhcraft.enc.api.EnchantmentAPI;
import org.jetbrains.annotations.NotNull;
import dev.anhcraft.jvmkit.utils.Condition;

import java.util.Objects;

/**
 * Represents a gem.
 */
public class Gem {
    private String id;
    private String name;
    private String enchantmentId;
    private int enchantmentLevel;
    private double minSuccessRate;
    private double maxSuccessRate;
    private double minProtectionRate;
    private double maxProtectionRate;
    private double dropRate;

    /**
     * Creates an instance of Gem.
     * @param id the id of gem
     * @param name the name of gem
     * @param enchantmentId the enchantment which is contained in this gem
     * @param enchantmentLevel the enchantment level
     * @param minSuccessRate the minimum success rate
     * @param maxSuccessRate the maximum success rate
     * @param minProtectionRate the minimum protection rate
     * @param maxProtectionRate the maximum protection rate
     * @param dropRate the drop rate
     */
    public Gem(@NotNull String id, @NotNull String name, @NotNull String enchantmentId, int enchantmentLevel, double minSuccessRate, double maxSuccessRate, double minProtectionRate, double maxProtectionRate, double dropRate) {
        Condition.argNotNull("id", id);
        Condition.argNotNull("name", name);
        Condition.argNotNull("enchantmentId", enchantmentId);
        Condition.check(id.matches("^[\\w]+$"), "the gem id must only contain A-Z, 0-9 and underscore");
        this.id = id;
        this.name = name;
        this.enchantmentId = enchantmentId;
        this.enchantmentLevel = Math.max(enchantmentLevel, 1);
        this.minSuccessRate = Math.max(minSuccessRate, 0);
        this.maxSuccessRate = Math.min(maxSuccessRate, 100);
        this.minProtectionRate = Math.max(minProtectionRate, 0);
        this.maxProtectionRate = Math.min(maxProtectionRate, 100);
        this.dropRate = dropRate;
    }

    /**
     * Returns the id of this gem.
     * @return gem's id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of this gem.
     * @return gem's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the id of enchantment which is contained in this gem.
     * @return enchantment's id
     */
    public String getEnchantmentId() {
        return enchantmentId;
    }

    /**
     * Returns the enchantment which is contained in this gem.
     * @return enchantment
     */
    public Enchantment getEnchantment() {
        return EnchantmentAPI.getEnchantmentById(enchantmentId);
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
     * Returns the drop rate.
     * @return drop rate
     */
    public double getDropRate() {
        return dropRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gem gem = (Gem) o;
        return enchantmentLevel == gem.enchantmentLevel &&
                Double.compare(gem.minSuccessRate, minSuccessRate) == 0 &&
                Double.compare(gem.maxSuccessRate, maxSuccessRate) == 0 &&
                Double.compare(gem.minProtectionRate, minProtectionRate) == 0 &&
                Double.compare(gem.maxProtectionRate, maxProtectionRate) == 0 &&
                Double.compare(gem.dropRate, dropRate) == 0 &&
                id.equals(gem.id) &&
                name.equals(gem.name) &&
                enchantmentId.equals(gem.enchantmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, enchantmentId);
    }
}
