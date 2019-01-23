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

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

import ru.byprogminer.Lab_Programming_Addition.util.Dimension

sealed class Packet {

    sealed class Request: Packet() {

        object Connect: Request()

        data class PlayerJoin(
                val player: Player
        ): Request()

        data class PlayerLeave(
                val player: Player
        ): Request()

        data class PlayerLeaveById(
                val playerId: Int
        ): Request()
    }

    sealed class Response: Packet() {

        data class Connect(
                val updatePort: Int,
                val size: Dimension,
                val blocks: List<List<AbstractGame.Block>>,
                val players: List<Player>
        ): Response()

        data class PlayerJoin(
                val playerId: Int
        ): Response()

        object PlayerLeave: Response()

        object PlayerLeaveById: Response()

        data class Sync(
                val players: List<Player>,
                val state: AbstractGame.State
        ): Response()

        data class Fail(
                val exception: Throwable
        ): Response()

        companion object {

            fun checkFail(packet: Response): Response {
                if (packet is Fail) {
                    throw packet.exception
                }

                return packet
            }
        }
    }

    infix fun send(socket: Socket): Response {
        val output = ObjectOutputStream(socket.getOutputStream())
        val input = ObjectInputStream(socket.getInputStream())

        output.writeObject(this)
        return Response.checkFail(input.readObject() as Response)
    }
}
