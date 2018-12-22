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

import java.awt.GraphicsEnvironment
import java.awt.Rectangle

import javax.swing.JFrame

import ru.byprogminer.Lab_Programming_Addition.gui.StartPanel

const val APP_NAME = "Lab Programming Addition"
const val APP_VERSION = "3.0-SNAPSHOT"

private val startWindow = JFrame("$APP_NAME $APP_VERSION")
private lateinit var gameStarter: GameStarter

fun main() {
    startWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    startWindow.isResizable = false

    val startPanel = StartPanel(3) { event ->
        if (event is StartPanel.StartPanelEvent.ServerStartEvent) {
            println("${event.address}:${event.port}")

            return@StartPanel
        }

        if (!::gameStarter.isInitialized) {
            gameStarter = GameStarter()
        }

        when (event) {
            is StartPanel.StartPanelEvent.GameStartEvent ->
                gameStarter.start(event.difficulty, event.width, event.height, event.players)

            is StartPanel.StartPanelEvent.ConnectEvent ->
                gameStarter.connect(event.address, event.port)
        }

        gameStarter.gameWindow.isVisible = true
        startWindow.isVisible = false
    }

    // val oldStartPanel = OldStartPanel(3, gameStarter.makeCallback(startWindow) {
    //     val gameServer = GameServer(gameStarter.game, ServerSocket(0xDED))

    //     startWindow.isVisible = false
    //     startWindow.contentPane = PlayersWaitPanel(3, gameServer) {
    //         startWindow.isVisible = false

    //         gameStarter.gameWindow.isVisible = true
    //         gameStarter.start()
    //     }

    //     startWindow.pack()
    //     startWindow.minimumSize = startWindow.size
    //     startWindow.isVisible = true

    //     gameServer
    // })

    // startWindow.contentPane = oldStartPanel
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

fun centerWindow(window: JFrame) {
    val centerPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().centerPoint

    window.bounds = Rectangle(
            centerPoint.x - window.width / 2,
            centerPoint.y - window.height / 2,
            window.width,
            window.height
    )
}
