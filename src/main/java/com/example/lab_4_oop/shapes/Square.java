package com.example.lab_4_oop.shapes;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public class Square extends Shape {
    public Square(double x, double y, double size, Color color) { super(x, y, size, color); }

    @Override
    public boolean isClickInShape(double px, double py) {
        double half = size / 2; // половина стороны
        return px >= x - half && px <= x + half && py >= y - half && py <= y + half;
    }

    @Override
    public void draw(GraphicsContext gc) {
        double half = size / 2;
        gc.setFill(color);
        gc.fillRect(x - half, y - half, size, size);
        setShapesStroke(gc);
        gc.strokeRect(x - half, y - half, size, size);
    }

    @Override
    public double getMinX() {return x - size / 2;} // Левая граница квадрата
    @Override
    public double getMinY() {return y - size / 2;} // Верхняя граница квадрата
    @Override
    public double getMaxX() {return x + size / 2;} // Правая граница квадрата
    @Override
    public double getMaxY() {return y + size / 2;} // Нижняя граница квадрата
}
