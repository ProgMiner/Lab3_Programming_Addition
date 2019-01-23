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

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.*
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class GameServer(private val socket: ServerSocket): GameStarter(), Runnable {

    private inner class SyncSender(val address: SocketAddress): Runnable {

        lateinit var schedule: ScheduledFuture<*>

        fun schedule() {
            schedule = executor.scheduleAtFixedRate(this, 0, SYNC_INTERVAL_VALUE, SYNC_INTERVAL_UNIT)
        }

        override fun run() {
            syncSocket.send(DatagramPacket(gameStateLength, gameStateLength.size, address))
            syncSocket.send(DatagramPacket(gameState, gameState.size, address))
        }
    }

    companion object {

        const val SYNC_INTERVAL_VALUE = 20L
        val SYNC_INTERVAL_UNIT = TimeUnit.MILLISECONDS
    }

    val syncSocket = DatagramSocket()
    private val executor = Executors.newSingleThreadScheduledExecutor()

    private lateinit var gameStateLength: ByteArray
    private lateinit var gameState: ByteArray

    private lateinit var syncStateSchedule: ScheduledFuture<*>
    private val syncSenders = mutableSetOf<SyncSender>()

    override fun run() {
        syncStateSchedule = executor.scheduleAtFixedRate(this::syncState, 0, SYNC_INTERVAL_VALUE, SYNC_INTERVAL_UNIT)

        while (true) {
            connect(socket.accept())
        }
    }

    private fun connect(socket: Socket) {
        val `is` = socket.getInputStream()
        val os = socket.getOutputStream()

        try {
            val ois = ObjectInputStream(`is`)

            val player = ois.readObject() as Player

            // TODO SocketPlayerController
            val id = game.joinPlayer(player)
            os.write(ByteBuffer.allocate(Int.SIZE_BYTES).putInt(id).array())

            val port = ByteBuffer.allocate(Int.SIZE_BYTES).put(`is`.readBytes()).int

            val sender = SyncSender(InetSocketAddress(socket.inetAddress, port))
            sender.schedule()
            syncSenders.add(sender)
        } catch (ex: Throwable) {
            ex.printStackTrace()
        } finally {
            `is`.close()
            os.close()
            socket.close()
        }
    }

    private fun syncState() {
        val bos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(bos)

        oos.writeObject(game.players)
        oos.close()
        bos.close()

        val gameState = bos.toByteArray()

        this.gameState = gameState
        this.gameStateLength = ByteBuffer.allocate(Int.SIZE_BYTES).putInt(gameState.size).array()
    }
}
