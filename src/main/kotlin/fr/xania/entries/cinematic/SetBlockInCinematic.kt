package fr.xania.entries.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.MaterialProperties
import com.typewritermc.core.extension.annotations.MaterialProperty
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.entries.*
import fr.xania.utils.resetBlocks
import fr.xania.packets.setBlockWithPacket
import org.bukkit.Material
import org.bukkit.entity.Player

@Entry("set_block_in_cinematic", "Set a block for players in cinematic", Colors.BLUE, "fluent:cube-add-20-filled")
class SetBlockInCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Segments(Colors.BLUE, "fluent:cube-add-20-filled")
    val segments: List<SetBlockInCinematicSegment> = emptyList(),
    @MaterialProperties(MaterialProperty.BLOCK)
    val material: Var<Material> = ConstVar(Material.AIR),
    private val location: Var<Position> = ConstVar(Position.ORIGIN),
) : CinematicEntry {
    override fun create(player: Player): CinematicAction {
        return SetBlockInCinematicAction(player, this, material, location)
    }
}

data class SetBlockInCinematicSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
) : Segment

class SetBlockInCinematicAction(
    private val player: Player,
    private val entry: SetBlockInCinematicEntry,
    private val material: Var<Material>,
    private val location: Var<Position>,
) : CinematicAction {

    override suspend fun setup() {}

    override suspend fun tick(frame: Int) {
        entry.segments.forEach { segment ->
            if (frame >= segment.startFrame && frame <= segment.endFrame) {
                setBlockWithPacket(player, material, location)
            } else if (frame == segment.endFrame + 1) {
                resetBlocks(player)
            }
        }
    }

    override suspend fun teardown() {}

    override fun canFinish(frame: Int): Boolean = entry.segments canFinishAt frame
}