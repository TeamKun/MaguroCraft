package com.bun133.maguro

import com.destroystokyo.paper.Title
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

//死ぬまでのtick数
//お好みでどうぞ
const val limitTime = 30
var isStarting = false

class Maguro : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        this.server.pluginManager.registerEvents(MoveListener.instance, this)
        TickCounter(MoveListener.instance, ScoreBoardUpdater(Bukkit.getScoreboardManager().newScoreboard)).runTaskTimer(
            this,
            10,
            1
        )
        this.getCommand("maguro")!!.setExecutor(GameMaster())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

class MoveListener : Listener {
    companion object {
        val instance = MoveListener()
    }

    /**
     * This is a map shows ticks since last time player moved
     */
    var map: HashMap<Player, Int> = hashMapOf()

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (e.from.x != e.to.x || e.from.y != e.to.y || e.from.z != e.to.z){
            map[e.player] = 0
        }
    }

    @EventHandler
    fun onRespawn(e:PlayerRespawnEvent){
        map[e.player] = 0
    }
}

class TickCounter(val move: MoveListener, val scoreBoardUpdater: ScoreBoardUpdater) : BukkitRunnable() {
    override fun run() {
        if (isStarting) {
            move.map.forEach { (p, i) ->
                if (p.gameMode !== GameMode.SPECTATOR && p.gameMode !== GameMode.CREATIVE) {
                    move.map[p] = i + 1
                    if (i > limitTime) {
                        p.health = 0.0
                        p.damage(10000.0)
                        move.map[p] = 0
                    }
                    scoreBoardUpdater.update(p, i)
                }
            }
        }
    }
}

class ScoreBoardUpdater(val scoreboard: Scoreboard) {
    val OBJECTIVE_NAME = "deathtime"
    fun update(p: Player, i: Int) {
        var o: Objective? = scoreboard.getObjective(OBJECTIVE_NAME)
        if (o == null) {
            o = scoreboard.registerNewObjective(OBJECTIVE_NAME, "time", "0")
            o.displaySlot = DisplaySlot.BELOW_NAME
        }
        o.getScore(p.displayName).score = i
        p.sendActionBar("Time:${i}/${limitTime}")
    }
}

class GameMaster : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (sender.isOp)
                return onRun(sender, command, label, args)
        } else return onRun(sender, command, label, args)
        return false
    }

    fun onRun(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size == 1) {
            when (args[0]) {
                "s", "start" -> {
                    isStarting = true
                    return true
                }
                "e", "end" -> {
                    isStarting = false
                    return true
                }
            }
        } else return false
        return false
    }
}