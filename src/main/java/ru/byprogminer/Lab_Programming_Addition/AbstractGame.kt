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

import java.awt.Dimension
import java.awt.image.BufferedImage

import javax.imageio.ImageIO

import kotlin.random.Random

abstract class AbstractGame(val size: Dimension) {

    enum class State {

        BEFORE, GAME, AFTER
    }

    enum class Block {

        NORMAL,

        GROUND {

            private val texture: BufferedImage = ImageIO.read(javaClass.getResourceAsStream("/assets/images/blocks/ground.png"))

            override fun onStand(player: Player) {
                try {
                    player.game!!.stop()
                } catch (ex: RuntimeException) {}
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

            override fun onStand(player: Player) {
                player.fall()
            }

            override fun getTexture() = texture
        };

        private val texture: BufferedImage = ImageIO.read(javaClass.getResourceAsStream("/assets/images/blocks/normal.png"))

        open fun canStand() = true

        open fun onStand(player: Player) {
            if (Random(System.currentTimeMillis()).nextInt(10) == 0) {
                player.fall()
            }
        }

        open fun getTexture() = texture

        open fun getObjectsTexture(coef: Int) = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
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
    abstract val blocks: Array<out Array<Block>>

    abstract var state: State
        protected set

    val callbacks = mutableMapOf<State, (AbstractGame) -> Unit>()

    abstract val players: List<Player>

    abstract fun joinPlayer(player: Player)
    abstract fun leavePlayer(player: Player)

    abstract fun getBlockAt(x: Int, y: Int): Block

    open fun start() {}
    open fun stop() {}
}
