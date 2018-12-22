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

import java.awt.Dimension
import java.util.*

import kotlin.random.Random

open class Game(size: Dimension, difficulty: Difficulty, seed: Long = System.currentTimeMillis()): AbstractGame(size) {

    enum class Difficulty {

        EASY, MEDIUM, HARD
    }

    override val blocks: Array<out Array<Block>>
        get() = Arrays.copyOf(_blocks, _blocks.size)

    private val _blocks = Array(size.height) { y ->
        when (y) {
            size.height - 1 -> Array(size.width) { Block.GROUND }
            0               -> Array(size.width) { Block.NORMAL }
            else            -> Array(size.width) { Block.STONE }
        }
    }

    override var state = State.BEFORE
        protected set

    override val players
        get() = _players.toList()

    private val _players = mutableListOf<Player>()

    constructor(width: Int, height: Int, difficulty: Difficulty): this(Dimension(width, height), difficulty)
    constructor(width: Int, height: Int, difficulty: Difficulty, seed: Long): this(Dimension(width, height), difficulty, seed)

    init {
        makePath(seed)
        clean(seed)
    }

    override fun joinPlayer(player: Player): Int {
        if (_players.contains(player)) {
            throw IllegalArgumentException("Player is already joined")
        }

        if (state != State.BEFORE) {
            throw RuntimeException("Joining after game start is not permitted")
        }

        player.game = this

        _players.add(player)
        return _players.indexOf(player)
    }

    override fun leavePlayer(player: Player) {
        if (!_players.contains(player)) {
            throw IllegalArgumentException("Player isn't joined")
        }

        _players.remove(player)

        if (state == State.GAME && _players.isEmpty()) {
            stop()
        }
    }

    override fun leavePlayer(player: Int) {
        leavePlayer(_players[player])
    }

    override fun start() {
        if (state != State.BEFORE) {
            throw RuntimeException("Game is already started")
        }

        if (_players.isEmpty()) {
            throw RuntimeException("Cannot start game without players")
        }

        state = State.GAME

        for (player in _players) {
            _blocks[0][0].onStand(player)
        }

        callbacks[state]?.invoke(this)
    }

    override fun stop() {
        if (state == State.BEFORE) {
            throw RuntimeException("Game isn't started")
        }

        if (state == State.AFTER) {
            throw RuntimeException("Game is already stopped")
        }

        state = State.AFTER
        callbacks[state]?.invoke(this)
    }

    override fun getBlockAt(x: Int, y: Int) =
            _blocks[y][x]

    private fun makePath(seed: Long) {
        val rand = Random(seed)

        var x = 0
        var y = 0
        while (y < size.height - 1) {
            _blocks[y][x] = Block.NORMAL

            when (rand.nextInt(4)) {
                0 -> if (x < size.width - 1) ++x
                1 -> if (y > 1) --y
                2 -> if (x > 1) --x
                3 -> ++y
            }
        }
    }

    private fun clean(seed: Long) {
        val rand = Random(seed)

        for (y in 1 until (size.height - 1)) {
            for (x in 0 until size.width) {
                if (_blocks[y][x] != Block.STONE) {
                    continue
                }

                _blocks[y][x] = when (rand.nextInt(10)) {
                    0    -> Block.STONE
                    1    -> Block.DIRT
                    else -> Block.NORMAL
                }
            }
        }
    }
}
