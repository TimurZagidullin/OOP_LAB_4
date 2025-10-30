package com.example.lab_4_oop.shapes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public class Ellipse extends Shape {
    private double widthRatio = 1.5;
    private double heightRatio = 1.0;

    public Ellipse(double x, double y, double size, Color color) {
        super(x, y, size, color);
    }

    @Override
    public boolean isClickInShape(double px, double py) {
        double a = size * widthRatio / 2;
        double b = size * heightRatio / 2;
        double dx = (px - x) / a;
        double dy = (py - y) / b;
        return dx * dx + dy * dy <= 1;
    }

    @Override
    public void draw(GraphicsContext gc) {
        double width = size * widthRatio;
        double height = size * heightRatio;
        gc.setFill(color);
        gc.fillOval(x - width / 2, y - height / 2, width, height);
        
        gc.setStroke(selected ? Color.BLUE : Color.BLACK);
        gc.setLineWidth(selected ? 3 : 1);
        gc.strokeOval(x - width / 2, y - height / 2, width, height);
    }

    @Override
    public double getMinX() {
        return x - size * widthRatio / 2;
    }

    @Override
    public double getMinY() {
        return y - size * heightRatio / 2;
    }

    @Override
    public double getMaxX() {
        return x + size * widthRatio / 2;
    }

    @Override
    public double getMaxY() {
        return y + size * heightRatio / 2;
    }
}

