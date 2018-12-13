package ru.byprogminer.Lab_Programming_Addition

import java.awt.event.KeyEvent
import java.awt.event.KeyListener

import ru.byprogminer.Lab_Programming_Addition.Player.Direction

class KeyPlayerController(player: Player): PlayerController(player), KeyListener {

    val bindings = mutableMapOf<Int, Any>(
            KeyEvent.VK_UP    to Direction.UP,
            KeyEvent.VK_LEFT  to Direction.LEFT,
            KeyEvent.VK_DOWN  to Direction.DOWN,
            KeyEvent.VK_RIGHT to Direction.RIGHT
    )

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