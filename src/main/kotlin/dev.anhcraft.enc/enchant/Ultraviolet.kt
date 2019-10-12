package dev.anhcraft.enc.enchant

import dev.anhcraft.craftkit.cb_common.BoundingBox
import dev.anhcraft.craftkit.utils.EntityUtil
import dev.anhcraft.enc.ENC
import dev.anhcraft.enc.api.Enchantment
import dev.anhcraft.enc.api.ItemReport
import dev.anhcraft.enc.api.handlers.InteractHandler
import dev.anhcraft.enc.utils.Cooldown
import dev.anhcraft.enc.utils.PlayerMap
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.EnchantmentTarget
import org.bukkit.entity.LivingEntity
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.concurrent.ThreadLocalRandom

class Ultraviolet : Enchantment(
        "Ultraviolet",
        arrayOf("Summon ultraviolet at your location"),
        "anhcraft",
        null,
        5,
        EnchantmentTarget.ALL
) {
    private val MAP = PlayerMap<Cooldown>()

    override fun onInitConfig(){
        val map = HashMap<String, Any>()
        map["amount"] = "{level}*2+12"
        map["damage"] = "{level}*1.2+0.45"
        map["cooldown"] = "{level}*30+50"
        map["rand_offset_x"] = "7.5"
        map["rand_offset_z"] = "7.5"
        initConfigEntries(map)
    }

    init {
        enchantHandlers.add(object : InteractHandler() {
            @Override
            override fun onInteract(report: ItemReport, event: PlayerInteractEvent) {
                val player = report.player
                if (EquipmentSlot.OFF_HAND == event.hand || event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR)
                    return

                val cooldown = computeConfigValue("cooldown", report)
                if (!handleCooldown(MAP, player, cooldown)) return

                val amount = computeConfigValue("amount", report).toInt()
                val dam = computeConfigValue("damage", report)
                val rox = computeConfigValue("rand_offset_x", report)
                val roz = computeConfigValue("rand_offset_z", report)
                val origin = player.location
                val playerY = origin.y
                origin.y = player.world.maxHeight.toDouble()
                val playerVec = origin.toVector()

                val entityMap = HashMap<LivingEntity, BoundingBox>()
                for (x in event.player.world.getEntitiesByClasses(LivingEntity::class.java)) {
                    entityMap[x as LivingEntity] = EntityUtil.getBoundingBox(x).expand(1.0, 1.0, 1.0)
                }

                var i = 0
                while (i++ < amount) {
                    val x = origin.x + ThreadLocalRandom.current().nextDouble(-rox, rox)
                    val z = origin.z + ThreadLocalRandom.current().nextDouble(-roz, roz)
                    val target = Location(origin.world, x, playerY, z)
                    val dir = target.toVector().subtract(playerVec).normalize()

                    var n = 0
                    while(n++ < 255){
                        val location = origin.clone().add(dir.clone().multiply(n))
                        if(location.block.type.isSolid){
                            player.world.spawnParticle(Particle.SMOKE_NORMAL, location, 15, 0.0, 0.0, 0.0, 0.0, null)
                            player.world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 3.0f, 1.0f)
                           break
                        }
                        ENC.getEffectManager().display(Particle.REDSTONE, location, 0f, 0f, 0f, 0f, 5, 3f, Color.PURPLE, null, 0.toByte(), 100.0, null)
                        entityMap.forEach { (t, u) ->
                            if(t != player && u.contains(location)) {
                                t.damage(dam, player)
                            }
                        }
                    }
                }
            }
        })
    }
}
