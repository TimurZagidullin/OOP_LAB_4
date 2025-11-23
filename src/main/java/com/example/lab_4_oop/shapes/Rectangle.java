package com.example.lab_4_oop.shapes;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public class Rectangle extends Shape {
    private double widthRatio = 1.5; // коэффициент ширины
    private double heightRatio = 1.0; // коэффициент высоты

    public Rectangle(double x, double y, double size, Color color) { super(x, y, size, color); }

    @Override
    public boolean isClickInShape(double px, double py) {
        double width = size * widthRatio;
        double height = size * heightRatio;
        return px >= x - width / 2 && px <= x + width / 2 && py >= y - height / 2 && py <= y + height / 2;
    }

    @Override
    public void draw(GraphicsContext gc) {
        double width = size * widthRatio;
        double height = size * heightRatio;
        gc.setFill(color);
        gc.fillRect(x - width / 2, y - height / 2, width, height);
        setShapesStroke(gc);
        gc.strokeRect(x - width / 2, y - height / 2, width, height);
    }

    @Override
    public double getMinX() {return x - size * widthRatio / 2;} // Левая граница прямоугольника
    @Override
    public double getMinY() {return y - size * heightRatio / 2;} // Верхняя граница прямоугольника
    @Override
    public double getMaxX() {return x + size * widthRatio / 2;} // Правая граница прямоугольника
    @Override
    public double getMaxY() {return y + size * heightRatio / 2;} // Нижняя граница прямоугольника

    @Override
    public boolean canResize(double newSize, double canvasWidth, double canvasHeight) {
        double width = newSize * widthRatio;
        double height = newSize * heightRatio;
        double minX = x - width / 2;
        double minY = y - height / 2;
        double maxX = x + width / 2;
        double maxY = y + height / 2;
        return minX >= 0 && minY >= 0 && maxX <= canvasWidth && maxY <= canvasHeight;
    }
}
