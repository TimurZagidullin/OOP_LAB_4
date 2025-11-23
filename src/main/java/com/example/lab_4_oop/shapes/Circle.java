package com.example.lab_4_oop.shapes;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public class Circle extends Shape {
    public Circle(double x, double y, double size, Color color) { super(x, y, size, color); }

    @Override
    public boolean isClickInShape(double px, double py) {
        double dx = px - x; // Расстояние от центра до точки клика
        double dy = py - y;
        return dx * dx + dy * dy <= (size / 2) * (size / 2); // квадрат расстояния от центтра до точки <= квадрату радиуса
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x - size / 2, y - size / 2, size, size);
        setShapesStroke(gc);
        gc.strokeOval(x - size / 2, y - size / 2, size, size);
    }

    @Override
    public double getMinX() {return x - size / 2;} // Левая граница круга
    @Override
    public double getMinY() {return y - size / 2;}// Верхняя граница круга
    @Override
    public double getMaxX() {return x + size / 2;}// Правая граница круга
    @Override
    public double getMaxY() {return y + size / 2;} // Нижняя граница круга
}
