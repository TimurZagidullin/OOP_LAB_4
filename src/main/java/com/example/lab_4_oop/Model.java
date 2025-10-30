package com.example.lab_4_oop;

import com.example.lab_4_oop.shapes.*;
import javafx.scene.paint.Color;
public class Model {
    private final Storage<Shape> shapes = new Storage<>();
    private final Storage<Shape> selectedShapes = new Storage<>();
    private ShapeType currentShapeType = ShapeType.CIRCLE;
    private Color currentColor = Color.RED;
    private Runnable onModelChanged;
    
    // Система отката
    private final java.util.List<Storage<Shape>> history = new java.util.ArrayList<>();
    private int historyIndex = -1;

    public enum ShapeType {
        CIRCLE, SQUARE, ELLIPSE, RECTANGLE, TRIANGLE, LINE
    }

    public void setOnModelChanged(Runnable callback) {
        this.onModelChanged = callback;
    }

    private void notifyChange() {
        if (onModelChanged != null) {
            onModelChanged.run();
        }
    }

    public void addShape(Shape shape) {
        saveState();
        shapes.add(shape);
        notifyChange();
    }

    public void removeShape(Shape shape) {
        saveState();
        shapes.remove(shape);
        selectedShapes.remove(shape);
        notifyChange();
    }

    public void removeSelected() {
        if (!selectedShapes.isEmpty()) {
            saveState();
            for (Shape shape : selectedShapes) {
                shapes.remove(shape);
            }
            selectedShapes.clear();
            notifyChange();
        }
    }

    public void selectShape(Shape shape, boolean multiSelect) {
        if (!multiSelect) {
            clearSelection();
        }
        
        // Если фигура уже выделена и это не множественное выделение, оставляем её выделенной
        if (shape.isSelected() && !multiSelect) {
            // Фигура уже выделена, ничего не делаем
            return;
        }
        
        if (shape.isSelected()) {
            shape.setSelected(false);
            selectedShapes.remove(shape);
        } else {
            shape.setSelected(true);
            selectedShapes.add(shape);
        }
        notifyChange();
    }

    public void clearSelection() {
        for (Shape shape : selectedShapes) {
            shape.setSelected(false);
        }
        selectedShapes.clear();
        notifyChange();
    }

    public Shape findShapeAt(double x, double y) {
        Shape found = null;
        // Ищем фигуры в обратном порядке (сверху вниз)
        for (int i = shapes.size() - 1; i >= 0; i--) {
            Shape shape = shapes.get(i);
            if (shape != null && shape.isClickInShape(x, y)) {
                found = shape;
                break;
            }
        }
        return found;
    }

    public void moveSelected(double dx, double dy, double canvasWidth, double canvasHeight) {
        if (!selectedShapes.isEmpty()) {
            // Проверяем, можно ли переместить все выделенные фигуры
            boolean canMoveAll = true;
            for (Shape shape : selectedShapes) {
                if (!shape.canMove(dx, dy, canvasWidth, canvasHeight)) {
                    canMoveAll = false;
                    break;
                }
            }
            
            if (canMoveAll) {
                for (Shape shape : selectedShapes) {
                    shape.move(dx, dy, canvasWidth, canvasHeight);
                }
                notifyChange();
            }
        }
    }
    
    public void saveStateForMove() {
        saveState();
    }

    public void resizeSelected(double scale, double canvasWidth, double canvasHeight) {
        if (!selectedShapes.isEmpty()) {
            for (Shape shape : selectedShapes) {
                double newSize = shape.getSize() * scale;
                if (newSize > 5 && newSize < 1000) { // Ограничения на размер
                    shape.resize(newSize, canvasWidth, canvasHeight);
                }
            }
            notifyChange();
        }
    }

    public void changeColorSelected(Color color) {
        for (Shape shape : selectedShapes) {
            shape.setColor(color);
        }
        notifyChange();
    }

    public Storage<Shape> getShapes() {
        return shapes;
    }

    public Storage<Shape> getSelectedShapes() {
        return selectedShapes;
    }

    public ShapeType getCurrentShapeType() {
        return currentShapeType;
    }

    public void setCurrentShapeType(ShapeType type) {
        this.currentShapeType = type;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
    }
    
    public void clearAll() {
        saveState();
        shapes.clear();
        selectedShapes.clear();
        notifyChange();
    }
    
    private void saveState() {
        // Удаляем все состояния после текущего индекса
        while (history.size() > historyIndex + 1) {
            history.remove(history.size() - 1);
        }
        
        // Создаем копию текущего состояния
        Storage<Shape> stateCopy = new Storage<>();
        for (Shape shape : shapes) {
            // Создаем копию фигуры
            Shape copy = createShapeCopy(shape);
            stateCopy.add(copy);
        }
        
        history.add(stateCopy);
        historyIndex++;
        
        // Ограничиваем историю 50 состояниями
        if (history.size() > 50) {
            history.remove(0);
            historyIndex--;
        }
    }
    
    private Shape createShapeCopy(Shape original) {
        // Создаем копию фигуры для истории
        if (original instanceof Circle) {
            return new Circle(original.getX(), original.getY(), original.getSize(), original.getColor());
        } else if (original instanceof Square) {
            return new Square(original.getX(), original.getY(), original.getSize(), original.getColor());
        } else if (original instanceof Ellipse) {
            return new Ellipse(original.getX(), original.getY(), original.getSize(), original.getColor());
        } else if (original instanceof Rectangle) {
            return new Rectangle(original.getX(), original.getY(), original.getSize(), original.getColor());
        } else if (original instanceof Triangle) {
            return new Triangle(original.getX(), original.getY(), original.getSize(), original.getColor());
        } else if (original instanceof Line) {
            Line line = (Line) original;
            return new Line(line.getX(), line.getY(), line.getX2(), line.getY2(), original.getColor());
        }
        return null;
    }
    
    public boolean canUndo() {
        return historyIndex > 0;
    }
    
    public void undo() {
        if (canUndo()) {
            historyIndex--;
            restoreState();
        }
    }
    
    private void restoreState() {
        if (historyIndex >= 0 && historyIndex < history.size()) {
            shapes.clear();
            selectedShapes.clear();
            
            Storage<Shape> state = history.get(historyIndex);
            for (Shape shape : state) {
                shapes.add(shape);
            }
            notifyChange();
        }
    }
}

