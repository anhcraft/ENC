package org.anhcraft.enc.api.rune;

import org.anhcraft.spaciouslib.utils.ExceptionThrower;
import org.anhcraft.spaciouslib.utils.MathUtils;
import org.anhcraft.spaciouslib.utils.RandomUtils;

/**
 * Represents a rune item.
 */
public class RuneItem {
    private Rune rune;
    private double successRate;
    private double protectionRate;

    /**
     * Creates an instance of RuneItem.<br>
     * The success rate and protection rate will be generated randomly and then be rounded up to two decimal places.
     * @param rune the rune
     */
    public RuneItem(Rune rune) {
        this.rune = rune;
        successRate = MathUtils.round(RandomUtils.randomDouble(
                rune.getMinSuccessRate(), rune.getMaxSuccessRate()));
        protectionRate = MathUtils.round(RandomUtils.randomDouble(
                rune.getMinProtectionRate(), rune.getMaxProtectionRate()));
    }

    /**
     * Creates an instance of RuneItem.
     * @param rune the rune
     * @param successRate the current success rate
     * @param protectionRate the current protection rate
     */
    public RuneItem(Rune rune, double successRate, double protectionRate) {
        ExceptionThrower.ifTrue(successRate < 0 || successRate > 100, new Exception("the success rate must between 0 and 100"));
        ExceptionThrower.ifTrue(protectionRate < 0 || protectionRate > 100, new Exception("the protection rate must between 0 and 100"));
        this.rune = rune;
        this.successRate = successRate;
        this.protectionRate = protectionRate;
    }

    /**
     * Gets the rune that this item represents for.
     * @return the rune
     */
    public Rune getRune() {
        return rune;
    }

    /**
     * Gets the current success rate.
     * @return success rate
     */
    public double getSuccessRate() {
        return successRate;
    }

    /**
     * Gets the current protection rate.
     * @return protection rate
     */
    public double getProtectionRate() {
        return protectionRate;
    }
}
