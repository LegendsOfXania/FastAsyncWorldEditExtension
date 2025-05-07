package fr.xania.entries.action

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operation
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
import com.typewritermc.engine.paper.utils.toBukkitLocation
import java.io.File
import java.io.FileInputStream


@Entry("paste_schematic", "Paste a schematic at a specific place for all players.", Colors.RED, "fluent:apps-48-filled")
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
    @Help("Does typewriter need to use -a?")
    val noAir: Boolean = false,
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val file = File("plugins/FastAsyncWorldEdit/schematics/$schematic")
        val bukkitLocation = location.get(player).toBukkitLocation()
        val clipboard: Clipboard
        val weWorld = BukkitAdapter.adapt(bukkitLocation.world)

        val format = ClipboardFormats.findByFile(file)
        format!!.getReader(FileInputStream(file)).use { reader ->
            clipboard = reader.read()
        }


        WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
            val operation: Operation = ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(BlockVector3.at(bukkitLocation.x, bukkitLocation.y, bukkitLocation.z))
                .ignoreAirBlocks(noAir)
                .build()
            Operations.complete(operation)
        }
    }
}


