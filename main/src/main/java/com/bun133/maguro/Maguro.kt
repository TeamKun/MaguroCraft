package com.bun133.maguro

import com.destroystokyo.paper.Title
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

const val limitTime = 20
var isStarting = false

class Maguro : JavaPlugin() {
    override fun onEnable() {
        // Plugin startup logic
        this.server.pluginManager.registerEvents(MoveListener.instance, this)
        TickCounter(MoveListener.instance, ScoreBoardUpdater(Bukkit.getScoreboardManager().newScoreboard)).runTaskTimer(this, 10, 1)
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
        map[e.player] = 0
    }
}

class TickCounter(val move: MoveListener,val scoreBoardUpdater: ScoreBoardUpdater) : BukkitRunnable() {
    override fun run() {
        if(isStarting){
            move.map.forEach { (p, i) ->
                if (i > limitTime) {
                    p.damage(Double.MAX_VALUE)
                }else scoreBoardUpdater.update(p,i)
            }
        }
    }
}

class ScoreBoardUpdater(val scoreboard: Scoreboard){
    val OBJECTIVE_NAME = "deathtime"
    fun update(p:Player,i:Int){
        var o : Objective? = scoreboard.getObjective(OBJECTIVE_NAME)
        if(o==null){
            o = scoreboard.registerNewObjective(OBJECTIVE_NAME, "time","0")
            o.displaySlot = DisplaySlot.BELOW_NAME
        }
        o.getScore(p.displayName).score = i

        p.sendTitle(Title("Time:${i}"))
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
        if(args.size == 1){
            when(args[0]){
                "s","start" -> {
                    isStarting = true
                }
                "e","end" -> {
                    isStarting = false
                }
            }
        }else return false
        return false
    }
}