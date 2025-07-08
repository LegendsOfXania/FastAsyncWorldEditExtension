package fr.xania.packets

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.world.block.BaseBlock
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.utils.toBukkitLocation
import fr.xania.utils.modifiedBlocks
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.craftbukkit.CraftWorld
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

        val serverLevel = (bukkitLocation.world as CraftWorld).handle
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

        //Dispatchers.Sync.launch {
            tileEntities.forEach { (pos, baseBlock) ->
                sendTileEntityUpdate(serverPlayer, serverLevel, pos, baseBlock)
            }
        //}
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
    serverPlayer: net.minecraft.server.level.ServerPlayer,
    blocks: List<Pair<BlockPos, BlockState>>
) {
    blocks.forEach { (pos, state) ->
        try {
            val packet = ClientboundBlockUpdatePacket(pos, state)
            serverPlayer.connection.send(packet)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun sendTileEntityUpdate(
    serverPlayer: net.minecraft.server.level.ServerPlayer,
    serverLevel: ServerLevel,
    pos: BlockPos,
    baseBlock: BaseBlock
) {
    try {
        val blockState = convertToNMSBlockState(baseBlock)
        serverLevel.setBlock(pos, blockState, 3)

        val blockEntity = serverLevel.getBlockEntity(pos)
        if (blockEntity != null && baseBlock.nbt != null) {
            val nbtData = convertWorldEditNBTToMinecraft(baseBlock.nbtData!!)

            nbtData.putString("id", blockEntity.type.toString())
            nbtData.putInt("x", pos.x)
            nbtData.putInt("y", pos.y)
            nbtData.putInt("z", pos.z)

            try {
                blockEntity.loadWithComponents(nbtData, serverLevel.registryAccess())
            } catch (e: Exception) {
                try {
                    blockEntity.loadCustomOnly(nbtData, serverLevel.registryAccess())
                } catch (e2: Exception) {
                    val loadMethod = blockEntity.javaClass.methods.find {
                        it.name == "load" && it.parameterCount == 2
                    }
                    loadMethod?.invoke(blockEntity, nbtData, serverLevel.registryAccess())
                }
            }

            blockEntity.setChanged()

            ClientboundBlockEntityDataPacket.create(blockEntity)?.let { packet ->
                serverPlayer.connection.send(packet)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}



private fun convertWorldEditNBTToMinecraft(worldEditNBT: com.sk89q.jnbt.CompoundTag): net.minecraft.nbt.CompoundTag {
    val minecraftNBT = net.minecraft.nbt.CompoundTag()

    worldEditNBT.value.forEach { (key, value) ->
        try {
            when (value) {
                is com.sk89q.jnbt.StringTag -> minecraftNBT.putString(key, value.value)
                is com.sk89q.jnbt.IntTag -> minecraftNBT.putInt(key, value.value)
                is com.sk89q.jnbt.DoubleTag -> minecraftNBT.putDouble(key, value.value)
                is com.sk89q.jnbt.FloatTag -> minecraftNBT.putFloat(key, value.value)
                is com.sk89q.jnbt.LongTag -> minecraftNBT.putLong(key, value.value)
                is com.sk89q.jnbt.ShortTag -> minecraftNBT.putShort(key, value.value)
                is com.sk89q.jnbt.ByteTag -> minecraftNBT.putByte(key, value.value)
                is com.sk89q.jnbt.ByteArrayTag -> minecraftNBT.putByteArray(key, value.value)
                is com.sk89q.jnbt.IntArrayTag -> minecraftNBT.putIntArray(key, value.value)
                is com.sk89q.jnbt.LongArrayTag -> minecraftNBT.putLongArray(key, value.value)
                is com.sk89q.jnbt.CompoundTag -> minecraftNBT.put(key, convertWorldEditNBTToMinecraft(value))
                is com.sk89q.jnbt.ListTag<*, *> -> {
                    val listNBT = net.minecraft.nbt.ListTag()
                    val listValue = value.value as List<*>
                    for (listItem in listValue) {
                        when (listItem) {
                            is com.sk89q.jnbt.CompoundTag -> listNBT.add(convertWorldEditNBTToMinecraft(listItem))
                            is com.sk89q.jnbt.StringTag -> listNBT.add(net.minecraft.nbt.StringTag.valueOf(listItem.value))
                            is com.sk89q.jnbt.IntTag -> listNBT.add(net.minecraft.nbt.IntTag.valueOf(listItem.value))
                            is com.sk89q.jnbt.DoubleTag -> listNBT.add(net.minecraft.nbt.DoubleTag.valueOf(listItem.value))
                            is com.sk89q.jnbt.FloatTag -> listNBT.add(net.minecraft.nbt.FloatTag.valueOf(listItem.value))
                            is com.sk89q.jnbt.LongTag -> listNBT.add(net.minecraft.nbt.LongTag.valueOf(listItem.value))
                            is com.sk89q.jnbt.ShortTag -> listNBT.add(net.minecraft.nbt.ShortTag.valueOf(listItem.value))
                            is com.sk89q.jnbt.ByteTag -> listNBT.add(net.minecraft.nbt.ByteTag.valueOf(listItem.value))
                        }
                    }
                    minecraftNBT.put(key, listNBT)
                }
            }
        } catch (e: Exception) { }
    }
    return minecraftNBT
}