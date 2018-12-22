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

package ru.byprogminer.Lab_Programming_Addition.gui

import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import javax.swing.*

import ru.byprogminer.Lab_Programming_Addition.APP_NAME
import ru.byprogminer.Lab_Programming_Addition.GameServer
import ru.byprogminer.Lab_Programming_Addition.Player

class PlayersWaitPanel(
        gap: Int = 2,
        val gameServer: GameServer,
        private val callback: () -> Unit
): JPanel(GridBagLayout()) {

    var maxPlayers = 100

    private val titleLable = JLabel("Waiting for players", JLabel.CENTER)
    private val playersListModel = DefaultListModel<Player>()
    private val playersList = JList<Player>(playersListModel)
    private val startButton = JButton("Start game")

    init {
        border = BorderFactory.createEmptyBorder(gap, gap, gap, gap)

        val font = font.deriveFont(20F)

        titleLable.font = font
        add(titleLable, GridBagConstraints(0, 0, 1, 1, 1.0, .0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))

        playersList.font = font
        playersList.border = BorderFactory.createLineBorder(Color.GRAY)
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            val players = gameServer.game.players

            for (player in playersListModel.elements()) {
                if (!players.contains(player)) {
                    playersListModel.removeElement(player)
                }
            }

            gameServer.game.players.forEach {
                if (!playersListModel.contains(it)) {
                    playersListModel.addElement(it)
                }
            }
        }, 0, 10, TimeUnit.MILLISECONDS)
        add(playersList, GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, Insets(gap, 0, 0, 0), 0, 0))

        startButton.font = font
        startButton.addActionListener(this::actionPerformed)
        add(startButton, GridBagConstraints(0, 2, 1, 1, 1.0, .0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, Insets(gap, 0, 0, 0), 0, 0))
    }

    private fun actionPerformed(event: ActionEvent) {
        if (gameServer.game.players.size !in 1..maxPlayers) {
            JOptionPane.showMessageDialog(this, "Players count must be in [1, $maxPlayers]!", APP_NAME, JOptionPane.WARNING_MESSAGE)
            return
        }

        callback()
    }
}
