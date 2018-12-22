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

import ru.byprogminer.Lab_Programming_Addition.Player.Direction

import java.awt.event.KeyEvent
import java.awt.event.KeyListener

class KeyPlayerController(
        player: Player,
        var bindings: Map<Int, Any> = ARROW_BINDINGS
): PlayerController(player), KeyListener {

    companion object {

        val ARROW_BINDINGS = mapOf(
                KeyEvent.VK_UP    to Direction.UP,
                KeyEvent.VK_LEFT  to Direction.LEFT,
                KeyEvent.VK_DOWN  to Direction.DOWN,
                KeyEvent.VK_RIGHT to Direction.RIGHT
        )

        val WASD_BINDINGS = mapOf(
                KeyEvent.VK_W to Direction.UP,
                KeyEvent.VK_A to Direction.LEFT,
                KeyEvent.VK_S to Direction.DOWN,
                KeyEvent.VK_D to Direction.RIGHT
        )

        val HJKL_BINDINGS = mapOf(
                KeyEvent.VK_K to Direction.UP,
                KeyEvent.VK_H to Direction.LEFT,
                KeyEvent.VK_J to Direction.DOWN,
                KeyEvent.VK_L to Direction.RIGHT
        )
    }

    private fun move(event: KeyEvent) {
        val binding = (bindings[event.keyCode] ?: return) as? Direction ?: return
        player.move(binding)

        event.component.repaint()
    }

    override fun keyPressed(event: KeyEvent) {
        move(event)
    }

    override fun keyReleased(event: KeyEvent) {}
    override fun keyTyped(event: KeyEvent) {}
}
