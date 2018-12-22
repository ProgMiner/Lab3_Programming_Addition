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
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.KeyListener
import java.net.Socket
import java.util.*

import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane

import ru.byprogminer.Lab_Programming_Addition.gui.GamePanel

open class GameStarter {

    open lateinit var game: AbstractGame
        protected set

    val gameWindow = JFrame("$APP_NAME $APP_VERSION")
    open lateinit var gamePanel: GamePanel
        protected set

    protected val players = mutableMapOf<Player, PlayerController>()

    init {
        gameWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        gameWindow.setLocationRelativeTo(null)
        gameWindow.isFocusable = true

    }

    open fun start(difficulty: Game.Difficulty, width: Int, height: Int, players: Map<Player, PlayerController>) {
        checkGameNotStarted()

        game = Game(width, height, difficulty)
        game.callbacks[AbstractGame.State.AFTER] = ::gameOver

        this.players.clear()
        players.forEach { player, controller ->
            this.players[player] = controller

            game.joinPlayer(player)

            if (controller is KeyListener) {
                gameWindow.addKeyListener(controller)
            }
        }

        initGameWindow()
        game.start()
    }

    open fun restart() {}

    open fun connect(address: String, port: Int) {
        checkGameNotStarted()

        val socket = Socket(address, port)
        try {
            game = ClientGame(socket)
        } catch (ex: Throwable) {
            socket.close()
            throw ex
        }
    }

    protected fun checkGameNotStarted() {
        if (this::game.isInitialized && game.state != AbstractGame.State.BEFORE) {
            throw RuntimeException("Game is already started")
        }
    }

    protected fun initGameWindow() {
        gamePanel = GamePanel(game)
        gamePanel.background = Color.BLACK
        gamePanel.layout = null

        val resetButton = JButton("Restart")
        resetButton.bounds = Rectangle(Point(10, 10), resetButton.preferredSize)
        resetButton.isFocusable = false
        resetButton.addActionListener { restart() }
        gamePanel.add(resetButton)

        gameWindow.contentPane = gamePanel
        gameWindow.revalidate()

        gameWindow.rootPane.preferredSize = Dimension(20 + resetButton.width, 20 + resetButton.height)
        gameWindow.pack()
        gameWindow.minimumSize = gameWindow.size

        gameWindow.rootPane.preferredSize = Dimension(640, 480)
        gameWindow.pack()

        centerWindow(gameWindow)
    }

    protected fun gameOver(game: AbstractGame) {
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

    protected fun winMessage(message: Any) {
        JOptionPane.showMessageDialog(gameWindow, message, "$APP_NAME $APP_VERSION", JOptionPane.INFORMATION_MESSAGE)
    }
}
