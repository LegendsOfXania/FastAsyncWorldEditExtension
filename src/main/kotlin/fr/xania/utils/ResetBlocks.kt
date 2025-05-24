package fr.xania.utils

import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

val modifiedBlocks = mutableMapOf<UUID, MutableList<Location>>()

fun resetBlocks(player: Player) {
    val modified = modifiedBlocks[player.uniqueId]
    if (modified == null) {
        return
    }
    for (loc in modified) {
        val realBlock = loc.block
        player.sendBlockChange(loc, realBlock.blockData)
    }
    modifiedBlocks.remove(player.uniqueId)
}