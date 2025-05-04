package fr.xania.entries.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.logger
import net.sandrohc.schematic4j.SchematicLoader
import org.bukkit.entity.Player
import java.io.File

@Entry("paste_schematic", "Paste a schematic at a specific place for all players.", Colors.RED, "material-symbols:touch-app-rounded")
class PasteSchematicActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),

    @Help("The name of your schematic. (include the extension of the file)")
    val schematic: String = "",

    @Help("Where the schematic will be placed.")
    val location: Var<Position> = ConstVar(Position.ORIGIN),
) : ActionEntry {
    override fun ActionTrigger.execute() {
        pasteSchematic(schematic, location, player)
    }
}

private fun pasteSchematic(schematic: String, location: Var<Position>, player: Player) {
    val path = "plugins/FastAsyncWorldEdit/schematics/$schematic"
    val file = File(path)
    if (!file.exists()) {
        logger.severe("Schematic $schematic not foud at : $path")
    }
    val schem = SchematicLoader.load(path)
    logger.info("Loaded ${schem.name()} : ${schem.width()}x${schem.height()}x${schem.length()}")
}


