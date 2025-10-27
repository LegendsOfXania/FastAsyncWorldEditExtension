package fr.legendsofxania.fawe.entries.action

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.math.transform.AffineTransform
import com.sk89q.worldedit.session.ClipboardHolder
import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.*
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.utils.toBukkitLocation
import java.io.File

@Entry("paste_schematic", "Paste a schematic at a specific place for all players.", Colors.RED, "fluent:apps-48-filled")
class PasteSchematicActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("The name of your schematic. (include the extension of the file)")
    val schematic: String = "",
    @Help("The place where the schematic will be placed.")
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    @Help("Will air blocks be ignored?")
    val ignoreAir: Boolean = false,
    @Help("The rotation of the schematic in degrees")
    val rotation: Int = 0
) : ActionEntry {
    override fun ActionTrigger.execute() {
        val worldEdit = WorldEdit.getInstance()
        val file = File(worldEdit.schematicsFolderPath.toFile(), schematic)

        val clipboard = ClipboardFormats.findByFile(file)
            ?.getReader(file.inputStream())
            ?.read() ?: return

        val bukkitLoc = location.get(player, context).toBukkitLocation()
        val origin = BlockVector3.at(bukkitLoc.x, bukkitLoc.y, bukkitLoc.z)

        worldEdit.newEditSession(BukkitAdapter.adapt(bukkitLoc.world)).use { session ->
            val holder = ClipboardHolder(clipboard).apply {
                if (rotation != 0) {
                    transform = transform.combine(AffineTransform().rotateY(rotation.toDouble()))
                }
            }

            val operation = holder.createPaste(session)
                .to(origin)
                .ignoreAirBlocks(ignoreAir)
                .build()

            Operations.complete(operation)
        }
    }
}