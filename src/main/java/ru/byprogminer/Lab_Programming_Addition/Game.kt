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

class Game(val size: Dimension, seed: Long = System.currentTimeMillis()) {

    enum class Block {

        NORMAL,

        GROUND {

            private val texture: BufferedImage = ImageIO.read(javaClass.getResourceAsStream("/assets/images/blocks/ground.png"))

            override fun onStand(player: Player) {
                player.game!!.gameOver = true
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
    val blocks = Array(size.height) { y ->
        when (y) {
            size.height - 1 -> Array(size.width) { Block.GROUND }
            0               -> Array(size.width) { Block.NORMAL }
            else            -> Array(size.width) { Block.STONE }
        }
    }

    val players: Set<Player> = mutableSetOf()
        get() {
            for (player in field) {
                if (!_players.contains(player)) {
                    (field as MutableSet).remove(player)
                }
            }

            for (player in _players) {
                if (!field.contains(player)) {
                    (field as MutableSet).add(player)
                }
            }

            return field
        }

    private val _players = mutableSetOf<Player>()

    var gameOver = false
        private set

    constructor(x: Int, y: Int): this(Dimension(x, y))
    constructor(x: Int, y: Int, seed: Long): this(Dimension(x, y), seed)

    init {
        makePath(seed)
        clean(seed)
    }

    fun joinPlayer(player: Player) {
        player.game = this

        _players.add(player)
    }

    fun leavePlayer(player: Player) {
        _players.remove(player)
    }

    private fun makePath(seed: Long) {
        val rand = Random(seed)

        var x = 0
        var y = 0
        while (y < size.height - 1) {
            blocks[y][x] = Block.NORMAL

            when (rand.nextInt(4)) {
                0 -> if (x < size.width - 1) ++x
                1 -> if (y > 1) --y
                2 -> if (x > 1) --x
                3 -> ++y
            }
        }
    }

    private fun clean(seed: Long) {
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