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

import java.awt.*
import java.awt.event.ItemEvent
import java.lang.reflect.Field

import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

import ru.byprogminer.Lab_Programming_Addition.*

class StartPanel(
        gap: Int,
        val callback: (StartPanelEvent) -> Unit
): JPanel(GridBagLayout()) {

    sealed class StartPanelEvent {

        data class ServerStartEvent(
                val port: Int,
                val address: String
        ): StartPanelEvent()

        data class GameStartEvent(
                val difficulty: Game.Difficulty,
                val width: Int,
                val height: Int,
                val players: Map<Player, PlayerController>
        ): StartPanelEvent()

        data class ConnectEvent(
                val port: Int,
                val address: String
        ): StartPanelEvent()
    }

    enum class Difficulty(val difficulty: Game.Difficulty, val text: String) {

        EASY(Game.Difficulty.EASY, "Easy"),
        MEDIUM(Game.Difficulty.MEDIUM, "Medium"),
        HARD(Game.Difficulty.HARD, "Hard");

        override fun toString() = text
    }

    enum class KeyScheme(val text: String, val keys: Map<Int, Player.Direction>) {

        WASD("WASD", KeyPlayerController.WASD_BINDINGS),
        ARROWS("Arrows", KeyPlayerController.ARROW_BINDINGS),
        HJKL("HJKL", KeyPlayerController.HJKL_BINDINGS);

        override fun toString() = text
    }

    private data class PlayerConfig(
            var player: Player,
            var keyScheme: KeyScheme = KeyScheme.ARROWS,
            val remote: Boolean = false
    ) {

        override fun toString() = player.toString()
    }

    private class Disabler {

        private lateinit var components: Map<Field, Boolean>

        fun disableAll(obj: StartPanel) {
            val components = mutableMapOf<Field, Boolean>()

            for (field in obj::class.java.declaredFields) {
                if (JComponent::class.java.isAssignableFrom(field.type)) {
                    field.isAccessible = true
                    components[field] = (field.get(obj) as JComponent).isEnabled
                    (field.get(obj) as JComponent).isEnabled = false
                    field.isAccessible = false
                }
            }

            this.components = components
        }

        fun revert(obj: StartPanel) {
            if (!this::components.isInitialized) {
                throw RuntimeException("Components isn't initialized")
            }

            for ((field, value) in components) {
                field.isAccessible = true
                (field.get(obj) as JComponent).isEnabled = value
                field.isAccessible = false
            }

        }
    }

    companion object {

        const val DEFAULT_PORT = 3565

        private val borderColor = Color.LIGHT_GRAY
        private val addresses = arrayOf("0.0.0.0", "127.0.0.1")
    }

    private val mainTabbedPane = JTabbedPane()
    private val gamePanel = JPanel(GridBagLayout())
    private val gameTypePanels: Map<JPanel, String>
    private val gameTypeComboBox: JComboBox<String>
    private val gameTypeLayout = CardLayout()
    private val gameTypePanel = JPanel(gameTypeLayout)
    private val newGamePanel = JPanel(GridBagLayout())
    private val newGameDifficultyLabel = JLabel("Difficulty:")
    private val newGameDifficultyComboBox = JComboBox<Difficulty>(Difficulty.values())
    private val newGameWidthLabel = JLabel("Width:")
    private val newGameWidthTextField = JTextField("20", 5)
    private val newGameHeightLabel = JLabel("Height:")
    private val newGameHeightTextField = JTextField("12", 5)
    private val connectPanel = JPanel(GridBagLayout())
    private val connectPortLabel = JLabel("Port:")
    private val connectPortComboBox = JComboBox<Int>(arrayOf(DEFAULT_PORT))
    private val connectAddressLabel = JLabel("Address:")
    private val connectAddressTextField = JTextField("127.0.0.1", 15)
    private val networkCheckBox = JCheckBox("Network game")
    private val networkPanel = JPanel(GridBagLayout())
    private val networkPortLabel = JLabel("Port:")
    private val networkPortComboBox = JComboBox<Int>(arrayOf(DEFAULT_PORT))
    private val networkAddressLabel = JLabel("Address:")
    private val networkAddressComboBox = JComboBox<String>(addresses)
    private val networkStartButton = JButton("Wait players")
    private val playersPanel = JPanel(GridBagLayout())
    private val playersListModel = DefaultListModel<PlayerConfig>()
    private val playersList = JList<PlayerConfig>(playersListModel)
    private val playersButtonsPanel = JPanel(GridBagLayout())
    private val playersButtonsAddButton = JButton("Add")
    private val playersButtonsRemoveButton = JButton("Kick")
    private val playersButtonsBanButton = JButton("Ban")
    private val playersPlayerPanel = JPanel(GridBagLayout())
    private val playersPlayerNameLabel = JLabel("Name:")
    private val playersPlayerNameTextField = JTextField(12)
    private val playersPlayerKeySchemeLabel = JLabel("Key scheme:")
    private val playersPlayerKeySchemeComboBox = JComboBox<KeyScheme>(KeyScheme.values())
    private val startButton = JButton("Start")

    var labelGap = 7

    init {
        gameTypePanels = mapOf(
                newGamePanel to "New game",
                connectPanel to "Connect"
        )
        gameTypeComboBox = JComboBox(gameTypePanels.values.toTypedArray())
        gameTypeComboBox.addActionListener { gameTypeLayout.show(gameTypePanel, gameTypeComboBox.getItemAt(gameTypeComboBox.selectedIndex)) }
        gamePanel.add(gameTypeComboBox, GridBagConstraints(0, 0, 2, 1, 1.0, .0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, 0), 0, 0))

        newGamePanel.add(newGameDifficultyLabel, GridBagConstraints(0, 0, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, labelGap), 0, 0))
        newGamePanel.add(newGameDifficultyComboBox, GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, 0), 0, 0))
        newGamePanel.add(newGameWidthLabel, GridBagConstraints(0, 1, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, labelGap), 0, 0))
        newGamePanel.add(newGameWidthTextField, GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, 0), 0, 0))
        newGamePanel.add(newGameHeightLabel, GridBagConstraints(0, 2, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, labelGap), 0, 0))
        newGamePanel.add(newGameHeightTextField, GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        newGamePanel.border = CompoundBorder(
                BorderFactory.createTitledBorder(gameTypePanels[newGamePanel]),
                BorderFactory.createEmptyBorder(gap, gap, gap, gap)
        )
        gameTypePanel.add(newGamePanel, gameTypePanels[newGamePanel])

        connectPanel.add(connectAddressLabel, GridBagConstraints(0, 0, 1, 1, .0, .0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, labelGap), 0, 0))
        connectPanel.add(connectAddressTextField, GridBagConstraints(1, 0, 1, 1, 1.0, .0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, 0), 0, 0))
        connectPanel.add(connectPortLabel, GridBagConstraints(0, 1, 1, 1, .0, .0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, labelGap), 0, 0))

        connectPortComboBox.isEditable = true
        connectPanel.add(connectPortComboBox, GridBagConstraints(1, 1, 1, 1, 1.0, .0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        connectPanel.add(JPanel(), GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, Insets(0, 0, 0, 0), 0, 0))
        connectPanel.border = CompoundBorder(
                BorderFactory.createTitledBorder(gameTypePanels[connectPanel]),
                BorderFactory.createEmptyBorder(gap, gap, gap, gap)
        )
        gameTypePanel.add(connectPanel, gameTypePanels[connectPanel])
        gameTypePanel.border = BorderFactory.createEmptyBorder(gap, gap, gap, gap)
        gamePanel.add(gameTypePanel, GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))

        networkCheckBox.addItemListener { changeNetworkComponentsState(it.stateChange == ItemEvent.SELECTED) }
        gamePanel.add(networkCheckBox, GridBagConstraints(0, 2, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        gamePanel.add(JPanel(), GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))

        networkPanel.add(networkPortLabel, GridBagConstraints(0, 0, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, labelGap), 0, 0))

        networkPortComboBox.isEnabled = false
        networkPortComboBox.isEditable = true
        networkPanel.add(networkPortComboBox, GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, 0), 0, 0))
        networkPanel.add(networkAddressLabel, GridBagConstraints(0, 1, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, labelGap), 0, 0))

        networkAddressComboBox.isEnabled = false
        networkAddressComboBox.isEditable = true
        networkPanel.add(networkAddressComboBox, GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, 0), 0, 0))

        networkStartButton.isEnabled = false
        networkStartButton.addActionListener {
            if (checkExceptionAndDisplay {
                        callback(StartPanelEvent.ServerStartEvent(
                                networkPortComboBox.selectedItem.toString().toInt(),
                                networkAddressComboBox.selectedItem.toString()
                        ))
                    }) {
                networkCheckBox.isEnabled = false
                changeNetworkComponentsState(false)
            }
        }
        networkPanel.add(networkStartButton, GridBagConstraints(0, 2, 2, 1, 1.0, 2.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        networkPanel.border = CompoundBorder(
                BorderFactory.createTitledBorder(networkCheckBox.text),
                BorderFactory.createEmptyBorder(gap, gap, gap, gap)
        )
        gamePanel.add(networkPanel, GridBagConstraints(0, 3, 2, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        gamePanel.border = BorderFactory.createEmptyBorder(gap, gap, gap, gap)
        mainTabbedPane.addTab("Game", gamePanel)

        playersListModel.addElement(PlayerConfig(Player()))
        playersList.border = BorderFactory.createLineBorder(borderColor)
        playersList.addListSelectionListener {
            changePlayerComponentsState(playersList.selectedValue)
        }
        playersPanel.add(playersList, GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, Insets(0, 0, gap, 0), 0, 0))

        playersButtonsAddButton.addActionListener {
            playersListModel.addElement(PlayerConfig(Player()))
        }
        playersButtonsPanel.add(playersButtonsAddButton, GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE, Insets(0, 0, 0, gap), 0, 0))

        playersButtonsRemoveButton.isEnabled = false
        playersButtonsRemoveButton.addActionListener {
            if (playersList.selectedValue != null) {
                playersListModel.removeElement(playersList.selectedValue)
            }
        }
        playersButtonsPanel.add(playersButtonsRemoveButton, GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE, Insets(0, 0, 0, gap), 0, 0))

        playersButtonsBanButton.isEnabled = false
        playersButtonsBanButton.addActionListener {
            if (playersList.selectedValue != null) {
                playersListModel.removeElement(playersList.selectedValue)
            }
        }
        playersButtonsPanel.add(playersButtonsBanButton, GridBagConstraints(2, 1, 1, 1, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE, Insets(0, 0, 0, 0), 0, 0))
        playersPanel.add(playersButtonsPanel, GridBagConstraints(0, 1, 1, 1, 1.0, .0, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE, Insets(0, 0, 0, 0), 0, 0))

        playersPlayerPanel.add(playersPlayerNameLabel, GridBagConstraints(0, 0, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, labelGap), 0, 0))

        playersPlayerNameTextField.isEnabled = false
        playersPlayerNameTextField.document.addDocumentListener(object: DocumentListener {

            private fun update() {
                val playerConfig = playersList.selectedValue ?: return

                if (playerConfig.remote) {
                    return
                }

                playerConfig.player = Player(playersPlayerNameTextField.text)
                repaint()
            }

            override fun changedUpdate(ev: DocumentEvent) {
                update()
            }

            override fun insertUpdate(ev: DocumentEvent) {
                update()
            }

            override fun removeUpdate(ev: DocumentEvent) {
                update()
            }
        })
        playersPlayerPanel.add(playersPlayerNameTextField, GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, 0), 0, 0))
        playersPlayerPanel.add(playersPlayerKeySchemeLabel, GridBagConstraints(0, 1, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, gap, gap), 0, 0))

        playersPlayerKeySchemeComboBox.isEnabled = false
        playersPlayerKeySchemeComboBox.addActionListener {
            val playerConfig = playersList.selectedValue ?: return@addActionListener

            if (playerConfig.remote) {
                return@addActionListener
            }

            playerConfig.keyScheme = playersPlayerKeySchemeComboBox.getItemAt(playersPlayerKeySchemeComboBox.selectedIndex)
        }
        playersPlayerPanel.add(playersPlayerKeySchemeComboBox, GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        playersPlayerPanel.border = CompoundBorder(
                BorderFactory.createTitledBorder("Player"),
                BorderFactory.createEmptyBorder(gap, gap, gap, gap)
        )
        playersPanel.add(playersPlayerPanel, GridBagConstraints(0, 2, 1, 1, 1.0, .0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        playersPanel.border = BorderFactory.createEmptyBorder(gap, gap, gap, gap)
        mainTabbedPane.addTab("Players", playersPanel)
        add(mainTabbedPane, GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, Insets(0, 0, gap, 0), 0, 0))

        startButton.addActionListener {
            val disabler = Disabler()
            disabler.disableAll(this)

            if (!checkExceptionAndDisplay {
                        val event = when (gameTypeComboBox.getItemAt(gameTypeComboBox.selectedIndex)) {
                            gameTypePanels[newGamePanel] -> StartPanelEvent.GameStartEvent(
                                    newGameDifficultyComboBox.getItemAt(newGameDifficultyComboBox.selectedIndex).difficulty,
                                    newGameWidthTextField.text.toInt(),
                                    newGameHeightTextField.text.toInt(),
                                    run {
                                        val players = mutableMapOf<Player, PlayerController>()

                                        for (playerConfig in playersListModel.elements()) {
                                            players[playerConfig.player] = if (playerConfig.remote) {
                                                TODO("SocketPlayerController")
                                            } else {
                                                KeyPlayerController(playerConfig.player, playerConfig.keyScheme.keys)
                                            }
                                        }

                                        return@run players
                                    }
                            )
                            gameTypePanels[connectPanel] -> StartPanelEvent.ConnectEvent(
                                    connectPortComboBox.selectedItem.toString().toInt(),
                                    connectAddressTextField.text
                            )
                            else -> throw RuntimeException()
                        }

                        callback(event)
                    }) {
                disabler.revert(this)
            }
        }
        add(startButton, GridBagConstraints(0, 1, 1, 1, 1.0, .0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))
        border = BorderFactory.createEmptyBorder(gap, gap, gap, gap)
    }

    fun onPlayerJoin(player: Player, keyScheme: KeyScheme = KeyScheme.WASD, remote: Boolean = true) {
        playersListModel.addElement(PlayerConfig(player, keyScheme, remote))

        repaint()
    }

    fun onPlayerLeave(player: Player) {
        for (playerConfig in playersListModel.elements()) {
            if (playerConfig.player == player) {
                playersListModel.removeElement(playerConfig)
                break
            }
        }

        repaint()
    }

    private fun changeNetworkComponentsState(state: Boolean) {
        networkPortComboBox.isEnabled = state
        networkAddressComboBox.isEnabled = state
        networkStartButton.isEnabled = state
    }

    private fun changePlayerComponentsState(state: PlayerConfig?) {
        changePlayerListComponentsState(false)
        changePlayerButtonsComponentsState(null)
        changePlayerPanelComponentsState(false)

        if (state == null) {
            changePlayerListComponentsState(true)
            changePlayerButtonsComponentsState(state)
            return
        }

        playersPlayerNameTextField.text = state.player.name
        playersPlayerKeySchemeComboBox.selectedItem = state.keyScheme

        changePlayerListComponentsState(true)
        changePlayerButtonsComponentsState(state)
        changePlayerPanelComponentsState(true)
    }

    private fun changePlayerListComponentsState(state: Boolean) {
        playersList.isEnabled = state
        playersButtonsAddButton.isEnabled = state
    }

    private fun changePlayerButtonsComponentsState(state: PlayerConfig?) {
        playersButtonsRemoveButton.isEnabled = state != null
        playersButtonsBanButton.isEnabled = state?.remote ?: false
    }

    private fun changePlayerPanelComponentsState(state: Boolean) {
        playersPlayerNameTextField.isEnabled = state
        playersPlayerKeySchemeComboBox.isEnabled = state
    }

    private fun checkExceptionAndDisplay(what: () -> Unit): Boolean {
        try {
            what()
        } catch (exception: Throwable) {
            JOptionPane.showMessageDialog(this, "${exception::class.java.simpleName}: \"${exception.localizedMessage}\"", "$APP_NAME $APP_VERSION", JOptionPane.ERROR_MESSAGE)
            return false
        }

        return true
    }
}
