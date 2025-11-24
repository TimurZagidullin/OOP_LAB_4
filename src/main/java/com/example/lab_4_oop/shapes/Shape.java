package com.example.lab_4_oop.shapes;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public abstract class Shape {
    protected double x;
    protected double y;
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

    public abstract boolean isClickInShape(double px, double py); // проверка, находится ли точка клика внутри фигуры и
    public abstract void draw(GraphicsContext gc); // рисует фигуру на холсте
    public abstract double getMinX(); // левая граница фигуры
    public abstract double getMinY(); // верхняя граница фигуры
    public abstract double getMaxX(); // правая граница фигуры
    public abstract double getMaxY(); // нижняя граница фигуры

    // можно ли переместить
    public boolean canMove(double dx, double dy, double canvasWidth, double canvasHeight) {
        return getMinX() + dx >= 0 && getMinY() + dy >= 0 && getMaxX() + dx <= canvasWidth && getMaxY() + dy <= canvasHeight; // Все границы смещенных фигур по dx, dy остаются в пределах холста
    }

    //метод перемещения фигур
    public void moveShape(double dx, double dy, double canvasWidth, double canvasHeight) {
        if (canMove(dx, dy, canvasWidth, canvasHeight)) {x += dx; y += dy;}
    }

    // Метод проверки возможности изменения размера фигуры
    public boolean canResize(double newSizeOfShape, double canvasWidth, double canvasHeight) {
        double half = newSizeOfShape / 2;
        double minX = x - half;
        double minY = y - half;
        double maxX = x + half;
        double maxY = y + half;
        return minX >= 0 && minY >= 0 && maxX <= canvasWidth && maxY <= canvasHeight;
    }

    // Метод для изменения размера фигуры
    public void resizeShape(double newSizeOfShape, double canvasWidth, double canvasHeight) {
        if (canResize(newSizeOfShape, canvasWidth, canvasHeight)) { size = newSizeOfShape; }
    }

    // Метод устанавливает флаг выделения фигуры
    public void shapeSetSelected(boolean selected) {this.selected = selected;}

    // Метод проверяет, выделена ли фигура
    public boolean isSelected() { return selected;}

    // Метод устанавливает цвет фигуры
    public void setColor(Color color) { this.color = color;}

    // Методы получают координаты X Y центра фигуры
    public double getXcentrOfShape() {return x;}
    public double getYcentrOfShape() {return y;}

    // Метод получает размер фигуры
    public double getSizeOfShape() { return size;}

    // Метод корректирует позицию фигуры, чтобы она не выходила за границы
    public void correctPositionToBounds(double canvasWidth, double canvasHeight) {
        double minX = getMinX();
        double minY = getMinY();
        double maxX = getMaxX();
        double maxY = getMaxY();
        if (minX < 0) { x += -minX;} // Сдвиг фигуры вправо на расстояние, на которое она вышла за границу слева
        if (maxX > canvasWidth) {x -= (maxX - canvasWidth);} // Сдвиг фигуры влево
        if (minY < 0) {y += -minY;} // Сдвиг фигуры вниз
        if (maxY > canvasHeight) {y -= (maxY - canvasHeight);} // Сдвиг фигуры вверх
    }

    protected void setShapesStroke(GraphicsContext gc) {
        gc.setStroke(selected ? Color.BLUE : Color.BLACK);
        gc.setLineWidth(selected ? 2 : 1);
    }
}
