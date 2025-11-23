package com.example.lab_4_oop.shapes;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public class Line extends Shape {
    private double x2, y2; // 2 точка
    public Line(double x1, double y1, double x2, double y2, Color color) {
        super(x1, y1, Math.hypot(x2 - x1, y2 - y1), color);
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public boolean isClickInShape(double px, double py) {
        double distance = distanceToLineSegment(px, py, x, y, x2, y2);
        return distance <= 5; // клик в пределах 5 пикселей от линии
    }

    private double distanceToLineSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double A = px - x1; // вектор от начальной точки до точки клика
        double B = py - y1;
        double C = x2 - x1; // вектор отрезка
        double D = y2 - y1;
        double dot = A * C + B * D; // скалярное произведение
        double lenSq = C * C + D * D; // квадрат длины отрезка
        double param = lenSq != 0 ? dot / lenSq : -1; // параметр проекции на отрезок
        double xx, yy;
        if (param < 0) { xx = x1; yy = y1; } // ближайшая точка - начальная
        else if (param > 1) { xx = x2; yy = y2; } // ближайшая точка - конечная
        else { xx = x1 + param * C; yy = y1 + param * D; } // ближайшая точка на отрезке
        double dx = px - xx;
        double dy = py - yy;
        return Math.hypot(dx, dy); // расстояние до ближайшей точки
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(color);
        gc.setLineWidth(selected ? 4 : 2);
        gc.strokeLine(x, y, x2, y2);
        if (selected) {
            setShapesStroke(gc);
            gc.strokeLine(x, y, x2, y2);
        }
    }

    @Override
    public double getMinX() {return Math.min(x, x2);} // Левая граница линии
    @Override
    public double getMinY() {return Math.min(y, y2);} // Верхняя граница линии
    @Override
    public double getMaxX() {return Math.max(x, x2);} // Правая граница линии
    @Override
    public double getMaxY() {return Math.max(y, y2);} // Нижняя граница линии

    @Override
    public boolean canMove(double dx, double dy, double canvasWidth, double canvasHeight) {
        double newX = x + dx;
        double newY = y + dy;
        double newX2 = x2 + dx;
        double newY2 = y2 + dy;
        return checkBounds(newX, newY, newX2, newY2, canvasWidth, canvasHeight);
    }

    @Override
    public void moveShape(double dx, double dy, double canvasWidth, double canvasHeight) {
        if (canMove(dx, dy, canvasWidth, canvasHeight)) {
            x += dx;
            y += dy;
            x2 += dx;
            y2 += dy;
        }
    }

    @Override
    public boolean canResize(double newSize, double canvasWidth, double canvasHeight) {
        double currentLength = Math.hypot(x2 - x, y2 - y);
        if (currentLength == 0) return true; // линия - точка
        double scale = newSize / currentLength;
        double newX2 = x + (x2 - x) * scale;
        double newY2 = y + (y2 - y) * scale;
        return checkBounds(x, y, newX2, newY2, canvasWidth, canvasHeight);
    }

    @Override
    public void resizeShape(double newSize, double canvasWidth, double canvasHeight) {
        if (canResize(newSize, canvasWidth, canvasHeight)) {
            double currentLength = Math.hypot(x2 - x, y2 - y);
            if (currentLength == 0) {
                x2 = x + newSize; // горизонтальная линия
                y2 = y;
            } else {
                double scale = newSize / currentLength;
                x2 = x + (x2 - x) * scale;
                y2 = y + (y2 - y) * scale;
            }
            size = newSize;
        }
    }

    public void setX2(double x2) { this.x2 = x2; }
    public void setY2(double y2) { this.y2 = y2; }
    public void setSize(double size) { this.size = size; }
    public void updateSize() { this.size = Math.hypot(x2 - x, y2 - y); }
    
    private boolean checkBounds(double x, double y, double canvasWidth, double canvasHeight) {
        return x >= 0 && y >= 0 && x <= canvasWidth && y <= canvasHeight;
    }

    private boolean checkBounds(double x1, double y1, double x2, double y2, double canvasWidth, double canvasHeight) {
        return checkBounds(x1, y1, canvasWidth, canvasHeight) && checkBounds(x2, y2, canvasWidth, canvasHeight);
    }
    
    @Override
    public void correctPositionToBounds(double canvasWidth, double canvasHeight) {
        double minX = getMinX();
        double minY = getMinY();
        double maxX = getMaxX();
        double maxY = getMaxY();
        double dx = 0, dy = 0;
        if (minX < 0) { dx = -minX; } // сдвигаем вправо
        if (maxX > canvasWidth) { dx = canvasWidth - maxX; } // сдвигаем влево
        if (minY < 0) { dy = -minY; } // сдвигаем вниз
        if (maxY > canvasHeight) { dy = canvasHeight - maxY; } // сдвигаем вверх
        x += dx;
        y += dy;
        x2 += dx;
        y2 += dy;
        size = Math.hypot(x2 - x, y2 - y);
    }

}
