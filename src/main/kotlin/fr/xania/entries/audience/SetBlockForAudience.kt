package fr.xania.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.MaterialProperties
import com.typewritermc.core.extension.annotations.MaterialProperty
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.utils.toBukkitLocation
import fr.xania.utils.setBlockWithPacket
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

@Entry("set_block_for_audience", "Set a block for players in audience.", Colors.GREEN, "material-symbols:chat-rounded")
class SetBlockForAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    @MaterialProperties(MaterialProperty.BLOCK)
    val material: Var<Material> = ConstVar(Material.AIR),
    private val location: Var<Position> = ConstVar(Position.ORIGIN),
) : AudienceEntry {
    override suspend fun display(): AudienceDisplay {
        return SetBlockForAudienceDisplay(material, location)
    }
}

class SetBlockForAudienceDisplay(
    private val material: Var<Material>,
    private val location: Var<Position>,
) : AudienceDisplay(), TickableDisplay {

    private var originalBlockData: BlockData? = null
    private var originalWorld: World? = null

    override fun onPlayerAdd(player: Player) {
        if (originalBlockData == null) {
            val loc = location.get(player).toBukkitLocation()
            val block = loc.block
            originalBlockData = block.blockData.clone()
            originalWorld = loc.world
        }
        setBlockWithPacket(player, material, location)
    }

    override fun onPlayerRemove(player: Player) {
            val loc = location.get(player).toBukkitLocation()
            if (originalBlockData != null && originalWorld != null && loc.world == originalWorld) {
                loc.block.blockData = originalBlockData!!
        }
    }

    override fun tick() {
        players.forEach { player ->
            setBlockWithPacket(player, material, location)
        }
    }
}