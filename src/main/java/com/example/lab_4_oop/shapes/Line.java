package com.example.lab_4_oop.shapes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public class Line extends Shape {
    private double x2, y2;

    public Line(double x1, double y1, double x2, double y2, Color color) {
        super(x1, y1, Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)), color);
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public boolean isClickInShape(double px, double py) {
        double distance = distanceToLineSegment(px, py, x, y, x2, y2);
        return distance <= 5;
    }

    private double distanceToLineSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1;
        double B = py - y1;
        double C = x2 - x1;
        double D = y2 - y1;
        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        double param = lenSq != 0 ? dot / lenSq : -1;
        double xx, yy;
        if (param < 0) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }
        double dx = px - xx;
        double dy = py - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.setLineWidth(selected ? 4 : 2);
        gc.strokeLine(x, y, x2, y2);
        
        if (selected) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(3);
            gc.strokeLine(x, y, x2, y2);
            gc.setFill(Color.BLUE);
            gc.fillOval(x - 4, y - 4, 8, 8);
            gc.fillOval(x2 - 4, y2 - 4, 8, 8);
        }
    }

    @Override
    public double getMinX() {
        return Math.min(x, x2);
    }

    @Override
    public double getMinY() {
        return Math.min(y, y2);
    }

    @Override
    public double getMaxX() {
        return Math.max(x, x2);
    }

    @Override
    public double getMaxY() {
        return Math.max(y, y2);
    }

    @Override
    public void move(double dx, double dy, double canvasWidth, double canvasHeight) {
        double newX = x + dx;
        double newY = y + dy;
        double newX2 = x2 + dx;
        double newY2 = y2 + dy;
        
        if (newX >= 0 && newY >= 0 && newX2 <= canvasWidth && newY2 <= canvasHeight &&
            newX <= canvasWidth && newY <= canvasHeight && newX2 >= 0 && newY2 >= 0) {
            x = newX;
            y = newY;
            x2 = newX2;
            y2 = newY2;
        }
    }

    @Override
    public boolean canResize(double newSize, double canvasWidth, double canvasHeight) {
        double currentLength = Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
        if (currentLength == 0) return true;
        double scale = newSize / currentLength;
        double newX2 = x + (x2 - x) * scale;
        double newY2 = y + (y2 - y) * scale;
        return newX2 >= 0 && newY2 >= 0 && newX2 <= canvasWidth && newY2 <= canvasHeight &&
               x >= 0 && y >= 0 && x <= canvasWidth && y <= canvasHeight;
    }

    @Override
    public void resize(double newSize, double canvasWidth, double canvasHeight) {
        if (canResize(newSize, canvasWidth, canvasHeight)) {
            double currentLength = Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
            if (currentLength == 0) {
                x2 = x + newSize;
                y2 = y;
            } else {
                double scale = newSize / currentLength;
                x2 = x + (x2 - x) * scale;
                y2 = y + (y2 - y) * scale;
            }
            size = newSize;
        }
    }

    public double getX2() {
        return x2;
    }

    public double getY2() {
        return y2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }
    
    public void setSize(double size) {
        this.size = size;
    }
    
    @Override
    public void constrainToBounds(double canvasWidth, double canvasHeight) {
        // Корректируем первую точку
        if (x < 0) x = 0;
        if (x > canvasWidth) x = canvasWidth;
        if (y < 0) y = 0;
        if (y > canvasHeight) y = canvasHeight;
        
        // Корректируем вторую точку
        if (x2 < 0) x2 = 0;
        if (x2 > canvasWidth) x2 = canvasWidth;
        if (y2 < 0) y2 = 0;
        if (y2 > canvasHeight) y2 = canvasHeight;
        
        // Обновляем размер линии после корректировки
        size = Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
    }
}
