package ru.byprogminer.Lab_Programming_Addition

import java.net.Socket
import java.util.*

import ru.byprogminer.Lab_Programming_Addition.util.Dimension

class ClientGame(val socket: Socket): AbstractGame() {

    override lateinit var size: Dimension
        private set

    override val blocks: Array<out Array<Block>>
        get() = Arrays.copyOf(_blocks, _blocks.size)

    private var _blocks: Array<out Array<Block>>

    override var state = State.BEFORE
        private set

    override val players: List<Player>
        get() = _players.toList()

    private var _players: List<Player>

    init {
        val response = (Packet.Request.Connect send socket) as Packet.Response.Connect

        size = response.size
        _blocks = Array(size.height) { y ->
            Array(size.width) { x -> response.blocks[y][x] }
        }
        _players = response.players
    }

    override fun joinPlayer(player: Player) =
            ((Packet.Request.PlayerJoin(player) send socket) as Packet.Response.PlayerJoin).playerId

    override fun leavePlayer(player: Player) {
        Packet.Request.PlayerLeave(player) send socket
    }

    override fun leavePlayer(player: Int) {
        Packet.Request.PlayerLeaveById(player) send socket
    }

    override fun getBlockAt(x: Int, y: Int) =
            blocks[y][x]
}
