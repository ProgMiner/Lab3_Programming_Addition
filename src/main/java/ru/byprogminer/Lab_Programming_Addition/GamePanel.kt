package ru.byprogminer.Lab_Programming_Addition

import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.image.BufferedImage

import javax.swing.JPanel

import kotlin.math.roundToInt

class GamePanel(game: Game): JPanel() {

    var bottomHeightCoef = 0.4
    var skyColor: Color = Color.CYAN

    var game = game
        set(value) {
            field = value

            clearBuffers()
            repaint()
        }

    private var playersBuffer = mutableMapOf<Player, BufferedImage?>()
    private val tilesBuffer = Array<BufferedImage?>(game.size.height) { null }
    private val objectsBuffer = Array<BufferedImage?>(game.size.height) { null }

    init {
        isDoubleBuffered = false

        addComponentListener(object: ComponentAdapter() {

            override fun componentResized(event: ComponentEvent) {
                clearBuffers()
                repaint()
            }
        })
    }

    fun clearBuffers() {
        playersBuffer.clear()

        for (y in 0 until game.size.height) {
            tilesBuffer[y] = null
            objectsBuffer[y] = null
        }
    }

    private fun regenBuffers(coef: Int) {
        val coefWidth = coef * game.size.width

        for (y in 0 until tilesBuffer.size) {
            if (tilesBuffer[y] != null) {
                continue
            }

            val newTilesBuffer = BufferedImage(coefWidth, coef, BufferedImage.TYPE_INT_ARGB)
            val graphics = newTilesBuffer.graphics

            for (x in 0 until game.size.width) {
                graphics.drawImage(game.blocks[y][x].getTexture(), coef * x, 0, coef, coef, null)
            }

            tilesBuffer[y] = newTilesBuffer
        }

        for (y in 0 until objectsBuffer.size) {
            if (objectsBuffer[y] != null) {
                continue
            }

            var newObjectsBuffer = BufferedImage(coefWidth, 1, BufferedImage.TYPE_INT_ARGB)
            for (x in game.blocks[y].indices) {
                val objects = game.blocks[y][x].getObjectsTexture(coef)

                if (objects.height > newObjectsBuffer.height) {
                    val newObjectsBuffer2 = BufferedImage(coefWidth, objects.height, BufferedImage.TYPE_INT_ARGB)
                    newObjectsBuffer2.graphics.drawImage(newObjectsBuffer, 0, 0, null)

                    newObjectsBuffer = newObjectsBuffer2
                }

                newObjectsBuffer.graphics.drawImage(objects, coef * x, 0, null)
            }

            objectsBuffer[y] = newObjectsBuffer
        }

        for (player in game.players) {
            playersBuffer[player] = player.getTexture()
        }
    }

    override fun paintComponent(graphics: Graphics) {
        super.paintComponent(graphics)

        val maxY = game.size.height - 1
        val sumOfFirstQuads = maxY * (maxY + 1) * (2 * maxY + 1) / 6 // Sum of N first quads
        val heightCoefDeltaCoef = (1 - bottomHeightCoef) / Math.pow(maxY.toDouble(), 2.0)
        val baseHeight = ((maxY + 1) * bottomHeightCoef + sumOfFirstQuads * heightCoefDeltaCoef).fixNaN()
        val coef = maxOf(1, minOf(width / game.size.width, (height / baseHeight).roundToInt()))
        val coefWidth = coef * game.size.width
        val coefHeight = (coef * baseHeight).roundToInt()
        var offset = height - coefHeight

        graphics.color = skyColor
        graphics.fillRect(0, 0, coefWidth, height)

        if (graphics is Graphics2D) {
            graphics.paint = GradientPaint(0F, 0F, Color(0, 0, 0, 0), 0F, height.toFloat(), Color(0, 0, 0, 180))
            graphics.fillRect(0, 0, coefWidth, height)
        }

        val players = mutableMapOf<Int, Pair<Int, Player>>()
        for (player in game.players) {
            players[player.y] = player.x to player
        }

        for (y in maxY downTo 0) {
            val heightCoef = (coef * (bottomHeightCoef + Math.pow(y.toDouble(), 2.0) * heightCoefDeltaCoef).fixNaN()).roundToInt()

            if (tilesBuffer[y] == null) {
                regenBuffers(coef)
            }

            graphics.drawImage(tilesBuffer[y]!!, 0, offset, coefWidth, heightCoef, this)
            offset += heightCoef

            if (objectsBuffer[y] == null) {
                regenBuffers(coef)
            }

            graphics.drawImage(objectsBuffer[y]!!, 0, offset - coef, coefWidth, coef, null)

            val (playerX, player) = players[y] ?: 0 to null
            if (player != null) {
                if (playersBuffer[player] == null) {
                    regenBuffers(coef)
                }

                graphics.drawImage(playersBuffer[player]!!, coef * (playerX - (player.rotation - 1) / 2), offset - coef, coef * player.rotation, coef, this)
            }
        }

        if (graphics is Graphics2D) {
            graphics.paint = GradientPaint(0F, (height - coefHeight + coef).toFloat(), Color(0, 0, 0, 0), 0F, height.toFloat(), Color(0, 0, 0, 170))
            graphics.fillRect(0, height - coefHeight, coefWidth, coefHeight)
        }
    }
}
