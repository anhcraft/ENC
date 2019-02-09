package org.anhcraft.enc.utils;

import org.anhcraft.spaciouslib.utils.RandomUtils;

import java.util.List;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Fitness_proportionate_selection">https://en.wikipedia.org/wiki/Fitness_proportionate_selection</a>
 */
public class RouletteSelect {
    public static int chooseFromList(List<Double> list){
        double weight_sum = list.stream().mapToDouble(Double::doubleValue).sum();
        double value = RandomUtils.randomDouble(0, weight_sum);
        for(int i = 0; i < list.size(); i++) {
            if(value > 0) {
                value -= list.get(i);
            } else {
                return i;
            }
        }
        return list.size()-1;
    }
}
