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

import javax.swing.JButton
import javax.swing.JFrame

const val APP_NAME = "Lab3 Programming Addition"
const val APP_VERSION = "1.0"

private val startWindow = JFrame("$APP_NAME $APP_VERSION")

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

private fun start(width: Int, height: Int) {
    startWindow.isVisible = false

    var game = Game(width, height)

    val gameWindow = JFrame("$APP_NAME $APP_VERSION")
    gameWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    gameWindow.setLocationRelativeTo(null)
    gameWindow.isFocusable = true

    var gameController = game.Controller()
    gameWindow.addKeyListener(gameController)

    val resetButton = JButton("Reset")
    resetButton.bounds = Rectangle(Point(10, 10), resetButton.preferredSize)

    val gamePanelRegen = {
        val gamePanel = game.Panel()
        gamePanel.background = Color.BLACK
        gamePanel.layout = null

        gamePanel.add(resetButton)

        gameWindow.contentPane = gamePanel
        gameWindow.revalidate()

        gamePanel
    }
    gamePanelRegen()

    val newGame = {
        gameWindow.removeKeyListener(gameController)

        game = Game(width, height)

        gamePanelRegen()
        gameController = game.Controller()
        gameWindow.addKeyListener(gameController)
    }

    resetButton.addActionListener {
        newGame()
    }

    gameWindow.rootPane.preferredSize = Dimension(20 + resetButton.width, 20 + resetButton.height)
    gameWindow.pack()
    gameWindow.minimumSize = gameWindow.size

    gameWindow.rootPane.preferredSize = Dimension(640, 480)
    gameWindow.pack()

    centerWindow(gameWindow)
    gameWindow.isVisible = true
}