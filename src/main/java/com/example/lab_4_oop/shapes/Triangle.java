package com.example.lab_4_oop.shapes;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public class Triangle extends Shape {
    public Triangle(double x, double y, double size, Color color) { super(x, y, size, color); }

    @Override
    public boolean isClickInShape(double px, double py) {
        double half = size / 2;
        double h = size * Math.sqrt(3) / 2; // высота треугольника
        double x1 = x, y1 = y - h * 2 / 3; // верхняя вершина
        double x2 = x - half, y2 = y + h / 3; // левая нижняя вершина
        double x3 = x + half, y3 = y + h / 3; // правая нижняя вершина
        double d1 = sign(px, py, x1, y1, x2, y2);
        double d2 = sign(px, py, x2, y2, x3, y3);
        double d3 = sign(px, py, x3, y3, x1, y1);
        boolean hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0);
        boolean hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0);
        return !(hasNeg && hasPos); // точка внутри, если все знаки одного типа
    }

    private double sign(double px, double py, double x1, double y1, double x2, double y2) {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2); // векторное произведение
    }

    @Override
    public void draw(GraphicsContext gc) {
        double half = size / 2;
        double h = size * Math.sqrt(3) / 2;
        double[] xPoints = {x, x - half, x + half};
        double[] yPoints = {y - h * 2 / 3, y + h / 3, y + h / 3};
        gc.setFill(color);
        gc.fillPolygon(xPoints, yPoints, 3);
        setShapesStroke(gc);
        gc.strokePolygon(xPoints, yPoints, 3);
    }

    @Override
    public double getMinX() {return x - size / 2;} // Левая граница треугольника
    @Override
    public double getMinY() {
        double h = size * Math.sqrt(3) / 2;
        return y - h * 2 / 3; // Верхняя граница треугольника
    }
    @Override
    public double getMaxX() {return x + size / 2;} // Правая граница треугольника
    @Override
    public double getMaxY() {
        double h = size * Math.sqrt(3) / 2;
        return y + h / 3; // Нижняя граница треугольника
    }

    @Override
    public boolean canResize(double newSize, double canvasWidth, double canvasHeight) {
        double half = newSize / 2;
        double h = newSize * Math.sqrt(3) / 2;
        double minX = x - half;
        double minY = y - h * 2 / 3;
        double maxX = x + half;
        double maxY = y + h / 3;
        return minX >= 0 && minY >= 0 && maxX <= canvasWidth && maxY <= canvasHeight;
    }
}
