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

import java.awt.Color
import java.awt.Dimension

import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame

const val APP_NAME = "Lab3 Addition"

private val startWindow = JFrame(APP_NAME)

fun main() {
    startWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    startWindow.isResizable = false

    val startPanel = StartPanel(::start)

    startPanel.border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
    startWindow.contentPane = startPanel

    startWindow.pack()
    startWindow.minimumSize = startWindow.size
    startWindow.isVisible = true
}

private fun start(width: Int, height: Int) {
    startWindow.isVisible = false

    var game = Game(width, height)

    val gameWindow = JFrame(APP_NAME)
    gameWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    gameWindow.isFocusable = true

    var gameController = game.Controller()
    gameWindow.addKeyListener(gameController)

    val resetButton = JButton("Reset")
    resetButton.setBounds(10, 10, 100, 25)

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

    gameWindow.rootPane.preferredSize = Dimension(120, 45)
    gameWindow.pack()
    gameWindow.minimumSize = gameWindow.size

    gameWindow.rootPane.preferredSize = Dimension(640, 480)
    gameWindow.pack()

    gameWindow.isVisible = true
}

fun Double.fixNaN(fallback: Double = 1.0) = when {
    this.isNaN() -> fallback
    else         -> this
}