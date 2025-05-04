package fr.xania.entries.action

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
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
import com.typewritermc.engine.paper.utils.ThreadType
import com.typewritermc.engine.paper.utils.toBukkitLocation
import org.bukkit.Bukkit
import java.io.File

@Entry("place_schematic", "Place a schematic at a specific place.", Colors.RED, "material-symbols:touch-app-rounded")
class PlaceSchematicActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("The name of your schematic. (include the extension of the file)")
    val schematic: String = "",
    @Help("Where the schematic will be placed.")
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    @Help("Should -a be used?")
    val noAir: Boolean = false,
) : ActionEntry {
    override fun ActionTrigger.execute() {
        ThreadType.SYNC.launch {
            val positionValue = location.get(player, context)
            val bukkitLocation = positionValue.toBukkitLocation()

            val schematicFile = File(Bukkit.getWorldContainer(), "plugins/FastAsyncWorldEdit/schematics/$schematic")
            if (!schematicFile.exists()) {
                logger.severe("Schematic file not found: $schematic")
                return@launch
            }

            val format = ClipboardFormats.findByFile(schematicFile)
            if (format == null) {
                logger.severe("Could not determine format for schematic: $schematic")
                return@launch
            }

            try {
                format.getReader(schematicFile.inputStream()).use { reader ->
                    val clipboard = reader.read()

                    val weWorld = BukkitAdapter.adapt(bukkitLocation.world)
                    val editSession = WorldEdit.getInstance().newEditSession(weWorld)

                    val blockVector = BlockVector3.at(
                        bukkitLocation.blockX,
                        bukkitLocation.blockY,
                        bukkitLocation.blockZ
                    )

                    val operation = ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(blockVector)
                        .ignoreAirBlocks(noAir)
                        .build()

                    Operations.complete(operation)
                    editSession.close()
                }
            } catch (ex: Exception) {
                logger.severe("Error while pasting schematic '$schematic': ${ex.message}")
                ex.printStackTrace()
            }
        }
    }
}