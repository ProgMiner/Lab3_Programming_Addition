/* MIT License

Copyright (c) 2018 Eridan Domoratskiy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE. */

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
                graphics.drawImage(game.getBlockAt(x, y).getTexture(), coef * x, 0, coef, coef, null)
            }

            tilesBuffer[y] = newTilesBuffer
        }

        for (y in 0 until objectsBuffer.size) {
            if (objectsBuffer[y] != null) {
                continue
            }

            var newObjectsBuffer = BufferedImage(coefWidth, 1, BufferedImage.TYPE_INT_ARGB)
            for (x in game.blocks[y].indices) {
                val objects = game.getBlockAt(x, y).getObjectsTexture(coef)

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

        val players = mutableMapOf<Int, MutableSet<Pair<Int, Player>>>()
        for (player in game.players) {
            val set = players[player.y] ?: mutableSetOf()

            set.add(player.x to player)

            players[player.y] = set
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

            val playersSet = players[y]
            if (playersSet != null) {
                for ((playerX, player) in playersSet) {
                    if (playersBuffer[player] == null) {
                        regenBuffers(coef)
                    }

                    graphics.drawImage(playersBuffer[player]!!, coef * (playerX - (player.rotation - 1) / 2), offset - coef, coef * player.rotation, coef, this)
                }
            }
        }

        if (graphics is Graphics2D) {
            graphics.paint = GradientPaint(0F, (height - coefHeight + coef).toFloat(), Color(0, 0, 0, 0), 0F, height.toFloat(), Color(0, 0, 0, 170))
            graphics.fillRect(0, height - coefHeight, coefWidth, coefHeight)
        }
    }
}
