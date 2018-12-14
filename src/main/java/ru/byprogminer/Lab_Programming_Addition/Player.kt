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

import java.awt.image.BufferedImage

import javax.imageio.ImageIO

import kotlin.random.Random

open class Player(
        val name: String = "Player"
) {

    enum class Direction(val x: Int, val y: Int) {

        RIGHT(1, 0), DOWN(0, -1), LEFT(-1, 0), UP(0, 1)
    }

    companion object {

        val defaultTexture: BufferedImage = ImageIO.read(this::class.java.getResourceAsStream("/assets/images/player.png"))
    }

    var game: Game? = null
        set(value) {
            if (field != null && value != field) {
                throw RuntimeException("Change game of player is not permitted")
            }

            field = value
        }

    var x = 0
        private set

    var y = 0
        private set

    var rotation = 1
        private set

    fun move(direction: Direction) {
        val game = game!!

        if (game.state == Game.State.BEFORE) {
            return
        }

        var newX = x
        var newY = y

        newX += direction.x
        newY += direction.y

        if (y == game.size.height - 1 && newX in 0 until game.size.width) {
            x = newX

        } else if (
                newX in 0 until game.size.width &&
                newY in 0 until game.size.height &&
                game.getBlockAt(newX, newY).canStand()
        ) {
            x = newX
            y = newY

            game.getBlockAt(newX, newY).onStand(this)
        }

        /**
         *   r | d.x | d.x * 2 |  + |  %
         * ----+-----+---------+----+----
         *   1 |  1  |    2    |  3 |  1
         *   1 |  0  |    0    |  1 |  1
         *   1 | -1  |   -2    | -1 | -1
         *  -1 |  1  |    2    |  1 |  1
         *  -1 |  0  |    0    | -1 | -1
         *  -1 | -1  |   -2    | -3 | -1
         *
         *    r\d.x | -1 |  0 |  1
         *  --------+----+----+----
         *     -1   | -1 | -1 |  1
         *  --------+----+----+----
         *      1   | -1 |  1 |  1
         */
        rotation = (rotation + direction.x * 2) % 2
    }

    fun fall() {
        val game = game!!

        if (game.state == Game.State.BEFORE) {
            return
        }

        var playerY = y

        if (playerY == 0) {
            return
        }

        val height = Random(System.currentTimeMillis()).nextInt(1, playerY + 1)
        for (i in 0 until height) {
            --playerY

            if (!game.getBlockAt(x, playerY).canStand()) {
                if (i == 0) {
                    return
                }

                break
            }

            y = playerY
        }

        game.getBlockAt(x, y).onStand(this)
    }

    open fun getTexture() = defaultTexture

    override fun toString() = name
}