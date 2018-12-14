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

import java.awt.*
import java.util.*

import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane

const val APP_NAME = "Lab Programming Addition"
const val APP_VERSION = "2.0-SNAPSHOT"

private val startWindow = JFrame("$APP_NAME $APP_VERSION")
private val gameWindow = JFrame("$APP_NAME $APP_VERSION")

fun main() {
    startWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    startWindow.isResizable = false

    val startPanel = StartPanel(3, ::start)

    startWindow.contentPane = startPanel

    startWindow.pack()
    startWindow.minimumSize = startWindow.size

    centerWindow(startWindow)
    startWindow.isVisible = true
}

fun Double.fixNaN(fallback: Double = 1.0) = when {
    this.isNaN() -> fallback
    else         -> this
}

private fun centerWindow(window: JFrame) {
    val centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().centerPoint

    window.bounds = Rectangle(
            centerPoint.x - window.width / 2,
            centerPoint.y - window.height / 2,
            window.width,
            window.height
    )
}

private fun start(width: Int, height: Int, gameMode: StartPanel.GameMode) {
    startWindow.isVisible = false

    gameWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    gameWindow.setLocationRelativeTo(null)
    gameWindow.isFocusable = true

    var game = Game(width, height)
    game.callbacks[Game.State.AFTER] = ::gameOver

    var player = Player()
    game.joinPlayer(player)
    // game.joinPlayer(Player())

    var gameController = KeyPlayerController(player)
    gameWindow.addKeyListener(gameController)

    val resetButton = JButton("New game")
    resetButton.bounds = Rectangle(Point(10, 10), resetButton.preferredSize)
    resetButton.isFocusable = false

    val gamePanel = GamePanel(game)
    gamePanel.background = Color.BLACK
    gamePanel.layout = null

    gamePanel.add(resetButton)

    gameWindow.contentPane = gamePanel
    gameWindow.revalidate()
    game.start()

    resetButton.addActionListener {
        gameWindow.removeKeyListener(gameController)

        game = Game(width, height)
        game.callbacks[Game.State.AFTER] = ::gameOver

        player = Player()
        game.joinPlayer(player)

        gameController = KeyPlayerController(player)
        gameWindow.addKeyListener(gameController)

        gamePanel.game = game
        game.start()
    }

    gameWindow.rootPane.preferredSize = Dimension(20 + resetButton.width, 20 + resetButton.height)
    gameWindow.pack()
    gameWindow.minimumSize = gameWindow.size

    gameWindow.rootPane.preferredSize = Dimension(640, 480)
    gameWindow.pack()

    centerWindow(gameWindow)
    gameWindow.isVisible = true
}

private fun winMessage(message: Any) {
    JOptionPane.showMessageDialog(gameWindow, message, "$APP_NAME $APP_VERSION", JOptionPane.INFORMATION_MESSAGE)
}

private fun gameOver(game: Game) {
    val players = game.players

    gameWindow.repaint()
    if (players.size == 1) {
        winMessage("You win!")
    } else {
        val sortedPlayers = TreeMap<Int, Player>(Comparator.reverseOrder())

        for (player in players) {
            sortedPlayers[player.y] = player
        }

        val msgBuilder = StringBuilder("Player ")
                .append(sortedPlayers[sortedPlayers.firstKey()]!!.name)
                .append(" win!\n")

        var counter = 0
        for ((playerY, player) in sortedPlayers) {
            msgBuilder
                    .append('\n')
                    .append(++counter)
                    .append(". ")
                    .append(player.name)
                    .append(" (")
                    .append(playerY)
                    .append(')')
        }

        winMessage(msgBuilder)
    }
}
