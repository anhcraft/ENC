package org.anhcraft.enc.api.gem;

import org.anhcraft.spaciouslib.utils.ExceptionThrower;
import org.anhcraft.spaciouslib.utils.MathUtils;
import org.anhcraft.spaciouslib.utils.RandomUtils;

/**
 * Represents a gem item.
 */
public class GemItem {
    private Gem gem;
    private double successRate;
    private double protectionRate;

    /**
     * Creates an instance of GemItem.<br>
     * The success rate and protection rate will be generated randomly and then be rounded up to two decimal places.
     * @param gem the gem
     */
    public GemItem(Gem gem) {
        this.gem = gem;
        successRate = MathUtils.round(RandomUtils.randomDouble(
                gem.getMinSuccessRate(), gem.getMaxSuccessRate()));
        protectionRate = MathUtils.round(RandomUtils.randomDouble(
                gem.getMinProtectionRate(), gem.getMaxProtectionRate()));
    }

    /**
     * Creates an instance of GemItem.
     * @param gem the gem
     * @param successRate the current success rate
     * @param protectionRate the current protection rate
     */
    public GemItem(Gem gem, double successRate, double protectionRate) {
        ExceptionThrower.ifTrue(successRate < 0 || successRate > 100, new Exception("the success rate must between 0 and 100"));
        ExceptionThrower.ifTrue(protectionRate < 0 || protectionRate > 100, new Exception("the protection rate must between 0 and 100"));
        this.gem = gem;
        this.successRate = successRate;
        this.protectionRate = protectionRate;
    }

    /**
     * Gets the gem that this item represents for.
     * @return the gem
     */
    public Gem getGem() {
        return gem;
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
