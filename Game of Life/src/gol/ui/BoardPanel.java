package gol.ui;

import gol.GameOfLifeModel;
import gol.Pattern;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BoardPanel extends JComponent {
    private final GameOfLifeModel model;

    private int cellSize = 14;
    private int offsetX = 0, offsetY = 0;

    // выбранный паттерн для «штампа»; null = обычный «кистью по одной клетке»
    private Pattern currentPattern = null;

    public void setCurrentPattern(Pattern p) { this.currentPattern = p; }

    public BoardPanel(GameOfLifeModel model) {
        this.model = model;

        setBackground(new Color(0x111111));

        MouseAdapter mouse = new MouseAdapter() {
            private boolean drawing = false;    //рисую ли я сейчас?
            private boolean drawValue = true;   //что именно я рисую?

            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isRightMouseButton(e)) {
                    drawing = true;
                    drawValue = SwingUtilities.isLeftMouseButton(e);
                    applyAt(e.getX(), e.getY(), true);
                }
            }
            public void mouseDragged(MouseEvent e) {
                if (drawing) applyAt(e.getX(), e.getY(), false);
            }
            public void mouseReleased(MouseEvent e) { drawing = false; }

            private void applyAt(int px, int py, boolean firstClick) {
                Point gp = toGrid(px, py);
                if (currentPattern != null) {
                    // ставим один раз на клик (drag игнорим для паттерна)
                    if (firstClick) model.stampPattern(currentPattern, gp.x, gp.y, drawValue);
                    repaint();
                } else {
                    model.setCell(gp.x, gp.y, drawValue);
                    repaintCell(gp.x, gp.y);
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                cellSize = Math.max(4, Math.min(40, cellSize + (e.getWheelRotation() < 0 ? 1 : -1)));
                repaint();
            } else {
                if (e.isShiftDown()) offsetX += e.getWheelRotation();
                else offsetY += e.getWheelRotation();
                repaint();
            }
        });



    }
    public Rectangle getVisibleGridBounds() {
        int gx0 = toGrid(0, 0).x;
        int gy0 = toGrid(0, 0).y;
        int gx1 = toGrid(getWidth(), getHeight()).x;
        int gy1 = toGrid(getWidth(), getHeight()).y;
        int minX = Math.min(gx0, gx1), maxX = Math.max(gx0, gx1);
        int minY = Math.min(gy0, gy1), maxY = Math.max(gy0, gy1);
        return new Rectangle(minX, minY, (maxX - minX), (maxY - minY));
    }



     public Dimension getPreferredSize() { return new Dimension(1600, 1200); }

     protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        drawGrid(g2);

        for (long p : model.getAliveSnapshot()) {
            int x = GameOfLifeModel.unpackX(p);
            int y = GameOfLifeModel.unpackY(p);
            Point pt = toPixel(x, y);
            if (pt.x + cellSize < 0 || pt.y + cellSize < 0 || pt.x >= getWidth() || pt.y >= getHeight()) continue;

            int age = model.getAge(p);
            g2.setColor(colorByAge(age));
            g2.fillRect(pt.x + 1, pt.y + 1, cellSize - 2, cellSize - 2);
        }
        // красная рамка, если топология = TORUS
        if (model.getTopology() == gol.GameOfLifeModel.Topology.TORUS) {
            int w = model.getTorusWidth();
            int h = model.getTorusHeight();
            Point a = toPixel(0, 0);
            Point b = toPixel(w, h);
            int x = Math.min(a.x, b.x);
            int y = Math.min(a.y, b.y);
            int pw = Math.abs(b.x - a.x);
            int ph = Math.abs(b.y - a.y);

            g2.setColor(new java.awt.Color(255, 0, 0, 255));
            g2.setStroke(new java.awt.BasicStroke(2f));
            g2.drawRect(x, y, pw, ph);
        }

        g2.dispose();

    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 28));
        int w = getWidth(), h = getHeight();
        int startX = Math.floorDiv(-toPixel(0,0).x, cellSize) * cellSize + toPixel(0,0).x;
        int startY = Math.floorDiv(-toPixel(0,0).y, cellSize) * cellSize + toPixel(0,0).y;
        for (int x = startX; x < w; x += cellSize) g2.drawLine(x, 0, x, h);
        for (int y = startY; y < h; y += cellSize) g2.drawLine(0, y, w, y);
    }

    private Color colorByAge(int age) {
        age = Math.min(age, 20);
        int v = 255 - (int)(Math.log(1 + age) / Math.log(21) * 160);
        return new Color(80, v, 220);
    }

    // преобразования координат
    private Point toGrid(int px, int py) {
        int gx = Math.floorDiv(px, cellSize) + offsetX;
        int gy = Math.floorDiv(py, cellSize) + offsetY;
        return new Point(gx, gy);
    }
    private Point toPixel(int gx, int gy) {
        int px = (gx - offsetX) * cellSize;
        int py = (gy - offsetY) * cellSize;
        return new Point(px, py);
    }

    private void repaintCell(int gx, int gy) {
        Point p = toPixel(gx, gy);
        repaint(new Rectangle(p.x, p.y, cellSize, cellSize));
    }
}
