package me.cunzai.plugin.challenge.impl

import me.cunzai.plugin.BigChallenge
import me.cunzai.plugin.challenge.Challenge
import me.cunzai.plugin.game.Game
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random
import kotlin.random.nextInt


/*
 * @ Created with IntelliJ IDEA
 * @ Author EmptyIrony
 * @ Date 2021/8/23
 * @ Time 17:43
 */
class DeadRising: Challenge{
    private val monsters = ArrayList<LivingEntity>()
    private val runnable: BukkitRunnable by lazy {
        object :BukkitRunnable() {
            override fun run() {
                val clone = Game.currentLocation.clone()
                for (index in 0 until 10) {
                    val loc = clone.add(Random.nextInt(Game.size).toDouble(), 0.0, Random.nextInt(Game.size).toDouble())
                    monsters.add(clone.world!!.spawnEntity(loc, getRandomMonster()) as LivingEntity)
                }
                for (player in Game.alivePlayers) {
                    val loc = player.location.clone().add(Random.nextInt(2) - 1.0, 0.0, Random.nextInt(2) - 1.0)
                    monsters.add(clone.world!!.spawnEntity(loc, getRandomMonster()) as LivingEntity)
                }
            }
        }
    }

    private fun getRandomMonster(): EntityType {
        val i = Random.nextInt(10)
        if (i <= 2) {
            return EntityType.ZOMBIE
        } else if (i <= 4) {
            return EntityType.ZOMBIE_VILLAGER
        } else if (i <= 7) {
            return EntityType.SPIDER
        } else {
            return EntityType.SKELETON
        }
    }

    override fun getChallengeName(): String {
        return "&4丧尸围城"
    }

    override fun getDescription(): String {
        return "&c地图将随机刷新怪物"
    }


    override fun onStart() {
        runnable.runTaskTimer(BigChallenge.plugin, 20L, 20L)
        Game.currentLocation.world!!.time = 13000
    }

    override fun onEnd() {
        runnable.cancel()
        for (monster in monsters) {
            monster.remove()
        }
        Game.currentLocation.world!!.time = 1000
    }


}