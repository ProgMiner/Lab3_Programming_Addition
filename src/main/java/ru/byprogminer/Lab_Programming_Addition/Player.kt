package ru.byprogminer.Lab_Programming_Addition

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JOptionPane
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

    inner class Controller: KeyAdapter() {

        private fun move(event: KeyEvent) {
            val game = game!!
            val gameOver = game.gameOver

            move(when (event.keyCode) {
                KeyEvent.VK_UP    -> Direction.UP
                KeyEvent.VK_RIGHT -> Direction.RIGHT
                KeyEvent.VK_DOWN  -> Direction.DOWN
                KeyEvent.VK_LEFT  -> Direction.LEFT
                else -> return
            })

            if (!gameOver && game.gameOver) {
                event.component.repaint()
                JOptionPane.showMessageDialog(null, "You win!", "$APP_NAME $APP_VERSION", JOptionPane.INFORMATION_MESSAGE)
            }
        }

        override fun keyPressed(event: KeyEvent) {
            move(event)

            event.component.repaint()
        }
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

    constructor(game: Game): this() {
        this.game = game
    }

    constructor(name: String, game: Game): this(name) {
        this.game = game
    }

    fun move(direction: Direction) {
        val game = game!!
        var newX = x
        var newY = y

        newX += direction.x
        newY += direction.y

        if (game.gameOver && newX in 0 until game.size.width) {
            x = newX

        } else if (
                newX in 0 until game.size.width &&
                newY in 0 until game.size.height &&
                game.blocks[newY][newX].canStand()
        ) {
            x = newX
            y = newY

            game.blocks[newY][newX].onStand(this)
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
        var playerY = y

        if (playerY == 0) {
            return
        }

        val height = Random(System.currentTimeMillis()).nextInt(1, playerY + 1)
        for (i in 0 until height) {
            --playerY

            if (!game.blocks[playerY][x].canStand()) {
                if (i == 0) {
                    return
                }

                break
            }

            y = playerY
        }

        game.blocks[y][x].onStand(this)
    }

    open fun getTexture() = defaultTexture
}