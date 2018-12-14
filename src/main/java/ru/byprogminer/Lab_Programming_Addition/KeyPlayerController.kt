package ru.byprogminer.Lab_Programming_Addition

import ru.byprogminer.Lab_Programming_Addition.Player.Direction

import java.awt.event.KeyEvent
import java.awt.event.KeyListener

class KeyPlayerController(player: Player): PlayerController(player), KeyListener {

    companion object {

        val DEFAULT_BINDINGS = mapOf(
                KeyEvent.VK_UP    to Direction.UP,
                KeyEvent.VK_LEFT  to Direction.LEFT,
                KeyEvent.VK_DOWN  to Direction.DOWN,
                KeyEvent.VK_RIGHT to Direction.RIGHT
        )

        val SECOND_PLAYER_BINDINGS = mapOf(
                KeyEvent.VK_W to Direction.UP,
                KeyEvent.VK_A to Direction.LEFT,
                KeyEvent.VK_S to Direction.DOWN,
                KeyEvent.VK_D to Direction.RIGHT
        )
    }

    var bindings = DEFAULT_BINDINGS

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