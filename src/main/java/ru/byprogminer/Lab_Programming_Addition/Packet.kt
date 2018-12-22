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
                val size: Dimension,
                val blocks: List<List<AbstractGame.Block>>,
                val players: List<Player>
        ): Response()

        data class PlayerJoin(
                val playerId: Int
        ): Response()

        object PlayerLeave: Response()

        object PlayerLeaveById: Response()

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
