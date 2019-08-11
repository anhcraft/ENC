package dev.anhcraft.enc.enchantments

import dev.anhcraft.enc.ENC
import dev.anhcraft.enc.api.Enchantment
import dev.anhcraft.enc.api.ItemReport
import dev.anhcraft.enc.api.listeners.AsyncInteractListener
import dev.anhcraft.enc.utils.Cooldown
import dev.anhcraft.enc.utils.EntityFilter
import dev.anhcraft.enc.utils.PlayerMap
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.stream.Stream
import kotlin.math.cos
import kotlin.math.sin

class Windstorm : Enchantment(
        "Windstorm",
        arrayOf("Causes a windstorm to defeat nearby mobs"),
        "anhcraft",
        null,
        5,
        EnchantmentTarget.ALL
) {
    private val MAP = PlayerMap<Cooldown>()

    override fun onInitConfig(){
        val map = HashMap<String, Any>()
        map["wind_radius"] = "{level}*0.8+4"
        map["wind_height"] = "{level}*0.5+2"
        map["damage"] = "{level}*0.7+0.8"
        map["cooldown"] = "{level}*30+45"
        initConfigEntries(map)
    }

    init {
        eventListeners.add(object : AsyncInteractListener() {
            override fun onInteract(report: ItemReport, event: PlayerInteractEvent) {
                val player = report.player
                if (EquipmentSlot.OFF_HAND == event.hand || event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR)
                    return

                val cooldown = computeConfigValue("cooldown", report)
                if (!handleCooldown(MAP, player, cooldown)) return

                val radius = computeConfigValue("wind_radius", report)
                val height = computeConfigValue("wind_height", report)
                val dam = computeConfigValue("damage", report)

                val task = ENC.getTaskChainFactory().newChain<Any>()
                var i = 0.0
                while (i <= radius) {
                    val pl = player.location
                    val ii = i++
                    player.playSound(pl, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 3f, 1f)
                    var j = 0.0
                    while (j++ < height) {
                        val loc = pl.add(0.0, 1.0, 0.0).clone()
                        task.asyncFirst<Stream<Entity>> {
                            var k = 0.0
                            while (k < 360) {
                                val rad = Math.toRadians(k)
                                val x = cos(rad) * ii
                                val z = sin(rad) * ii
                                val f = loc.clone().add(x, 0.0, z)
                                player.world.spawnParticle(Particle.EXPLOSION_NORMAL, f, 15, 0.0, 0.0, 0.0, 0.0, null)
                                k += 10
                            }
                            return@asyncFirst player.world
                                    .getNearbyEntities(loc, ii, 0.5, ii)
                                    .stream()
                                    .filter{EntityFilter.check(it)}
                                    .filter{entity -> entity != player}
                        }.syncLast {
                            it.forEach { e ->
                                (e as LivingEntity).damage(dam, player)
                            }
                        }
                    }
                    task.delay(5)
                }
                task.execute()
            }
        })
    }
}
