package me.cunzai.plugin.challenge.impl

import me.cunzai.plugin.BigChallenge
import me.cunzai.plugin.challenge.Challenge
import me.cunzai.plugin.game.Game
import org.bukkit.entity.Fireball
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.random.Random
import kotlin.random.nextInt


/*
 * @ Created with IntelliJ IDEA
 * @ Author EmptyIrony
 * @ Date 2021/8/24
 * @ Time 13:01
 */
class FireBall: Challenge{
    private val runnable: BukkitRunnable by lazy {
        object :BukkitRunnable() {
            override fun run() {
                for (index in 0 until 10) {
                    val add = Game.currentLocation.clone().add(
                        (Random.nextInt(Game.size * 2) - Game.size).toDouble(),
                        Game.currentLocation.y + 5.0,
                        (Random.nextInt(Game.size * 2) - Game.size).toDouble()
                    )

                    val fireball = add.world!!.spawn(add, Fireball::class.java)
                    fireball.velocity = Vector(0.0, -1.0,0.0)
                }
            }
        }
    }

    override fun getChallengeName(): String {
        return "烈焰骤雨"
    }

    override fun getDescription(): String {
        return "在天空中向下发射可爆炸的火焰弹"
    }

    override fun onStart() {
        runnable.runTaskTimer(BigChallenge.plugin, 20L, 20L)
    }

    override fun onEnd() {
        runnable.cancel()
    }
}