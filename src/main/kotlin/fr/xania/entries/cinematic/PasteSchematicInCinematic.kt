package fr.xania.entries.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.*
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.entries.*
import fr.xania.packets.pasteSchematicWithPacket
import fr.xania.utils.resetBlocks
import org.bukkit.entity.Player

@Entry("paste_schematic_in_cinematic", "Paste a schematic for players in cinematic", Colors.BLUE, "fluent:apps-48-filled")
class PasteSchematicInCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Segments(Colors.BLUE, "fluent:apps-48-filled")
    val segments: List<PasteSchematicInCinematicSegment> = emptyList(),
    @Help("The name of your schematic. (include the extension of the file)")
    val schematic: String = "",
    @Help("Where the schematic will be placed.")
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    @Help("Will air blocks be ignored?")
    val ignoreAir: Boolean = false,
) : CinematicEntry {
    override fun create(player: Player): CinematicAction {
        return PasteSchematicInCinematicAction(player, this, schematic, location, ignoreAir)
    }
}

data class PasteSchematicInCinematicSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
) : Segment

class PasteSchematicInCinematicAction(
    private val player: Player,
    private val entry: PasteSchematicInCinematicEntry,
    private val schematic: String,
    private val location: Var<Position>,
    private val ignoreAir: Boolean
) : CinematicAction {
    override suspend fun setup() {}

    override suspend fun tick(frame: Int) {
         entry.segments.forEach { segment ->
                if (frame >= segment.startFrame && frame <= segment.endFrame) {
                    pasteSchematicWithPacket(player, schematic, location, ignoreAir)
                } else if (frame == segment.endFrame + 1) {
                    resetBlocks(player)
                }
        }
    }

    override suspend fun teardown() {}

    override fun canFinish(frame: Int): Boolean = entry.segments canFinishAt frame
}