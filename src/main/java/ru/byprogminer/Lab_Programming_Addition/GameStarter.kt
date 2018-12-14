package ru.byprogminer.Lab_Programming_Addition

import ru.byprogminer.Lab_Programming_Addition.StartPanel.GameMode
import java.awt.Color
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.KeyListener

import java.util.*
import javax.swing.JButton

import javax.swing.JFrame
import javax.swing.JOptionPane

class GameStarter {

    var width = 0
        private set

    var height = 0
        private set

    var gameMode = GameMode.SINGLEPLAYER
        private set

    val gameWindow = JFrame("$APP_NAME $APP_VERSION")
    lateinit var gamePanel: GamePanel

    lateinit var game: Game
        private set

    private val keyListeners = mutableSetOf<KeyListener>()

    init {
        gameWindow.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        gameWindow.setLocationRelativeTo(null)
        gameWindow.isFocusable = true
    }

    fun init(width: Int, height: Int, gameMode: GameMode) {
        if (this::game.isInitialized) {
            throw RuntimeException("Game is already initialized")
        }

        this.width = width
        this.height = height
        this.gameMode = gameMode

        initGame()
        initGameWindow()

        gameWindow.isVisible = true
    }

    fun start() {
        game.start()
    }

    fun makeCallback(startWindow: JFrame) = { width: Int, height: Int, gameMode: GameMode ->
        init(width, height, gameMode)

        if (gameMode != GameMode.MULTIPLAYER) {
            start()
        }

        startWindow.isVisible = false
    }

    fun restart() {
        if (!this::game.isInitialized) {
            throw RuntimeException("Game is not started")
        }

        keyListeners.forEach { gameWindow.removeKeyListener(it) }
        keyListeners.clear()

        val players = mutableSetOf<Player>()
        players.addAll(game.players)

        initGame()

        gamePanel.game = game
        game.start()
    }

    private fun initGame() {
        game = Game(width, height)
        game.callbacks[Game.State.AFTER] = ::gameOver

        when (gameMode) {
            GameMode.ONE_COMPUTER -> {
                val player1 = Player("Player 1")
                game.joinPlayer(player1)

                val controller1 = KeyPlayerController(player1)
                controller1.bindings = KeyPlayerController.WASD_BINDINGS
                gameWindow.addKeyListener(controller1)
                keyListeners.add(controller1)

                val player2 = Player("Player 2")
                game.joinPlayer(player2)

                val controller2 = KeyPlayerController(player2)
                val controller3 = KeyPlayerController(player2)
                controller3.bindings = KeyPlayerController.HJKL_BINDINGS
                gameWindow.addKeyListener(controller2)
                gameWindow.addKeyListener(controller3)
                keyListeners.add(controller2)
                keyListeners.add(controller3)
            }

            else -> {
                val player = Player()
                game.joinPlayer(player)

                val controller = KeyPlayerController(player)
                gameWindow.addKeyListener(controller)
                keyListeners.add(controller)
            }
        }
    }

    private fun initGameWindow() {
        gamePanel = GamePanel(game)
        gamePanel.background = Color.BLACK
        gamePanel.layout = null

        val resetButton = JButton("Restart")
        resetButton.bounds = Rectangle(Point(10, 10), resetButton.preferredSize)
        resetButton.isFocusable = false
        resetButton.addActionListener {
            restart()
        }
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

    private fun winMessage(message: Any) {
        JOptionPane.showMessageDialog(gameWindow, message, "$APP_NAME $APP_VERSION", JOptionPane.INFORMATION_MESSAGE)
    }
}