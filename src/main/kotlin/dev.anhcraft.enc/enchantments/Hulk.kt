package dev.anhcraft.enc.enchantments

import dev.anhcraft.craftkit.events.ArmorChangeEvent
import dev.anhcraft.craftkit.events.ArmorEquipEvent
import dev.anhcraft.craftkit.events.ArmorUnequipEvent
import dev.anhcraft.enc.api.Enchantment
import dev.anhcraft.enc.api.ItemReport
import dev.anhcraft.enc.api.listeners.SyncChangeEquipListener
import dev.anhcraft.enc.api.listeners.SyncEquipListener
import dev.anhcraft.enc.api.listeners.SyncUnequipListener
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class Hulk : Enchantment(
        "Hulk",
        arrayOf("Make yourself stronger!"),
        "anhcraft",
        null,
        3,
        EnchantmentTarget.ARMOR
) {
    override fun onInitConfig(){
        val map = HashMap<String, Any>()
        map["slowness_level"] = "{level}-1"
        map["damage_resistance_level"] = "{level}-1"
        map["regeneration_level"] = "0"
        initConfigEntries(map)
    }

    init {
        eventListeners.add(object : SyncEquipListener() {
            override fun onEquip(equip: ItemReport, event: ArmorEquipEvent) {
                equip.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 999999, computeConfigValue("slowness_level", equip).toInt()))
                equip.player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, computeConfigValue("damage_resistance_level", equip).toInt()))
                equip.player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 999999, computeConfigValue("regeneration_level", equip).toInt()))
            }
        })

        eventListeners.add(object : SyncUnequipListener() {
            override fun onUnequip(equip: ItemReport, event: ArmorUnequipEvent) {
                equip.player.removePotionEffect(PotionEffectType.SLOW)
                equip.player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE)
                equip.player.removePotionEffect(PotionEffectType.REGENERATION)
            }
        })

        eventListeners.add(object : SyncChangeEquipListener() {
            override fun onChangeEquip(oldEquip: ItemReport, newEquip: ItemReport, event: ArmorChangeEvent, onOldEquip: Boolean) {
                if (onOldEquip){
                    event.player.removePotionEffect(PotionEffectType.SLOW)
                    event.player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE)
                    event.player.removePotionEffect(PotionEffectType.REGENERATION)
                } else{
                    event.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, 999999, computeConfigValue("slowness_level", newEquip).toInt()))
                    event.player.addPotionEffect(PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 999999, computeConfigValue("damage_resistance_level", newEquip).toInt()))
                    event.player.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 999999, computeConfigValue("regeneration_level", newEquip).toInt()))
                }
            }
        })
    }
}
