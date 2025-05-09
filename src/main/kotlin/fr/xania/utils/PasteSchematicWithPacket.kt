package fr.xania.utils

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.utils.toBukkitLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import java.io.File

fun pasteSchematicWithPacket(player: Player, schematic: String, location: Var<Position>, noAir: Boolean) {
    val file = File("plugins/FastAsyncWorldEdit/schematics/$schematic")
    val bukkitLocation = location.get(player).toBukkitLocation()
    val clipboardFormat = ClipboardFormats.findByFile(file) ?: return

    val modified = modifiedBlocks.computeIfAbsent(player.uniqueId) { mutableListOf() }

    clipboardFormat.getReader(file.inputStream()).use { reader ->
        val clipboard = reader.read()
        val origin = clipboard.origin
        val region = clipboard.region

        val blockChanges = mutableMapOf<Location, BlockData>()

        for (vector in region) {
            val blockState = clipboard.getFullBlock(vector)
            val material = BukkitAdapter.adapt(blockState.blockType)
            if (noAir && material.isAir) continue

            val dx = vector.toVector3().blockX - origin.toVector3().blockX
            val dy = vector.toVector3().blockY - origin.toVector3().blockY
            val dz = vector.toVector3().blockZ - origin.toVector3().blockZ

            val target = bukkitLocation.clone().add(dx.toDouble(), dy.toDouble(), dz.toDouble())

            if (modified.none { it == target }) {
                modified.add(target.clone())
            }

            val blockData = Bukkit.createBlockData(material)
            blockChanges[target] = blockData
        }

        if (blockChanges.isNotEmpty()) {
            player.sendMultiBlockChange(blockChanges)
        }
    }
}