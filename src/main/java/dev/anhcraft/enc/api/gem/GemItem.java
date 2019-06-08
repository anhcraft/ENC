package dev.anhcraft.enc.api.gem;

import org.jetbrains.annotations.NotNull;
import dev.anhcraft.jvmkit.utils.Condition;
import dev.anhcraft.jvmkit.utils.MathUtil;
import dev.anhcraft.jvmkit.utils.RandomUtil;

/**
 * Represents an item which was assigned as a gem.
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
    public GemItem(@NotNull Gem gem) {
        Condition.argNotNull("gem", gem);
        this.gem = gem;
        successRate = MathUtil.round(RandomUtil.randomDouble(
                gem.getMinSuccessRate(), gem.getMaxSuccessRate()), 3);
        protectionRate = MathUtil.round(RandomUtil.randomDouble(
                gem.getMinProtectionRate(), gem.getMaxProtectionRate()), 3);
    }

    /**
     * Creates an instance of GemItem.
     * @param gem the gem
     * @param successRate the current success rate
     * @param protectionRate the current protection rate
     */
    public GemItem(@NotNull Gem gem, double successRate, double protectionRate) {
        Condition.argNotNull("gem", gem);
        Condition.check(100 >= successRate && successRate >= 0, "the success rate must be between 0 and 100");
        Condition.check(100 >= protectionRate && protectionRate >= 0, "the protection rate must be between 0 and 100");
        this.gem = gem;
        this.successRate = successRate;
        this.protectionRate = protectionRate;
    }

    /**
     * Gets the original gem.
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
