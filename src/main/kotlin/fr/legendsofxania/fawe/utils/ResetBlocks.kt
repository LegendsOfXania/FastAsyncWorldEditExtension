package fr.legendsofxania.fawe.utils

import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import java.util.*

val modifiedBlocks = mutableMapOf<UUID, MutableList<Location>>()

fun resetBlocks(player: Player) {
    val modified = modifiedBlocks[player.uniqueId] ?: return

    val blockChanges = mutableMapOf<Location, BlockData>()
    for (loc in modified) {
        val realBlock = loc.block
        blockChanges[loc] = realBlock.blockData
    }

    if (blockChanges.isNotEmpty()) {
        player.sendMultiBlockChange(blockChanges)
    }

    modifiedBlocks.remove(player.uniqueId)
}