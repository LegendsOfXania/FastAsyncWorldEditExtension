package fr.legendsofxania.fawe.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.*
import fr.legendsofxania.fawe.packets.pasteSchematicWithPacket
import fr.legendsofxania.fawe.utils.resetBlocks
import org.bukkit.entity.Player

@Entry("paste_schematic_for_audience", "Paste a schematic for players in audience.", Colors.GREEN, "fluent:apps-48-filled")
class PasteSchematicForAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("The name of your schematic. (include the extension of the file)")
    val schematic: String = "",
    @Help("Where the schematic will be placed.")
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    @Help("Will air blocks be ignored?")
    val ignoreAir: Boolean = false,
) : AudienceEntry {
    override suspend fun display(): AudienceDisplay {
        return PasteSchematicForAudienceDisplay(schematic, location, ignoreAir)
    }
}

class PasteSchematicForAudienceDisplay(
    private val schematic: String,
    private val location: Var<Position>,
    private val ignoreAir: Boolean
) : AudienceDisplay(), TickableDisplay {

    override fun onPlayerAdd(player: Player) {}

    override fun onPlayerRemove(player: Player) {
        resetBlocks(player)
    }

    override fun tick() {
        players.forEach { player ->
            pasteSchematicWithPacket(player, schematic, location, ignoreAir)
        }
    }
}
