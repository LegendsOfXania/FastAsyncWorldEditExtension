package fr.xania.entries.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.*
import fr.xania.packets.pasteSchematicWithPacket
import fr.xania.utils.resetBlocks
import org.bukkit.entity.Player

@Entry("paste_schematic_for_audience", "Paste a schematic for players in audience.", Colors.GREEN, "fluent:apps-48-filled")
class PasteSchematicForAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    @Help("The name of your schematic. (include the extension of the file)")
    val schematic: String = "",
    @Help("Where the schematic will be placed.")
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    @Help("Does typewriter need to use -a?")
    val noAir: Boolean = false,
) : AudienceEntry {
    override suspend fun display(): AudienceDisplay {
        return PasteSchematicForAudienceDisplay(schematic, location, noAir)
    }
}

class PasteSchematicForAudienceDisplay(
    private val schematic: String,
    private val location: Var<Position>,
    private val noAir: Boolean
) : AudienceDisplay(), TickableDisplay {

    override fun onPlayerAdd(player: Player) {}

    override fun onPlayerRemove(player: Player) {
        resetBlocks(player)
    }

    override fun tick() {
        players.forEach { player ->
            pasteSchematicWithPacket(player, schematic, location, noAir)
        }
    }
}
