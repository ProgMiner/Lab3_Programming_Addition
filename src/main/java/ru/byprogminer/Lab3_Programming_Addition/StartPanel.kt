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

package ru.byprogminer.Lab3_Programming_Addition

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent

import javax.swing.*

class StartPanel(
        gap: Int = 2,
        private val callback: (Int, Int) -> Unit
): JPanel(GridBagLayout()) {

    private val widthLabel = JLabel("Width:", JLabel.RIGHT)
    private val widthField = JTextField("20", 5)
    private val heightLabel = JLabel("Height:", JLabel.RIGHT)
    private val heightField = JTextField("12", 5)
    private val startButton = JButton("Start game")

    init {
        border = BorderFactory.createEmptyBorder(gap, gap, gap, gap)

        val font = font.deriveFont(20F)

        widthLabel.font = font
        add(widthLabel, GridBagConstraints(0, 0, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 5), 0, 0))

        widthField.font = font
        widthField.addActionListener(this::actionPerformed)
        add(widthField, GridBagConstraints(1, 0, 1, 1, 2.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 0), 0, 0))

        heightLabel.font = font
        add(heightLabel, GridBagConstraints(0, 1, 1, 1, .0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(0, 0, 0, 5), 0, 0))

        heightField.font = font
        heightField.addActionListener(this::actionPerformed)
        add(heightField, GridBagConstraints(1, 1, 1, 1, 2.0, 1.0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, Insets(gap, 0, 0, 0), 0, 0))

        startButton.font = font
        startButton.addActionListener(this::actionPerformed)
        add(startButton, GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, Insets(gap, 0, 0, 0), 0, 0))
    }

    private fun actionPerformed(event: ActionEvent) {
        try {
            val width = widthField.text.toInt()
            val height = heightField.text.toInt()

            if (width !in 1..100 || height !in 1..100) {
                JOptionPane.showMessageDialog(this, "Width and height must be in (0, 100]!", APP_NAME, JOptionPane.WARNING_MESSAGE)
                return
            }

            callback(width, height)
        } catch (e: NumberFormatException) {
            JOptionPane.showMessageDialog(this, "${e.javaClass.canonicalName}: ${e.localizedMessage}", APP_NAME, JOptionPane.ERROR_MESSAGE)

            e.printStackTrace()
        }
    }
}