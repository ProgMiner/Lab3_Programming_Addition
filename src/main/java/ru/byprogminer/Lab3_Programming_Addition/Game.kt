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

package ru.byprogminer.Lab3_Programming_Addition

import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage

import javax.imageio.ImageIO
import javax.swing.JPanel

import kotlin.math.roundToInt
import kotlin.random.Random

class Game(val size: Dimension, private val seed: Long = System.currentTimeMillis()) {

    enum class Block {

        NORMAL,

        GROUND {

            private val texture: BufferedImage = ImageIO.read(javaClass.getResourceAsStream("/assets/images/blocks/ground.png"))

            override fun onStand(game: Game) {
                game.gameOver = true
            }

            override fun getTexture() = texture
        },

        STONE {

            private val stone: BufferedImage = ImageIO.read(javaClass.getResourceAsStream("/assets/images/blocks/stone.png"))

            override fun canStand() = false

            override fun getObjectsTexture(coef: Int): BufferedImage {
                val ret = BufferedImage(coef, stone.height * coef / stone.width, BufferedImage.TYPE_INT_ARGB)
                ret.graphics.drawImage(stone, 0, 0, ret.width, ret.height, null)

                return ret
            }
        },

        DIRT {

            private val texture: BufferedImage = ImageIO.read(javaClass.getResourceAsStream("/assets/images/blocks/dirt.png"))

            override fun onStand(game: Game) {
                fall(game)
            }

            override fun getTexture() = texture
        };

        private val texture: BufferedImage = ImageIO.read(javaClass.getResourceAsStream("/assets/images/blocks/normal.png"))

        open fun canStand() = true

        open fun onStand(game: Game) {
            if (Random(System.currentTimeMillis()).nextInt(10) == 0) {
                fall(game)
            }
        }

        open fun getTexture() = texture

        open fun getObjectsTexture(coef: Int) = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)

