package com.example.lab_4_oop;

import com.example.lab_4_oop.shapes.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import java.util.ResourceBundle;
public class Controller implements Initializable {
    @FXML private Canvas canvas;
    @FXML private Button btnCursor, btnSelect, btnCircle, btnSquare, btnEllipse, btnRectangle, btnTriangle, btnLine;
    @FXML private ColorPicker colorPicker;
    @FXML private MenuItem menuNew;

    private Model model;
    private boolean ctrlPressed = false;
    private boolean shiftPressed = false;
    private double mouseStartX, mouseStartY;
    private Shape draggedShape = null;
    private boolean isDragging = false;
    private boolean isResizing = false;
    private boolean isSelecting = false;
    private boolean isCreating = false;
    private double initialSize = 0;
    private Tool currentTool = Tool.CURSOR;
    private Shape lastCreatedShape = null;
    private boolean dragStarted = false;
    private boolean isResizingLineStart = false;
    private long mousePressTime = 0;
    private static final long CLICK_THRESHOLD = 200; // 200ms для различения клика и перетаскивания
    
    public enum Tool {
        CURSOR, SELECT, CIRCLE, SQUARE, ELLIPSE, RECTANGLE, TRIANGLE, LINE
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = new Model();
        model.setOnModelChanged(this::redraw);

        setupToolbar();
        setupMenu();
        
        setupCanvas();
        setupKeyboard();
        redraw();
    }

    private void setupToolbar() {
        loadIcon(btnCursor, "/com/example/lab_4_oop/cursor.png");
        loadIcon(btnSelect, "/com/example/lab_4_oop/highlight.png");
        loadIcon(btnCircle, "/com/example/lab_4_oop/circle.png");
        loadIcon(btnSquare, "/com/example/lab_4_oop/square.png");
        loadIcon(btnEllipse, "/com/example/lab_4_oop/ellipse.png");
        loadIcon(btnRectangle, "/com/example/lab_4_oop/rectangle.png");
        loadIcon(btnTriangle, "/com/example/lab_4_oop/triangle.png");
        loadIcon(btnLine, "/com/example/lab_4_oop/line.png");
        
        // Инструменты
        btnCursor.setOnAction(e -> setTool(Tool.CURSOR));
        btnSelect.setOnAction(e -> setTool(Tool.SELECT));
        
        // Фигуры
        btnCircle.setOnAction(e -> setTool(Tool.CIRCLE));
        btnSquare.setOnAction(e -> setTool(Tool.SQUARE));
        btnEllipse.setOnAction(e -> setTool(Tool.ELLIPSE));
        btnRectangle.setOnAction(e -> setTool(Tool.RECTANGLE));
        btnTriangle.setOnAction(e -> setTool(Tool.TRIANGLE));
        btnLine.setOnAction(e -> setTool(Tool.LINE));

        colorPicker.setValue(Color.RED);
        colorPicker.setOnAction(e -> {
            model.setCurrentColor(colorPicker.getValue());
            model.changeColorSelected(colorPicker.getValue());
        });
        
        // Устанавливаем курсор по умолчанию
        setTool(Tool.CURSOR);
    }
    
