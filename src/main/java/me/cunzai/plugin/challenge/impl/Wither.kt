package me.cunzai.plugin.challenge.impl

import me.cunzai.plugin.challenge.Challenge
import me.cunzai.plugin.game.Game
import org.bukkit.entity.EntityType
import org.bukkit.entity.Wither


/*
 * @ Created with IntelliJ IDEA
 * @ Author EmptyIrony
 * @ Date 2021/8/24
 * @ Time 12:59
 */
class Wither: Challenge{
    lateinit var wither: Wither

    override fun getChallengeName(): String {
        return "凋灵盛宴"
    }

    override fun getDescription(): String {
        return "在地图中刷新凋零"
    }

    override fun onStart() {
        wither = Game.currentLocation.world!!.spawnEntity(Game.currentLocation, EntityType.WITHER) as Wither
    }

    override fun onEnd() {
        if (this::wither.isInitialized) {
            wither.remove()
        }
    }
}