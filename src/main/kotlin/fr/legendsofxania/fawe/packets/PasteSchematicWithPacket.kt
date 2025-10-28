package fr.legendsofxania.fawe.packets

import com.sk89q.jnbt.ByteArrayTag
import com.sk89q.jnbt.ByteTag
import com.sk89q.jnbt.CompoundTag
import com.sk89q.jnbt.DoubleTag
import com.sk89q.jnbt.FloatTag
import com.sk89q.jnbt.IntArrayTag
import com.sk89q.jnbt.IntTag
import com.sk89q.jnbt.ListTag
import com.sk89q.jnbt.LongArrayTag
import com.sk89q.jnbt.LongTag
import com.sk89q.jnbt.ShortTag
import com.sk89q.jnbt.StringTag
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.world.block.BaseBlock
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.utils.toBukkitLocation
import fr.legendsofxania.fawe.utils.modifiedBlocks
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import java.io.File

fun pasteSchematicWithPacket(player: Player, schematic: String, location: Var<Position>, ignoreAir: Boolean) {
    val bukkitLocation = location.get(player).toBukkitLocation()
    val file = File("plugins/FastAsyncWorldEdit/schematics/$schematic")
    val clipboardFormat = ClipboardFormats.findByFile(file) ?: return
    val modified = modifiedBlocks.computeIfAbsent(player.uniqueId) { mutableListOf() }

    clipboardFormat.getReader(file.inputStream()).use { reader ->
        val clipboard = reader.read()
        val origin = clipboard.origin
        val region = clipboard.region

        val craftPlayer = player as CraftPlayer
        val serverPlayer = craftPlayer.handle

        val sectionUpdates = mutableMapOf<Long, MutableList<Pair<BlockPos, BlockState>>>()
        val tileEntities = mutableListOf<Pair<BlockPos, BaseBlock>>()

        for (vector in region) {
            val baseBlock = clipboard.getFullBlock(vector)
            val material = BukkitAdapter.adapt(baseBlock.blockType)

            if (ignoreAir && material.isAir) continue

            val dx = vector.toVector3().blockX - origin.toVector3().blockX
            val dy = vector.toVector3().blockY - origin.toVector3().blockY
            val dz = vector.toVector3().blockZ - origin.toVector3().blockZ

            val target = bukkitLocation.clone().add(dx.toDouble(), dy.toDouble(), dz.toDouble())
            val chunk = target.chunk

            if (!chunk.isLoaded) continue

            if (modified.none { it == target }) {
                modified.add(target.clone())
            }

            val blockPos = BlockPos(target.blockX, target.blockY, target.blockZ)
            val nmsBlockState = convertToNMSBlockState(baseBlock)

            val sectionKey = getSectionKey(blockPos)
            sectionUpdates.computeIfAbsent(sectionKey) { mutableListOf() }.add(blockPos to nmsBlockState)

            if (baseBlock.nbt != null) {
                tileEntities.add(blockPos to baseBlock)
            }
        }

        sectionUpdates.forEach { (_, blocks) ->
            if (blocks.isNotEmpty()) {
                sendBlockUpdates(serverPlayer, blocks)
            }
        }

        tileEntities.forEach { (pos, baseBlock) ->
            sendTileEntityUpdate(serverPlayer, pos, baseBlock)
        }
    }
}

private fun convertToNMSBlockState(baseBlock: BaseBlock): BlockState {
    val bukkitBlockData = BukkitAdapter.adapt(baseBlock)
    return (bukkitBlockData as CraftBlockData).state
}

private fun getSectionKey(pos: BlockPos): Long {
    val sectionX = pos.x shr 4
    val sectionY = pos.y shr 4
    val sectionZ = pos.z shr 4
    return (sectionX.toLong() shl 32) or (sectionY.toLong() shl 16) or sectionZ.toLong()
}

private fun sendBlockUpdates(
    serverPlayer: ServerPlayer,
    blocks: List<Pair<BlockPos, BlockState>>
) {
    blocks.forEach { (pos, state) ->
        val packet = ClientboundBlockUpdatePacket(pos, state)
        serverPlayer.connection.send(packet)
    }
}

private fun sendTileEntityUpdate(
    serverPlayer: ServerPlayer,
    pos: BlockPos,
    baseBlock: BaseBlock
) {
    val blockState = convertToNMSBlockState(baseBlock)

    serverPlayer.connection.send(ClientboundBlockUpdatePacket(pos, blockState))

    if (baseBlock.nbt != null) {
        val nbtData = convertWorldEditNBTToMinecraft(baseBlock.nbtData!!)

        nbtData.putString("id", blockState.block.builtInRegistryHolder().key().location().toString())
        nbtData.putInt("x", pos.x)
        nbtData.putInt("y", pos.y)
        nbtData.putInt("z", pos.z)

        val blockEntityType = getBlockEntityType(blockState.block)
        if (blockEntityType != null) {
            val packet = ClientboundBlockEntityDataPacket(pos, blockEntityType, nbtData)
            serverPlayer.connection.send(packet)
        }
    }
}

private fun getBlockEntityType(block: Block): BlockEntityType<*>? {
    return when (block) {
        is EntityBlock -> block.newBlockEntity(BlockPos.ZERO, block.defaultBlockState())?.type
        else -> null
    }
}

private fun convertWorldEditNBTToMinecraft(worldEditNBT: CompoundTag): net.minecraft.nbt.CompoundTag {
    val minecraftNBT = net.minecraft.nbt.CompoundTag()

    worldEditNBT.value.forEach { (key, value) ->
            when (value) {
                is StringTag -> minecraftNBT.putString(key, value.value)
                is IntTag -> minecraftNBT.putInt(key, value.value)
                is DoubleTag -> minecraftNBT.putDouble(key, value.value)
                is FloatTag -> minecraftNBT.putFloat(key, value.value)
                is LongTag -> minecraftNBT.putLong(key, value.value)
                is ShortTag -> minecraftNBT.putShort(key, value.value)
                is ByteTag -> minecraftNBT.putByte(key, value.value)
                is ByteArrayTag -> minecraftNBT.putByteArray(key, value.value)
                is IntArrayTag -> minecraftNBT.putIntArray(key, value.value)
                is LongArrayTag -> minecraftNBT.putLongArray(key, value.value)
                is CompoundTag -> minecraftNBT.put(key, convertWorldEditNBTToMinecraft(value))
                is ListTag<*, *> -> {
                    val listNBT = net.minecraft.nbt.ListTag()
                    val listValue = value.value as List<*>
                    for (listItem in listValue) {
                        when (listItem) {
                            is CompoundTag -> listNBT.add(convertWorldEditNBTToMinecraft(listItem))
                            is StringTag -> listNBT.add(net.minecraft.nbt.StringTag.valueOf(listItem.value))
                            is IntTag -> listNBT.add(net.minecraft.nbt.IntTag.valueOf(listItem.value))
                            is DoubleTag -> listNBT.add(net.minecraft.nbt.DoubleTag.valueOf(listItem.value))
                            is FloatTag -> listNBT.add(net.minecraft.nbt.FloatTag.valueOf(listItem.value))
                            is LongTag -> listNBT.add(net.minecraft.nbt.LongTag.valueOf(listItem.value))
                            is ShortTag -> listNBT.add(net.minecraft.nbt.ShortTag.valueOf(listItem.value))
                            is ByteTag -> listNBT.add(net.minecraft.nbt.ByteTag.valueOf(listItem.value))
                        }
                    }
                    minecraftNBT.put(key, listNBT)
                }
            }
    }
    return minecraftNBT
}