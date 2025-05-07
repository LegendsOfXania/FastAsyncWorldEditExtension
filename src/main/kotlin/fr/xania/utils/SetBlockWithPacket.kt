package fr.xania.utils

import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.utils.toBukkitLocation
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

fun setBlockWithPacket(player: Player, material: Var<Material>, location: Var<Position>) {
    val bukkitLocation = location.get(player).toBukkitLocation()
    val block = material.get(player)
    val blockData = Bukkit.createBlockData(block)

    player.sendBlockChange(bukkitLocation, blockData)

}