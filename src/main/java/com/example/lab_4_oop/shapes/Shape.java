package com.example.lab_4_oop.shapes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public abstract class Shape {
    protected double x, y;
    protected double size;
    protected Color color;
    protected boolean selected;

    public Shape(double x, double y, double size, Color color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
        this.selected = false;
    }

    public abstract boolean isClickInShape(double px, double py);
    public abstract void draw(GraphicsContext gc);
    public abstract double getMinX();
    public abstract double getMinY();
    public abstract double getMaxX();
    public abstract double getMaxY();

    //проверка можно ли перемещать без выхода за границу
    public boolean canMove(double dx, double dy, double canvasWidth, double canvasHeight) {
        double newMinX = getMinX() + dx;
        double newMinY = getMinY() + dy;
        double newMaxX = getMaxX() + dx;
        double newMaxY = getMaxY() + dy;
        return newMinX >= 0 && newMinY >= 0 && 
               newMaxX <= canvasWidth && newMaxY <= canvasHeight;
    }

    public void move(double dx, double dy, double canvasWidth, double canvasHeight) {
        if (canMove(dx, dy, canvasWidth, canvasHeight)) {
            x += dx;
            y += dy;
        }
    }

    //проверка можно ли менять размер
    public boolean canResize(double newSize, double canvasWidth, double canvasHeight) {
        double tempSize = size;
        size = newSize;
        boolean can = getMinX() >= 0 && getMinY() >= 0 && 
                     getMaxX() <= canvasWidth && getMaxY() <= canvasHeight;
        size = tempSize;
        return can;
    }

    public void resize(double newSize, double canvasWidth, double canvasHeight) {
        if (canResize(newSize, canvasWidth, canvasHeight)) {
            size = newSize;
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getSize() {
        return size;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
    

    //Автоматическая подгонка фигуры под границу
    public void constrainToBounds(double canvasWidth, double canvasHeight) {
        double minX = getMinX();
        double minY = getMinY();
        double maxX = getMaxX();
        double maxY = getMaxY();

        if (minX < 0) {
            x += -minX;
        }

        if (maxX > canvasWidth) {
            x -= (maxX - canvasWidth);
        }

        if (minY < 0) {
            y += -minY;
        }

        if (maxY > canvasHeight) {
            y -= (maxY - canvasHeight);
        }
    }
}

