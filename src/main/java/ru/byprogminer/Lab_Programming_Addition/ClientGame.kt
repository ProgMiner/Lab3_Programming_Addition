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

import java.lang.reflect.Proxy
import java.net.Socket
import java.util.*

import ru.byprogminer.Lab_Programming_Addition.util.Dimension
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.reflect.jvm.javaMethod

class ClientGame
private constructor(private val socket: Socket): AbstractGame() {

    private inner class Updater(port: Int, address: InetAddress): Runnable {

        lateinit var schedule: ScheduledFuture<*>
            private set

        val socket = DatagramSocket(port, address)

        constructor(port: Int): this(port, InetAddress.getByName("0.0.0.0"))

        fun scheduleUpdate(delay: Long, period: Long, timeUnit: TimeUnit) {
            schedule = Executors.newSingleThreadScheduledExecutor()
                    .scheduleAtFixedRate(this, delay, period, timeUnit)
        }

        private fun receivePacket(): Packet? {
            val bufSize = PACSIZE + 1

            val byteArray = ByteArrayOutputStream()
            do {
                val packet = DatagramPacket(ByteArray(bufSize), bufSize)
                socket.receive(packet)

                byteArray.write(packet.data, 0, minOf(packet.length, PACSIZE))
            } while (packet.length > PACSIZE)

            return ObjectInputStream(ByteArrayInputStream(byteArray.toByteArray())).readObject() as? Packet
        }

        override fun run() {
            while (true) {
                val response = receivePacket() as? Packet.Response.Sync ?: continue

                _players = players.toMutableList()

                if (state != response.state) {
                    state = response.state

                    callbacks[response.state]?.invoke(this@ClientGame)
                } else {
                    state = response.state
                }
            }
        }
    }

    companion object {

        private const val PACSIZE = 1024

        private const val DEFAULT_UPDATE_DELAY = 0L
        private const val DEFAULT_UPDATE_PERIOD = 100L
        private val DEFAULT_UPDATE_TIME_UNIT = TimeUnit.MILLISECONDS

        fun connectTo(socket: Socket): ClientGame {
            val response = (Packet.Request.Connect send socket) as Packet.Response.Connect

            val game = ClientGame(socket)

            game.size = response.size
            game._blocks = Array(response.size.height) { y ->
                Array(response.size.width) { x -> response.blocks[y][x] }
            }
            game._players = response.players.toMutableList()

            game.updater = game.Updater(response.updatePort)

            return game
        }
    }

    private lateinit var updater: Updater

    override lateinit var size: Dimension
        private set

    override val blocks: Array<out Array<out Block>>
        get() = Arrays.copyOf(_blocks, _blocks.size)

    private lateinit var _blocks: Array<out Array<out Block>>

    override var state = State.BEFORE
        private set

    override val players: List<Player>
        get() = _players.toList()

    private lateinit var _players: MutableList<Player>

    fun scheduleUpdate(
            delay: Long = DEFAULT_UPDATE_DELAY,
            period: Long = DEFAULT_UPDATE_PERIOD,
            timeUnit: TimeUnit = DEFAULT_UPDATE_TIME_UNIT
    ) {
        updater.scheduleUpdate(delay, period, timeUnit)
    }

    override fun joinPlayer(player: Player): Int {
        val response = (Packet.Request.PlayerJoin(player) send socket) as Packet.Response.PlayerJoin

        _players[response.playerId] = Proxy.newProxyInstance(
                Player::class.java.classLoader,
                arrayOf(Player::class.java)
        ) { proxy, method, args ->
            if (method == Player::move.javaMethod) {
                println("$player::move at $proxy")
            }

            method.invoke(player, args)
        } as Player

        return response.playerId
    }

    override fun leavePlayer(player: Player) {
        Packet.Request.PlayerLeave(player) send socket
    }

    override fun leavePlayer(player: Int) {
        Packet.Request.PlayerLeaveById(player) send socket
    }

    override fun getBlockAt(x: Int, y: Int) =
            _blocks[y][x]
}
