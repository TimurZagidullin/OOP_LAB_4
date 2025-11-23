package com.example.lab_4_oop.shapes;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
public abstract class Shape { // abstract - означает, что нельзя создать объект этого класса напрямую // Можно только создать объекты классов-наследников (Circle, Square и т.д.)
    protected double x; // x - координата X центра фигуры (горизонтальная позиция)
    protected double y; // y - координата Y центра фигуры (вертикальная позиция)
    protected double size; // size - размер фигуры (диаметр для круга, сторона для квадрата и т.д.)
    protected Color color; // color - цвет заливки фигуры
    protected boolean selected; // selected - флаг, указывающий выделена ли фигура (true) или нет (false)

    public Shape(double x, double y, double size, Color color) { // double x, y - координаты центра фигуры // double size - размер фигуры // Color color - цвет фигуры
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
        this.selected = false;
    }

    // Абстрактные методы - должны быть реализованы в каждом классе-наследнике
    // abstract - означает, что здесь только объявление, реализация будет в наследниках
    public abstract boolean isClickInShape(double px, double py); // isClickInShape - проверяет, находится ли точка клика внутри фигуры // double px, py - координаты точки клика мыши
    public abstract void draw(GraphicsContext gc); // draw - рисует фигуру на холсте
    public abstract double getMinX(); // getMinX - возвращает минимальную координату X (левая граница фигуры)
    public abstract double getMinY(); // getMinY - возвращает минимальную координату Y (верхняя граница фигуры)
    public abstract double getMaxX(); // getMaxX - возвращает максимальную координату X (правая граница фигуры)
    public abstract double getMaxY(); // getMaxY - возвращает максимальную координату Y (нижняя граница фигуры)
    
    // Метод проверяет, можно ли переместить фигуру на заданное расстояние
    public boolean canMove(double dx, double dy, double canvasWidth, double canvasHeight) {
        return getMinX() + dx >= 0 && getMinY() + dy >= 0 && getMaxX() + dx <= canvasWidth && getMaxY() + dy <= canvasHeight; // Все границы смещенных фигур по dx, dy остаются в пределах холста
    }

    //Метод перемещения фигур
    public void moveShape(double dx, double dy, double canvasWidth, double canvasHeight) {
        if (canMove(dx, dy, canvasWidth, canvasHeight)) {x += dx; y += dy;}
    }

    // Метод проверки возможности изменения размера фигуры (переопределяется в наследниках)
    public boolean canResize(double newSizeOfShape, double canvasWidth, double canvasHeight) {
        // Для простых фигур вычисляем границы напрямую
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
    public void correctPositionToBounds(double canvasWidth, double canvasHeight) { // Используется при создании фигуры, чтобы сразу поместить её в допустимые границы
        double minX = getMinX();
        double minY = getMinY();
        double maxX = getMaxX();
        double maxY = getMaxY();
        if (minX < 0) { x += -minX;} // Сдвигаем фигуру вправо на расстояние, на которое она вышла за границу слева
        if (maxX > canvasWidth) {x -= (maxX - canvasWidth);} // Сдвигаем фигуру влево на расстояние, на которое она вышла за границу
        if (minY < 0) {y += -minY;} // Сдвигаем фигуру вниз на расстояние, на которое она вышла за границу
        if (maxY > canvasHeight) {y -= (maxY - canvasHeight);} // Сдвигаем фигуру вверх на расстояние, на которое она вышла за границу
    }

    // Метод устанавливает стиль обводки фигуры (цвет и толщину линии)
    protected void setShapesStroke(GraphicsContext gc) {
        gc.setStroke(selected ? Color.BLUE : Color.BLACK);
        gc.setLineWidth(selected ? 2 : 1);
    }
}
