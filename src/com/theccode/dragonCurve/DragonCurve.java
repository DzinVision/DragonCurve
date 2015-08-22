package com.theccode.dragonCurve;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;

public class DragonCurve extends Canvas implements Runnable {

    private ArrayList<Integer> turnSequence;
    private double startingAngle, sideLength;
    private Thread thread;
    private boolean running;
    public JFrame frame;
    public static int width = 900;
    public static int height = width / 16 * 9;
    public static String title = "Dragon Curve";
    private Keyboard key;
    public JSlider slider;
    private boolean changed = false;
    private JPanel panel = new JPanel();

    public DragonCurve(int iterations) {

        //setPreferredSize(new Dimension(width, height + 20));


        frame = new JFrame();
        frame.setSize(width, height + 100);
        panel = new JPanel();
        frame.add(panel);

        this.setBounds(0, 0, width, height);
        panel.add(this);

        slider = new JSlider(1, 16, 14);
        slider.setMajorTickSpacing(5);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setChanged(true);
            }
        });
        slider.setBounds(0, height + 1, width, 20);
        panel.add(slider);
        key = new Keyboard();

        addKeyListener(key);

        calculatePath(iterations);
    }

    private synchronized boolean getChanged() {
        return changed;
    }

    private synchronized void setChanged(boolean changed) {
        this.changed = changed;
    }

    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        final double ns = 1000000000.0 / 60.0;
        double delta = 0;
        int frames = 0;
        int updates = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            requestFocus();
            while (delta >= 1) {
                update();
                updates++;
                delta--;
            }

            render();
            frames++;

            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                frame.setTitle(title + "  |  " + updates + " ups, " + frames + " fps");
                frames = 0;
                updates = 0;
            }
        }

        stop();
    }

    int x = 290, y = 330;
    float zoom = 1.0f;

    public void update() {
        key.update();
        if (key.up) y -= 3;
        if (key.down) y += 3;
        if (key.left) x -= 3;
        if (key.right) x += 3;
        if (key.q && zoom > 0.3) zoom -= 0.08;
        if (key.w) zoom += 0.08;

        if (getChanged()) {
            setChanged(false);
            calculatePath(slider.getValue());
        }
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();
        paint(g);
        g.setColor(Color.BLACK);
        double angle = startingAngle;
        int x1 = x, y1 = y;
        int x2 = x1 + (int)(Math.cos(angle) * sideLength * zoom);
        int y2 = y1 + (int)(Math.sin(angle) * sideLength * zoom);
        g.drawLine(x1, y1, x2, y2);
        x1 = x2;
        y1 = y2;

        for (Integer turn : turnSequence) {
            angle += turn * (Math.PI / 2.0);
            x2 = x1 + (int)(Math.cos(angle) * sideLength * zoom);
            y2 = y1 + (int)(Math.sin(angle) * sideLength * zoom);
            g.drawLine(x1, y1, x2, y2);
            x1 = x2;
            y1 = y2;
        }

        g.dispose();
        bs.show();
    }

    private synchronized void calculatePath(int iterations) {
        turnSequence = new ArrayList<Integer>();

        startingAngle = -iterations * (Math.PI / 4.0);
        sideLength = 400 / Math.pow(2, iterations / 2.0);

        for (int i = 0; i < iterations; i++) {
            ArrayList<Integer> copy = new ArrayList<Integer>(turnSequence);
            Collections.reverse(copy);
            turnSequence.add(1);
            for (Integer turn : copy)
                turnSequence.add(-turn);
        }

    }

    public static void main(String[] args) {
        DragonCurve dragonCurve = new DragonCurve(14);
        dragonCurve.frame.setResizable(false);
        dragonCurve.frame.setTitle(DragonCurve.title);
        dragonCurve.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        dragonCurve.frame.setLocationRelativeTo(null);
        dragonCurve.frame.setVisible(true);

        dragonCurve.start();
    }
}