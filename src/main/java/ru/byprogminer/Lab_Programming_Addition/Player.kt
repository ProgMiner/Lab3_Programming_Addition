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

        if (game.state == Game.State.AFTER && newX in 0 until game.size.width) {
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