    private void loadIcon(Button button, String resourcePath) {
            Image icon = new Image(getClass().getResourceAsStream(resourcePath));
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true);
            button.setGraphic(imageView);
    }
    
    private void setTool(Tool tool) {
        currentTool = tool;
        updateToolButtons();
        
        // Очищаем выделение при смене инструмента
        if (tool != Tool.SELECT) {
            model.clearSelection();
        }
    }
    
    private void updateToolButtons() {
        // Сбрасываем стиль всех кнопок
        Button[] buttons = {btnCursor, btnSelect, btnCircle, btnSquare, btnEllipse, btnRectangle, btnTriangle, btnLine};
        for (Button btn : buttons) {
            btn.setStyle("-fx-background-color: #f0f0f0;");
        }
        
        // Выделяем активную кнопку
        Button activeButton = null;
        switch (currentTool) {
            case CURSOR: activeButton = btnCursor; break;
            case SELECT: activeButton = btnSelect; break;
            case CIRCLE: activeButton = btnCircle; break;
            case SQUARE: activeButton = btnSquare; break;
            case ELLIPSE: activeButton = btnEllipse; break;
            case RECTANGLE: activeButton = btnRectangle; break;
            case TRIANGLE: activeButton = btnTriangle; break;
            case LINE: activeButton = btnLine; break;
        }
        
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white;");
        }
    }

    private void setupCanvas() {
        // Устанавливаем начальные размеры Canvas
        canvas.setWidth(1000);
        canvas.setHeight(700);
        
        // Привязываем размеры к размеру окна после инициализации
        Platform.runLater(() -> {
            if (canvas.getScene() != null && canvas.getScene().getWindow() != null) {
                Stage stage = (Stage) canvas.getScene().getWindow();
                canvas.widthProperty().bind(stage.widthProperty().subtract(20));
                canvas.heightProperty().bind(stage.heightProperty().subtract(100));
            }
        });

        canvas.setOnMousePressed(e -> {
            mouseStartX = e.getX();
            mouseStartY = e.getY();
            mousePressTime = System.currentTimeMillis();
            Shape shape = model.findShapeAt(e.getX(), e.getY());

            if (e.getButton() == MouseButton.PRIMARY) {
                // Сначала обрабатываем выделение фигур для всех инструментов
                if (shape != null) {
                    // Выделяем фигуру только если она не выделена или зажат Ctrl
                    if (!shape.isSelected() || e.isControlDown()) {
                        model.selectShape(shape, e.isControlDown());
                    }

                    // Проверяем, кликнули ли мы на край фигуры или в центр
                    if (shape instanceof Line) {
                        // Для линии - всегда можно менять размер и направление
                        Line line = (Line) shape;
                        double lineLength = Math.sqrt((line.getX2() - line.getX()) * (line.getX2() - line.getX()) +
                                                      (line.getY2() - line.getY()) * (line.getY2() - line.getY()));
                        double distToStart = Math.sqrt((e.getX() - line.getX()) * (e.getX() - line.getX()) +
                                                       (e.getY() - line.getY()) * (e.getY() - line.getY()));
                        double distToEnd = Math.sqrt((e.getX() - line.getX2()) * (e.getX() - line.getX2()) +
                                                     (e.getY() - line.getY2()) * (e.getY() - line.getY2()));

                        if (distToStart < 10 || distToEnd < 10) {
                            // Клик на конце линии - изменение размера/направления
                            isResizing = true;
                            draggedShape = shape;
                            initialSize = lineLength;
                            isResizingLineStart = distToStart < distToEnd;
                        } else {
                            // Клик на середине линии - перемещение
                            isDragging = true;
                            draggedShape = shape;
                            dragStarted = false;
                        }
                    } else {
                        // Для других фигур - проверяем клавиши
                        if (e.isShiftDown()) {
                            // Изменение размера
                            isResizing = true;
                            if (model.getSelectedShapes().size() > 1) {
                                draggedShape = null; // Групповое изменение размера
                                initialSize = 50;
                            } else {
                                draggedShape = shape;
                                initialSize = shape.getSize();
                            }
                        } else {
                            // Перемещение
                            isDragging = true;
                            draggedShape = shape;
                            dragStarted = false;
                        }
                    }
                } else if (!e.isControlDown()) {
                    model.clearSelection();

                    // Проверяем изменение размера группы для всех инструментов
                    if (e.isShiftDown() && !model.getSelectedShapes().isEmpty()) {
                        isResizing = true;
                        draggedShape = null;
                        initialSize = 50;
                    }
                }

                switch (currentTool) {
                    case CURSOR:
                        // Логика уже обработана выше
                        break;

                    case SELECT:
                        isSelecting = true;
                        break;

                    case LINE:
                        if (shape == null) {
                            isCreating = true;
                            createLine(e.getX(), e.getY());
                        }
                        break;

                    case CIRCLE:
                    case SQUARE:
                    case ELLIPSE:
                    case RECTANGLE:
                    case TRIANGLE:
                        // Создаем фигуру только если клик не по существующей фигуре
                        if (shape == null) {
                            isCreating = true;
                            createShape(e.getX(), e.getY());
                        }
                        break;
                }
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (isDragging) {
                // Перемещаем все выделенные фигуры
                double dx = e.getX() - mouseStartX;
                double dy = e.getY() - mouseStartY;
                if (Math.abs(dx) > 1 || Math.abs(dy) > 1) {
                    if (!dragStarted) {
                        model.saveStateForMove();
                        dragStarted = true;
                    }
                    model.moveSelected(dx, dy, canvas.getWidth(), canvas.getHeight());
                    mouseStartX = e.getX();
                    mouseStartY = e.getY();
                }
            } else if (isResizing) {
                // Изменяем размер выделенных фигур
                if (draggedShape != null && draggedShape instanceof Line) {
                    // Для линии - изменение размера и направления
                    if (!dragStarted) {
                        model.saveStateForMove();
                        dragStarted = true;
                    }
                    Line line = (Line) draggedShape;

                    // Определяем, какой конец линии перемещается
                    if (isResizingLineStart) {
                        // Изменяем начальную точку
                        double newX = Math.max(0, Math.min(e.getX(), canvas.getWidth()));
                        double newY = Math.max(0, Math.min(e.getY(), canvas.getHeight()));
                        line.setX(newX);
                        line.setY(newY);
                    } else {
                        // Изменяем конечную точку
                        double newX2 = Math.max(0, Math.min(e.getX(), canvas.getWidth()));
                        double newY2 = Math.max(0, Math.min(e.getY(), canvas.getHeight()));
                        line.setX2(newX2);
                        line.setY2(newY2);
                    }

                    // Обновляем размер линии
                    double length = Math.sqrt((line.getX2() - line.getX()) * (line.getX2() - line.getX()) +
                                             (line.getY2() - line.getY()) * (line.getY2() - line.getY()));
                    line.setSize(length);
                    redraw();
                } else if (draggedShape != null) {
                    // Изменяем размер одной фигуры
                    double distance = Math.sqrt(
                        Math.pow(e.getX() - draggedShape.getX(), 2) +
                        Math.pow(e.getY() - draggedShape.getY(), 2)
                    );
                    // Замедляем изменение размера
                    double newSize = initialSize + (distance - Math.sqrt(
                        Math.pow(mouseStartX - draggedShape.getX(), 2) +
                        Math.pow(mouseStartY - draggedShape.getY(), 2)
                    )) * 0.5; // Коэффициент замедления 0.5

                    if (newSize > initialSize * 0.3 && newSize < initialSize * 10) {
                        if (!dragStarted) {
                            model.saveStateForMove();
                            dragStarted = true;
                        }
                        // Изменяем размер только выделенной фигуры
                        if (draggedShape.canResize(newSize, canvas.getWidth(), canvas.getHeight())) {
                            draggedShape.resize(newSize, canvas.getWidth(), canvas.getHeight());
                            redraw();
                        }
                    }
                } else {
                    // Изменяем размер группы фигур
                    double distance = Math.sqrt(
                        Math.pow(e.getX() - mouseStartX, 2) +
                        Math.pow(e.getY() - mouseStartY, 2)
                    );
                    // Замедляем изменение размера группы
                    double scale = 1.0 + (distance - 50) * 0.01; // Коэффициент замедления 0.01
                    if (scale > 0.3 && scale < 5.0) {
                        if (!dragStarted) {
                            model.saveStateForMove();
                            dragStarted = true;
                        }
                        model.resizeSelected(scale, canvas.getWidth(), canvas.getHeight());
                    }
                }
            } else if (isSelecting) {
                // Рисуем прямоугольник выделения
                redraw();
                drawSelectionRectangle(mouseStartX, mouseStartY, e.getX(), e.getY());
            } else if (isCreating && lastCreatedShape != null) {
                if (currentTool == Tool.LINE) {
                    // Обновляем линию
                    updateLine(e.getX(), e.getY());
                } else {
                    // Изменяем размер создаваемой фигуры
                    double distance = Math.sqrt(
                        Math.pow(e.getX() - lastCreatedShape.getX(), 2) +
                        Math.pow(e.getY() - lastCreatedShape.getY(), 2)
                    );
                    double newSize = distance * 2;
                    if (newSize > 10 && newSize < 500) {
                        lastCreatedShape.resize(newSize, canvas.getWidth(), canvas.getHeight());
                        redraw();
                    }
                }
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                long clickDuration = System.currentTimeMillis() - mousePressTime;

                if (isSelecting) {
                    // Завершаем выделение области
                    selectShapesInArea(mouseStartX, mouseStartY, e.getX(), e.getY());
                    redraw();
                } else if (isCreating) {
                    // Завершаем создание фигуры
                    if (currentTool == Tool.LINE) {
                        // Если это был быстрый клик (без перетаскивания), создаем полноценную линию
                        if (clickDuration < CLICK_THRESHOLD && lineStart != null) {
                            createFullLineAtClick(e.getX(), e.getY());
                        }
                        // Завершаем создание линии
                        lineStart = null;
                    }
                    // Фигура уже создана, просто завершаем процесс
                }
            }

            isDragging = false;
            isResizing = false;
            isSelecting = false;
            isCreating = false;
            isResizingLineStart = false;
            draggedShape = null;
            dragStarted = false;
        });
    }

    private void setupKeyboard() {
        // Настройка клавиатуры будет выполнена после загрузки сцены
        Platform.runLater(() -> {
            if (canvas.getScene() != null) {
                canvas.getScene().setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.CONTROL) ctrlPressed = true;
                    if (e.getCode() == KeyCode.SHIFT) shiftPressed = true;
                    if (e.getCode() == KeyCode.DELETE) {
                        model.removeSelected();
                    }
                    if (e.getCode() == KeyCode.A && ctrlPressed) {
                        selectAll();
                    }
                    if (e.getCode() == KeyCode.N && ctrlPressed) {
                        createNew();
                    }
                    if (e.getCode() == KeyCode.Z && ctrlPressed) {
                        model.undo();
                    }
                    if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT ||
                        e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                        moveWithArrowKeys(e.getCode());
                    }
                    if (e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.ADD ||
                        (e.getCode() == KeyCode.EQUALS && e.isShiftDown())) {
                        resizeSelected(1.1);
                    }
                    if (e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT) {
                        resizeSelected(0.9);
                    }
                });

                canvas.getScene().setOnKeyReleased(e -> {
                    if (e.getCode() == KeyCode.CONTROL) ctrlPressed = false;
                    if (e.getCode() == KeyCode.SHIFT) shiftPressed = false;
                });
            }
        });
    }

    private void setupMenu() {
        menuNew.setOnAction(e -> createNew());
    }
    
    @FXML
    private void createNew() {
        model.clearAll();
        redraw();
    }

    private void moveWithArrowKeys(KeyCode code) {
        double step = shiftPressed ? 10 : 1;
        double dx = 0, dy = 0;
        switch (code) {
            case LEFT: dx = -step; break;
            case RIGHT: dx = step; break;
            case UP: dy = -step; break;
            case DOWN: dy = step; break;
            default: break;
        }
        model.moveSelected(dx, dy, canvas.getWidth(), canvas.getHeight());
    }

    private void resizeSelected(double scale) {
        model.resizeSelected(scale, canvas.getWidth(), canvas.getHeight());
    }

    private void selectAll() {
        model.clearSelection();
        for (Shape shape : model.getShapes()) {
            shape.setSelected(true);
            model.getSelectedShapes().add(shape);
        }
        redraw();
    }

    private void createShape(double x, double y) {
        Shape shape = null;
        double size = 50;
        Color color = model.getCurrentColor();

        switch (currentTool) {
            case CIRCLE:
                shape = new Circle(x, y, size, color);
                break;
            case SQUARE:
                shape = new Square(x, y, size, color);
                break;
            case ELLIPSE:
                shape = new Ellipse(x, y, size, color);
                break;
            case RECTANGLE:
                shape = new Rectangle(x, y, size, color);
                break;
            case TRIANGLE:
                shape = new Triangle(x, y, size, color);
                break;
            default:
                break;
        }

        if (shape != null) {
            // Автоматически корректируем позицию фигуры, чтобы она помещалась в границы
            shape.constrainToBounds(canvas.getWidth(), canvas.getHeight());
            model.addShape(shape);
            lastCreatedShape = shape;
        }
    }

    private Shape lineStart = null;
    private void createLine(double x, double y) {
        // Начинаем создание линии
        lineStart = new Line(x, y, x, y, model.getCurrentColor());
        // Автоматически корректируем позицию линии, чтобы она помещалась в границы
        lineStart.constrainToBounds(canvas.getWidth(), canvas.getHeight());
        model.addShape(lineStart);
        lastCreatedShape = lineStart;
        redraw();
    }
    
    private void updateLine(double x, double y) {
        if (lineStart != null && lineStart instanceof Line) {
            Line line = (Line) lineStart;
            double x2 = Math.max(0, Math.min(x, canvas.getWidth()));
            double y2 = Math.max(0, Math.min(y, canvas.getHeight()));
            line.setX2(x2);
            line.setY2(y2);
            // Обновляем размер линии
            double length = Math.sqrt((x2 - line.getX()) * (x2 - line.getX()) + (y2 - line.getY()) * (y2 - line.getY()));
            line.setSize(length);
            redraw();
        }
    }
    
    private void createFullLineAtClick(double x, double y) {
        if (lineStart != null && lineStart instanceof Line) {
            Line line = (Line) lineStart;
            double startX = line.getX();
            double startY = line.getY();
            
            // Создаем линию длиной 50 пикселей в случайном направлении
            double angle = Math.random() * 2 * Math.PI;
            double lineLength = 50;
            double endX = startX + Math.cos(angle) * lineLength;
            double endY = startY + Math.sin(angle) * lineLength;
            
            // Ограничиваем конечную точку границами canvas
            endX = Math.max(0, Math.min(endX, canvas.getWidth()));
            endY = Math.max(0, Math.min(endY, canvas.getHeight()));
            
            // Обновляем линию
            line.setX2(endX);
            line.setY2(endY);
            line.setSize(Math.sqrt((endX - startX) * (endX - startX) + (endY - startY) * (endY - startY)));
            
            // Применяем корректировку границ
            line.constrainToBounds(canvas.getWidth(), canvas.getHeight());
            
            redraw();
        }
    }

    @FXML
    private void exitApplication() {
        System.exit(0);
    }

    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Shape shape : model.getShapes()) {
            shape.draw(gc);
        }
    }
    
    private void drawSelectionRectangle(double x1, double y1, double x2, double y2) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        gc.setLineDashes(5, 5);
        gc.strokeRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
        gc.setLineDashes();
    }
    
    private void selectShapesInArea(double x1, double y1, double x2, double y2) {
        double minX = Math.min(x1, x2);
        double maxX = Math.max(x1, x2);
        double minY = Math.min(y1, y2);
        double maxY = Math.max(y1, y2);
        
        for (Shape shape : model.getShapes()) {
            if (shape.getMinX() >= minX && shape.getMaxX() <= maxX &&
                shape.getMinY() >= minY && shape.getMaxY() <= maxY) {
                model.selectShape(shape, true); // true для множественного выделения
            }
        }
    }
    
    private Shape getLastCreatedShape() {
        return lastCreatedShape;
    }
    
}

