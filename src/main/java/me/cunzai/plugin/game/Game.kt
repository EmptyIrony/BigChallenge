package me.cunzai.plugin.game

import io.izzel.taboolib.kotlin.colored
import io.izzel.taboolib.module.inject.TListener
import me.cunzai.plugin.BigChallenge
import me.cunzai.plugin.challenge.Challenge
import me.cunzai.plugin.challenge.impl.DeadRising
import me.cunzai.plugin.challenge.impl.FireBall
import me.cunzai.plugin.challenge.impl.Wither
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.math.abs
import kotlin.random.Random


/*
 * @ Created with IntelliJ IDEA
 * @ Author EmptyIrony
 * @ Date 2021/8/23
 * @ Time 17:10
 */
@TListener
object Game: Listener{
    val size = 10
    val score = HashMap<UUID, Int>()
    val usedLocations = ArrayList<Location>()
    val alivePlayers = ArrayList<Player>()

    var preStartTimer = 0
    var round = 0
    var state = GameState.WAITING

    lateinit var currentChallenge: Challenge
    lateinit var currentLocation: Location

    lateinit var challenges: MutableList<Challenge>

    init {
        startGame()
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (state == GameState.GAMING) {
            return
        }
        event.isCancelled = true
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (state == GameState.GAMING) {
            if (event.damager is Player) {
                event.damage = 0.0
            }
            return
        }
        event.isCancelled = true
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.entity
        player.health = player.maxHealth
        player.gameMode = GameMode.SPECTATOR

        alivePlayers.remove(player)

        if (alivePlayers.size <= 1) {
            currentChallenge.onEnd()

            state = GameState.ROUND_END
            if (alivePlayers.isNotEmpty()) {
                val winner = alivePlayers[0]
                Bukkit.broadcastMessage("&e胜利者: &b${winner.name}")
            }

            Bukkit.getOnlinePlayers().forEach {
                it.gameMode = GameMode.SPECTATOR
                it.saturation = 20F
                it.foodLevel = 20
                it.health = it.maxHealth
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        alivePlayers.remove(event.player)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.gameMode = GameMode.SPECTATOR
    }

    fun startGame() {
        challenges = arrayListOf(
            DeadRising(),
            FireBall(),
            Wither()
        )
    }

    fun startRound() {
        if (state != GameState.WAITING && state != GameState.ROUND_END) {
            return
        }
        if (challenges.size == 0) {
            Bukkit.broadcastMessage("没有更多可以轮换的挑战了")
            state = GameState.END
            return
        }

        state = GameState.STARTING

        val world = Bukkit.getWorlds()[0]
        val location = Location(world, Random.nextInt(100000).toDouble(), 0.0, Random.nextInt(100000).toDouble())
        location.y = world.getHighestBlockYAt(location).toDouble()
        val match = usedLocations.any {
            it.distance(location) <= 64
        }
        //don't use nearby location
        if (match) {
            this.startRound()
            return
        }

        usedLocations.add(location)
        this.currentLocation = location

        Bukkit.broadcastMessage("正在加载区块...请稍等...")
        val chunk = location.chunk
        if (!chunk.isLoaded) {
            chunk.load()

            val biome = location.world!!.getBiome(location.blockX, location.blockY, location.blockZ)
            if (biome.name.contains("ocean", true)) {
                startRound()
                Bukkit.broadcastMessage("区块不合适")
                return
            }
        }

        world.worldBorder.center = location
        world.worldBorder.warningDistance = 0
        world.worldBorder.size = size * 2.0
        world.worldBorder.damageBuffer = 0.0
        world.worldBorder.damageAmount = 5.0


        for (xOffset in -64..64) {
            for (zOffset in -64..64) {
                if (abs(xOffset) <= size && abs(zOffset) <= size) continue

                this.clearBlocks(location.clone().add(xOffset.toDouble(), 0.0, zOffset.toDouble()))
                this.clearBlocks(location.clone().add(-xOffset.toDouble(), 0.0, -zOffset.toDouble()))
                this.clearBlocks(location.clone().add(-xOffset.toDouble(), 0.0, zOffset.toDouble()))
                this.clearBlocks(location.clone().add(xOffset.toDouble(), 0.0, -zOffset.toDouble()))
            }
        }

        Bukkit.broadcastMessage("加载完成!")

        for (player in Bukkit.getOnlinePlayers()) {
            alivePlayers.add(player)
            player.gameMode = GameMode.SURVIVAL
            player.teleport(location)
        }

        state = GameState.PRE_START
        preStartTimer = 10
        this.updateExpAndLevel()

        var ticks = 0
        object :BukkitRunnable() {
            override fun run() {
                if (preStartTimer <= 0) {
                    state = GameState.GAMING
                    cancel()
                    preStartTimer = 0
                    updateExpAndLevel()

                    Bukkit.getOnlinePlayers().forEach {
                        it.sendTitle("&4游戏开始!!".colored(),null , 0, 20 * 2, 0)
                        it.playSound(it.location, Sound.ENTITY_PLAYER_LEVELUP, 1.5F, 1.5F)
                    }

                    currentChallenge = currentChallenge.javaClass.newInstance()
                    currentChallenge.onStart()
                    return
                }
                if (preStartTimer <= 5) {
                    if (preStartTimer <= 2) {
                        if (ticks % 4 == 0) {
                            refreshChallenge()
                        }
                    } else {
                        refreshChallenge()
                    }
                }
                if (ticks % 4 == 0) {
                    preStartTimer--
                    updateExpAndLevel()
                }
                ticks++
            }
        }.runTaskTimer(BigChallenge.plugin, 5L, 5L)
    }

    private fun refreshChallenge() {
        currentChallenge = challenges.get(Random.nextInt(challenges.size))
        Bukkit.getOnlinePlayers().forEach {
            it.sendTitle(currentChallenge.getChallengeName().colored(), currentChallenge.getDescription().colored(), 0, 20 * 2, 0)
            it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.5F, 1.5F)
        }
    }

    private fun clearBlocks(location: Location) {
        if (!location.chunk.isLoaded) {
            location.chunk.load()
        }
        for (index in 0..256) {
            val add = location.clone().add(0.0, index.toDouble(), 0.0)
            if (add.block.type != Material.AIR) {
                add.block.type = Material.AIR
            }
        }
    }

    private fun updateExpAndLevel() {
        for (player in Bukkit.getOnlinePlayers()) {
            player.level = maxOf(0, preStartTimer)
            player.exp = preStartTimer.toFloat() / 10F
        }
    }



    enum class GameState {
        WAITING,
        STARTING,
        PRE_START,
        GAMING,
        ROUND_END,
        END
    }

}