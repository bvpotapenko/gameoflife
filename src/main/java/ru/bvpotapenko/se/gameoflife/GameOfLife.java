package ru.bvpotapenko.se.gameoflife;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * Conway's Game of Life
 */
public class GameOfLife {
    final String NAME_OF_GAME = "Conway's Game of Life";
    final int LIFE_SIZE = 50;
    final int POINT_RADIUS = 20;
    final int FIELD_SIZE = LIFE_SIZE * POINT_RADIUS + 7;
    final int BTN_PANEL_HEIGHT = 58 + 4;
    final int START_LOCATION = 10;
    boolean[][] lifeGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    boolean[][] nextGeneration = new boolean[LIFE_SIZE][LIFE_SIZE];
    boolean[][] tmp;

    Random random = new Random();
    int countGeneration = 0;
    int showDelay = 500;
    int showDelayStep = 50;
    volatile boolean goNextGeneration = false; // fixed the problem in 64-bit JVM added volatile
    boolean useColors = false;
    boolean showGrid = true;

    JFrame frame;
    Canvas canvasPanel;

    public static void main(String[] args) {
        GameOfLife game = new GameOfLife();
        game.initiate();
        game.go();
    }

    void go() {
        while (true) {
            if (goNextGeneration) {
                processOfLife();
                canvasPanel.repaint();
                try {
                    Thread.sleep(showDelay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void initiate() {
        frame = new JFrame(NAME_OF_GAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FIELD_SIZE, FIELD_SIZE + BTN_PANEL_HEIGHT);
        frame.setLocation(START_LOCATION, START_LOCATION);
        frame.setResizable(false);

        canvasPanel = new Canvas();
        canvasPanel.setBackground(Color.WHITE);
        canvasPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                int x = e.getX() / POINT_RADIUS;
                int y = e.getY() / POINT_RADIUS;
                lifeGeneration[x][y] = !lifeGeneration[x][y];
                canvasPanel.repaint();
            }
        });

        //Buttons
        JButton fillBtn = new JButton("Fill");
        fillBtn.addActionListener(new FillButtonListener());

        // get the next generation
        JButton stepBtn = new JButton("Step");
        stepBtn.setToolTipText("Show next generation");
        stepBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processOfLife();
                canvasPanel.repaint();
            }
        });

        //Use colors btn
        JButton colorBtn = new JButton("Color");
        colorBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                useColors = !useColors;
                canvasPanel.repaint();
            }
        });

        JButton playBtn = new JButton("|>");
        playBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goNextGeneration = !goNextGeneration;
                canvasPanel.repaint();
                ;
            }
        });

        //Button panel
        JPanel btnPanel = new JPanel();
        btnPanel.add(fillBtn);
        btnPanel.add(stepBtn);
        btnPanel.add(playBtn);
        btnPanel.add(colorBtn);


        frame.getContentPane().add(BorderLayout.CENTER, canvasPanel);
        frame.getContentPane().add(BorderLayout.SOUTH, btnPanel);
        frame.setVisible(true);
    }

    // randomly fill cells
    public class FillButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            countGeneration = 1;
            for (int x = 0; x < LIFE_SIZE; x++) {
                for (int y = 0; y < LIFE_SIZE; y++) {
                    lifeGeneration[x][y] = random.nextBoolean();
                }
            }
            canvasPanel.repaint();
        }
    }

    // count the number of neighbors
    int countNeighbors(int x, int y) {
        int count = 0;
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                int nX = x + dx;
                int nY = y + dy;
                nX = (nX < 0) ? LIFE_SIZE - 1 : nX;
                nY = (nY < 0) ? LIFE_SIZE - 1 : nY;
                nX = (nX > LIFE_SIZE - 1) ? 0 : nX;
                nY = (nY > LIFE_SIZE - 1) ? 0 : nY;
                count += (lifeGeneration[nX][nY]) ? 1 : 0;
            }
        }
        if (lifeGeneration[x][y]) {
            count--;
        }
        return count;
    }

    // the main process of life
    void processOfLife() {
        for (int x = 0; x < LIFE_SIZE; x++) {
            for (int y = 0; y < LIFE_SIZE; y++) {
                int count = countNeighbors(x, y);
                nextGeneration[x][y] = lifeGeneration[x][y];
                // if there are 3 live neighbors around empty cells - the cell will become populated
                nextGeneration[x][y] = (count == 3) ? true : nextGeneration[x][y];
                // if cell has less than 2 or greater than 3 neighbors - it will die
                nextGeneration[x][y] = ((count < 2) || (count > 3)) ? false : nextGeneration[x][y];
            }
        }
        // swap generations
        tmp = nextGeneration;
        nextGeneration = lifeGeneration;
        lifeGeneration = tmp;

        countGeneration++;
    }

    public class Canvas extends JPanel {

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            for (int x = 0; x < LIFE_SIZE; x++) {
                for (int y = 0; y < LIFE_SIZE; y++) {
                    if (lifeGeneration[x][y]) {
                        if (useColors) {
                            int count = countNeighbors(x, y);
                            g.setColor(((count < 2) || (count > 3)) ? Color.red : Color.blue);
                        } else {
                            g.setColor(Color.black);
                        }
                        //g.fillOval(x*POINT_RADIUS, y*POINT_RADIUS, POINT_RADIUS, POINT_RADIUS);
                        g.fillRect(x * POINT_RADIUS, y * POINT_RADIUS, POINT_RADIUS, POINT_RADIUS);
                    } else {
                        if (useColors) {
                            int count = countNeighbors(x, y);
                            if (count == 3) {
                                g.setColor(new Color(225, 255, 235));
                                //g.fillOval(x*POINT_RADIUS, y*POINT_RADIUS, POINT_RADIUS, POINT_RADIUS);
                                g.fillRect(x * POINT_RADIUS, y * POINT_RADIUS, POINT_RADIUS, POINT_RADIUS);
                            }
                        }
                    }
                    if (showGrid) {
                        g.setColor(Color.lightGray);
                        g.drawLine((x + 1) * POINT_RADIUS - 1, (y + 1) * POINT_RADIUS, (x + 1) * POINT_RADIUS + 1, (y + 1) * POINT_RADIUS);
                        g.drawLine((x + 1) * POINT_RADIUS, (y + 1) * POINT_RADIUS - 1, (x + 1) * POINT_RADIUS, (y + 1) * POINT_RADIUS + 1);
                    }
                }
            }
            frame.setTitle(NAME_OF_GAME + " : " + countGeneration);
        }
    }
}
