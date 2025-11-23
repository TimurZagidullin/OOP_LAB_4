package com.example.lab_4_oop;

import com.example.lab_4_oop.shapes.*;
import javafx.scene.paint.Color;
public class Model { // Model хранит данные, Controller обрабатывает действия пользователя
    private final Storage<Shape> shapes = new Storage<>();
    private Color colorNow = Color.RED; // Приватное поле colorNow - текущий выбранный цвет для новых фигур
    private Runnable onModelChanged = () -> {}; // Используется для уведомления Controller о том, что нужно перерисовать экран

    // Метод установки функции-обработчика изменений
    public void setOnModelChanged(Runnable callback) { // функция вызывается при изменении модели
        this.onModelChanged = callback;
    }

    // Метод добавления фигур в хранилище
    public void addShapeToStorage(Shape shape) {
        shapes.add(shape);
        notifyChange();
    }

    // Метод удаления фигур из хранилища
    public void removeShapeFromStorage(Shape shape) {
        shapes.remove(shape);
        notifyChange();
    }

    // Метод удаления выделенных фигур
    public void removeSelected() {
        java.util.ArrayList<Shape> shapesToRemove = new java.util.ArrayList<Shape>(); // Список фигур для удаления
        for (Shape shape : shapes) {
            if (shape.isSelected()) {shapesToRemove.add(shape);}
        }
        for (Shape shape : shapesToRemove) {
            removeShapeFromStorage(shape);
        }
    }

    // Метод для выделения или снятия выделения с фигуры
    public void selectShape(Shape shape, boolean multiSelect) {
        if (!multiSelect) { // Не множественное
            if (shape.isSelected()) {
                shape.shapeSetSelected(false);
                notifyChange();
                return;
            }
            clearSelection(); // Снтие всех выделенных
        }
        if (shape.isSelected()) { // Множественное
            shape.shapeSetSelected(false);
        } else {
            shape.shapeSetSelected(true);
        }
        notifyChange();
    }

    // Метод снятия выделения со всех фигур
    public void clearSelection() {
        for (Shape shape : shapes) {
            if (shape.isSelected()) {
                shape.shapeSetSelected(false);
            }
        }
        notifyChange();
    }

    // Метод выделения всех фигур
    public void selectAllShapes() {
        for (Shape s : shapes) {
            s.shapeSetSelected(true);
        }
        notifyChange();
    }

    // Метод нахождения фигуры в указанной точке
    public Shape findShapeAt(double x, double y) {
        Shape foundShape = null;
        for (int i = shapes.size() - 1; i >= 0; i--) { // последняя созданная фигура
            Shape shape = shapes.get(i);
            if (shape != null && shape.isClickInShape(x, y)) { // Попал ли клик в фигуру
                foundShape = shape;
                break;
            }
        }
        return foundShape;
    }

    // Метод для перемещения всех выделенных фигур
    public void moveSelected(double dx, double dy, double canvasWidth, double canvasHeight) {
        boolean canMoveAll = true;
        for (Shape shape : shapes) {
            if (shape.isSelected()) {
                if (!shape.canMove(dx, dy, canvasWidth, canvasHeight)) { // нельзя
                    canMoveAll = false;
                    break;
                }
            }
        }
        if (canMoveAll) { // можно
            for (Shape shape : shapes) {
                if (shape.isSelected()) {
                    shape.moveShape(dx, dy, canvasWidth, canvasHeight);
                }
            }
            notifyChange();
        }
    }

    // Метод изменения размера всех выделенных фигур
    public void resizeSelected(double scale, double canvasWidth, double canvasHeight) { // double scale: кэф масштабирования, например1.1 это увеличить на 10%
        boolean canResizeAll = true;
        for (Shape shape : shapes) {
            if (shape.isSelected()) {
                double newSize = shape.getSizeOfShape() * scale; //нынешний размер * на кэф масштабирования
                if (newSize <= 5 || newSize >= 1000 || !shape.canResize(newSize, canvasWidth, canvasHeight)) { //нельзя изменить
                    canResizeAll = false;
                    break;
                }
            }
        }
        if (canResizeAll) { // можно изменить
            for (Shape shape : shapes) {
                if (shape.isSelected()) {
                    double newSize = shape.getSizeOfShape() * scale;
                    shape.resizeShape(newSize, canvasWidth, canvasHeight);
                }
            }
            notifyChange();
        }
    }

    // Метод измененеия цвета всех выделенных фигур
    public void changeColorSelected(Color color) {
        for (Shape shape : shapes) {
            if (shape.isSelected()) {
                shape.setColor(color);
            }
        }
        notifyChange();
    }

    // Метод для перерисовки всех фигур на холсте
    public void drawAll(javafx.scene.canvas.GraphicsContext gc) {
        for (Shape shape : shapes) {
            shape.draw(gc);
        }
    }

    // Метод для выделения всех фигур, которые пересекаются с выделенной областью
    public void selectShapesInArea(double x1, double y1, double x2, double y2) { // Координаты начальной и конечной точки области выделения
        double minX = Math.min(x1, x2); // Вычисляем границы прямоугольника выделения
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        for (Shape shape : shapes) {
            if (shape.getMinX() <= maxX && shape.getMaxX() >= minX && // L фигуры <= R области выделелния и R фигуры >= L области
                shape.getMinY() <= maxY && shape.getMaxY() >= minY) { // верх ф <= низ о и низ ф >= верх о
                selectShape(shape, true);
            }
        }
    }

    // Получение текущего выбранного цвета
    public Color getColorNow() {
        return colorNow;
    }

    // Устанавливка текущего выбранного цвета
    public void setColorNow(Color color) {
        this.colorNow = color;
    }
    
    // Очистка всех фигуры с холста
    public void clearAll() {
        shapes.clear();
        notifyChange();
    }

    // Уведомление
    private void notifyChange() {
        onModelChanged.run();
    }
}