        protected fun fall(game: Game) {
            var playerY = game.playerY

            if (playerY == 0) {
                return
            }

            val height = Random(System.currentTimeMillis()).nextInt(1, playerY + 1)
            for (i in 0 until height) {
                --playerY

                if (!game.blocks[playerY][game.playerX].canStand()) {
                    if (i == 0) {
                        return
                    }

                    break
                }

                game.playerY = playerY
            }

            game.blocks[game.playerY][game.playerX].onStand(game)
        }
    }

    inner class Panel: JPanel() {

        var bottomHeightCoef = 0.4
        var skyColor: Color = Color.CYAN

        private var playerBuffer: BufferedImage? = null
        private val tilesBuffer = Array<BufferedImage?>(this@Game.size.height) { null }
        private val objectsBuffer = Array<BufferedImage?>(this@Game.size.height) { null }

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
            playerBuffer = null

            for (y in 0 until this@Game.size.height) {
                tilesBuffer[y] = null
                objectsBuffer[y] = null
            }
        }

        private fun regenBuffers(coef: Int) {
            val coefWidth = coef * this@Game.size.width

            for (y in 0 until tilesBuffer.size) {
                if (tilesBuffer[y] != null) {
                    continue
                }

                val newTilesBuffer = BufferedImage(coefWidth, coef, BufferedImage.TYPE_INT_ARGB)
                val graphics = newTilesBuffer.graphics

                for (x in 0 until this@Game.size.width) {
                    graphics.drawImage(blocks[y][x].getTexture(), coef * x, 0, coef, coef, null)
                }

                tilesBuffer[y] = newTilesBuffer
            }

            for (y in 0 until objectsBuffer.size) {
                if (objectsBuffer[y] != null) {
                    continue
                }

                var newObjectsBuffer = BufferedImage(coefWidth, 1, BufferedImage.TYPE_INT_ARGB)
                for (x in blocks[y].indices) {
                    val objects = blocks[y][x].getObjectsTexture(coef)

                    if (objects.height > newObjectsBuffer.height) {
                        val newObjectsBuffer2 = BufferedImage(coefWidth, objects.height, BufferedImage.TYPE_INT_ARGB)
                        newObjectsBuffer2.graphics.drawImage(newObjectsBuffer, 0, 0, null)

                        newObjectsBuffer = newObjectsBuffer2
                    }

                    newObjectsBuffer.graphics.drawImage(objects, coef * x, 0, null)
                }

                objectsBuffer[y] = newObjectsBuffer
            }

            if (playerBuffer == null) {
                playerBuffer = ImageIO.read(javaClass.getResourceAsStream("/assets/images/player.png"))
            }
        }

        override fun paintComponent(graphics: Graphics) {
            super.paintComponent(graphics)

            val maxY = this@Game.size.height - 1
            val sumOfFirstQuads = maxY * (maxY + 1) * (2 * maxY + 1) / 6 // Sum of N first quads
            val heightCoefDeltaCoef = (1 - bottomHeightCoef) / Math.pow(maxY.toDouble(), 2.0)
            val baseHeight = ((maxY + 1) * bottomHeightCoef + sumOfFirstQuads * heightCoefDeltaCoef).fixNaN()
            val coef = maxOf(1, minOf(width / this@Game.size.width, (height / baseHeight).roundToInt()))
            val coefWidth = coef * this@Game.size.width
            val coefHeight = (coef * baseHeight).roundToInt()
            var offset = height - coefHeight

            graphics.color = skyColor
            graphics.fillRect(0, 0, coefWidth, height)

            if (graphics is Graphics2D) {
                graphics.paint = GradientPaint(0F, 0F, Color(0, 0, 0, 0), 0F, height.toFloat(), Color(0, 0, 0, 180))
                graphics.fillRect(0, 0, coefWidth, height)
            }

            val playerX = playerX
            val playerY = playerY
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

                if (playerY == y) {
                    if (playerBuffer == null) {
                        regenBuffers(coef)
                    }

                    graphics.drawImage(playerBuffer!!, coef * (playerX - (playerRotation - 1) / 2), offset - coef, coef * playerRotation, coef, this)
                }
            }

            if (graphics is Graphics2D) {
                graphics.paint = GradientPaint(0F, (height - coefHeight + coef).toFloat(), Color(0, 0, 0, 0), 0F, height.toFloat(), Color(0, 0, 0, 200))
                graphics.fillRect(0, height - coefHeight, coefWidth, coefHeight)
            }
        }
    }

    inner class Controller: KeyAdapter() {

        override fun keyPressed(event: KeyEvent) {
            var newPlayerX = playerX
            var newPlayerY = playerY

            when (event.keyCode) {
                KeyEvent.VK_UP    -> ++newPlayerY
                KeyEvent.VK_RIGHT -> {
                    ++newPlayerX
                    playerRotation = -1
                }
                KeyEvent.VK_DOWN  -> --newPlayerY
                KeyEvent.VK_LEFT  -> {
                    --newPlayerX
                    playerRotation = 1
                }
            }

            if (gameOver && newPlayerX >= 0 && newPlayerX < this@Game.size.width) {
                playerX = newPlayerX

            } else if (
                    newPlayerX >= 0 && newPlayerX < this@Game.size.width &&
                    newPlayerY >= 0 && newPlayerY < this@Game.size.height &&
                    blocks[newPlayerY][newPlayerX].canStand()
            ) {
                playerX = newPlayerX
                playerY = newPlayerY

                blocks[newPlayerY][newPlayerX].onStand(this@Game)
            }

            event.component.repaint()
        }
    }

    /**
     * ^ Y
     * | . . . . .
     * | . . . . .
     * | . . . . .
     * | . . . . .
     * | . . . . . X
     * +----------->
     */
    val blocks = Array(size.height) { y ->
        when (y) {
            size.height - 1 -> Array(size.width) { Block.GROUND }
            0               -> Array(size.width) { Block.NORMAL }
            else            -> Array(size.width) { Block.STONE }
        }
    }

    var playerX = 0
        private set

    var playerY = 0
        private set

    var playerRotation = 1
        private set

    var gameOver = false
        private set

    constructor(x: Int, y: Int): this(Dimension(x, y))
    constructor(x: Int, y: Int, seed: Long): this(Dimension(x, y), seed)

    init {
        makePath()
        clean()
    }

    private fun makePath() {
        val rand = Random(seed)

        while (true) {
            blocks[playerY][playerX].onStand(this)

            if (gameOver) {
                break
            }

            blocks[playerY][playerX] = Block.NORMAL
            when (rand.nextInt(4)) {
                0 -> ++playerY                          // Up

                1 -> if (playerX < size.width - 1) {    // Right
                    ++playerX
                }

                2 -> if (playerY > 0) {                 // Down
                    --playerY
                }

                3 -> if (playerX > 0) {                 // Left
                    --playerX
                }
            }
        }

        playerX = 0
        playerY = 0
        gameOver = false
    }

    private fun clean() {
        val rand = Random(seed)

        for (y in 1 until (size.height - 1)) {
            for (x in 0 until size.width) {
                if (blocks[y][x] != Block.STONE) {
                    continue
                }

                blocks[y][x] = when (rand.nextInt(10)) {
                    0    -> Block.STONE
                    1    -> Block.DIRT
                    else -> Block.NORMAL
                }
            }
        }
    }
}