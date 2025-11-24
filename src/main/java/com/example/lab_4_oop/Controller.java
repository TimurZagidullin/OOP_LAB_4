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
    @FXML private Button btnCursor, btnSelect, btnCircle, btnSquare, btnRectangle, btnTriangle, btnLine;
    @FXML private ColorPicker colorPicker;

    private Model model;
    private double mouseStartX, mouseStartY;
    private double prevMouseX, prevMouseY; // предыдущие координаты мыши для вычисления смещения при перетаскивании
    private boolean isDragging = false;
    private boolean isSelecting = false; // выделение области
    private boolean isCreating = false;
    private Tool useNowTool = Tool.CURSOR;
    private Shape lastCreatedShape = null; // для изменения размера фигуры во время её создания
    private boolean dragStarted = false; // для предотвращения случайных перемещений при клике
    private long mousePressTime = 0;
    private static final long CLICK_THRESHOLD = 200; //  < 200 мс - клик, иначе перетаскивание

    public enum Tool { CURSOR, SELECT, CIRCLE, SQUARE, RECTANGLE, TRIANGLE, LINE } // список конст значений инструменты

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = new Model();
        model.setOnModelChanged(this::redraw);
        setupToolbar();
        setupCanvas();
        setupKeyboard();
        redraw();
    }

    private void setupToolbar() {
        loadIcon(btnCursor, "/com/example/lab_4_oop/cursor.png");
        loadIcon(btnSelect, "/com/example/lab_4_oop/highlight.png");
        loadIcon(btnCircle, "/com/example/lab_4_oop/circle.png");
        loadIcon(btnSquare, "/com/example/lab_4_oop/square.png");
        loadIcon(btnRectangle, "/com/example/lab_4_oop/rectangle.png");
        loadIcon(btnTriangle, "/com/example/lab_4_oop/triangle.png");
        loadIcon(btnLine, "/com/example/lab_4_oop/line.png");

        btnCursor.setOnAction(e -> setTool(Tool.CURSOR));
        btnSelect.setOnAction(e -> setTool(Tool.SELECT));
        btnCircle.setOnAction(e -> setTool(Tool.CIRCLE));
        btnSquare.setOnAction(e -> setTool(Tool.SQUARE));
        btnRectangle.setOnAction(e -> setTool(Tool.RECTANGLE));
        btnTriangle.setOnAction(e -> setTool(Tool.TRIANGLE));
        btnLine.setOnAction(e -> setTool(Tool.LINE));

        colorPicker.setValue(Color.RED);
        colorPicker.setOnAction(e -> {
            model.setColorNow(colorPicker.getValue()); // сохранение в model для новых фигур
            model.changeColorSelected(colorPicker.getValue()); // измененение цвета выделенных фигур
        });
        setTool(Tool.CURSOR);
    }

    private void loadIcon(Button button, String resourcePath) {
        java.io.InputStream img = getClass().getResourceAsStream(resourcePath);
        if (img == null) return;
        try (java.io.InputStream stream = img) {
            Image icon = new Image(stream);
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            button.setGraphic(imageView);
        } catch (Exception ex) {}
    }
    
    // Метод переключения активного инструмента
    private void setTool(Tool tool) {
        useNowTool = tool;
        updateToolButtonsLights(); // подсветка активной кнопки
        if (tool != Tool.SELECT) {
            model.clearSelection();
        }
    }
    
    // Метод обновления подсветки кнопок
    private void updateToolButtonsLights() {
        Button[] buttons = {btnCursor, btnSelect, btnCircle, btnSquare, btnRectangle, btnTriangle, btnLine};
        for (Button btn : buttons) { btn.setStyle("-fx-background-color: #f0f0f0;"); }
        Button activeButton = null; // Переменная для хранения активной кнопки
        switch (useNowTool) {
            case CURSOR: activeButton = btnCursor; break;
            case SELECT: activeButton = btnSelect; break;
            case CIRCLE: activeButton = btnCircle; break;
            case SQUARE: activeButton = btnSquare; break;
            case RECTANGLE: activeButton = btnRectangle; break;
            case TRIANGLE: activeButton = btnTriangle; break;
            case LINE: activeButton = btnLine; break;
        }
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white;"); // цвет фона и текста
        }
    }

    // Метод настройки холста
    private void setupCanvas() {
        canvas.setFocusTraversable(true); // холст может получать фокус клавиатуры
        Platform.runLater(() -> { // выполняет код после полной инициализации
            if (canvas.getScene() != null && canvas.getScene().getWindow() != null) { // Проверка, что холст уже добавлен в сцену и сцена в окне
                Stage stage = (Stage) canvas.getScene().getWindow(); // Ссылка на главное окно приложения
                canvas.widthProperty().bind(stage.widthProperty().subtract(20)); // Привязка ширины холста к ширине окна, отступ 20
                canvas.heightProperty().bind(stage.heightProperty().subtract(105)); // Привязка высоты холста к высоте окна, отступ 105
            }
        });

        canvas.setOnMousePressed(this::handleMousePressed); // нажатие
        canvas.setOnMouseDragged(this::handleMouseDrag); // перетаскивание
        canvas.setOnMouseReleased(this::handleMouseReleased); // отпускание
    }

    // Обработка нажатия кнопки мыши на холсте
    private void handleMousePressed(MouseEvent e) {
        canvas.requestFocus();
        mouseStartX = e.getX();
        mouseStartY = e.getY();
        prevMouseX = e.getX();
        prevMouseY = e.getY();
        mousePressTime = System.currentTimeMillis();
        Shape shape = model.findShapeAt(e.getX(), e.getY()); // поиск фигуры в точке клика

        if (e.getButton() == MouseButton.PRIMARY) {
            if (shape != null) { // если клик по фигуре
                mouseShapeClick(shape, e);
            }
            else if (!e.isControlDown()) { // если в пустое место
                model.clearSelection();
            }
            handleToolAction(e, shape); // обработка действия в зависимости от инструмента
        }
    }

    // Метод обработки клика по фигуре
    private void mouseShapeClick(Shape shape, MouseEvent e) {
        if (!shape.isSelected() || e.isControlDown()) { // не выделена или ctrl
            model.selectShape(shape, e.isControlDown()); // выделить или снять
        }
        isDragging = true;
        dragStarted = false;
    }

    // Метод обработки действия в зависимости от выбранного инструмента
    private void handleToolAction(MouseEvent e, Shape shape) {
        switch (useNowTool) {
            case CURSOR:
                break;
            case SELECT:
                isSelecting = true; // флаг начала выделения области
                break;
            case LINE:
                if (shape == null) { // клик в пустое место
                    isCreating = true;
                    updateLine(e.getX(), e.getY()); // создание линии
                }
                break;
            case CIRCLE:
            case SQUARE:
            case RECTANGLE:
            case TRIANGLE:
                if (shape == null) {
                    isCreating = true;
                    createShape(e.getX(), e.getY());
                }
                break;
        }
    }

    // Метод обработки перетаскивания мыши
    private void handleMouseDrag(MouseEvent e) {
        if (isDragging) {
            handleDrag(e); // обработка перетаскивания фигуры
        }
        else if (isSelecting) { // Если выделение области
            redraw();
            drawSelectionRectangle(mouseStartX, mouseStartY, e.getX(), e.getY()); // выделение от начальной точки до текущей позиции мыши
        }
        else if (isCreating && lastCreatedShape != null) { // Если создание новой фигуры
            handleShapeCreation(e); // создание фигуры (изменение размера во время создания)
        }
    }

    // Метод обработки перетаскивания фигуры
    private void handleDrag(MouseEvent e) {
        double dx = e.getX() - prevMouseX; // смещение
        double dy = e.getY() - prevMouseY;
        if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) { // смещение достаточное
            if (!dragStarted) {
                dragStarted = true;
            }
            // Перемещение всех выделенных фигур на вычисленное смещение
            model.moveSelected(dx, dy, getCanvasWidth(), getCanvasHeight());
            prevMouseX = e.getX();
            prevMouseY = e.getY();
        }
    }

    // Метод обработки создания новой фигуры (изменение размера во время создания)
    private void handleShapeCreation(MouseEvent e) {
        if (useNowTool == Tool.LINE) { // Если создается линия
            updateLine(e.getX(), e.getY()); // Обновление конечной точки линии до текущей позиции мыши
        }
        else { // Если создается обычная фигура
            double radius = Math.hypot(e.getX() - lastCreatedShape.getXcentrOfShape(), e.getY() - lastCreatedShape.getYcentrOfShape()); //hypot -гипотенуза
            double newSize = radius * 2; // размер фигуры равен удвоенному расстоянию от центра до мыши
            if (newSize > 10 && newSize < 500) {
                lastCreatedShape.resizeShape(newSize, getCanvasWidth(), getCanvasHeight()); // Изменение размера создаваемой фигуры
                redraw();
            }
        }
    }

    // Метод обработки отпускания кнопки мыши
    private void handleMouseReleased(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            long clickDuration = System.currentTimeMillis() - mousePressTime; // разница между временем отпускания и временем нажатия
            if (isSelecting) { // Если выделение
                selectShapesInArea(mouseStartX, mouseStartY, e.getX(), e.getY()); // выделение всех фигур в области
            }
            else if (isCreating) {
                if (useNowTool == Tool.LINE && lineStart != null) { // Если создавалась линия
                    if (clickDuration < CLICK_THRESHOLD && lineStart.getSizeOfShape() == 0) {
                        updateLine(lineStart.getXcentrOfShape() + 100, lineStart.getYcentrOfShape());
                    }
                    lineStart = null;
                }
            }
        }
        resetMouseState();
    }

    // Метод сбрасывания всех флагов состояния мыши
    private void resetMouseState() {
        isDragging = false;
        isSelecting = false;
        isCreating = false;
        dragStarted = false;
    }

    // Метод для настройки обработки нажатий клавиш
    private void setupKeyboard() {
        Platform.runLater(() -> {
            if (canvas.getScene() != null) {
                canvas.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                        moveWithArrowKeys(e.getCode());
                        e.consume();
                    }
                });
                canvas.setOnKeyPressed(e -> {
                    if (e.getCode() == KeyCode.DELETE) { model.removeSelected(); }
                    if (e.isControlDown() && e.getCode() == KeyCode.A) { selectAll(); }
                    if (e.isControlDown() && e.getCode() == KeyCode.N) { createNew(); }
                    if (e.getCode() == KeyCode.ADD || (e.getCode() == KeyCode.EQUALS && e.isShiftDown())) { resizeSelected(1.1); }
                    if (e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT) { resizeSelected(0.9); }
                });
            }
        });
    }

    // Метод для очистки холста
    @FXML
    private void createNew() {
        model.clearAll();
    }
    @FXML
    private void exitApplication() {
        System.exit(0);
    }

    // Метод для перемещения стрелками
    private void moveWithArrowKeys(KeyCode code) {
        double step = 5;
        double dx = 0, dy = 0;
        switch (code) {
            case LEFT: dx = -step; break;
            case RIGHT: dx = step; break;
            case UP: dy = -step; break;
            case DOWN: dy = step; break;
            default: break;
        }
        model.moveSelected(dx, dy, getCanvasWidth(), getCanvasHeight()); // Перемещение всех выделенных фигур на вычисленное смещение
    }

    // Метод изменения размера выделенных фигур
    private void resizeSelected(double scale) { // double scale - кэф масштабирования
        model.resizeSelected(scale, getCanvasWidth(), getCanvasHeight());
    }

    // Метод выделения всех фигур на холсте
    private void selectAll() {
        model.selectAllShapes();
    }
    
    // Метод для получениия ширины холста
    private double getCanvasWidth() { return canvas.widthProperty().get(); }
    // Метод для получения высоты холста
    private double getCanvasHeight() { return canvas.heightProperty().get(); }

    // Метод для создания фигуры
    private void createShape(double x, double y) {
        Shape shape = null;
        double size = 50;
        Color color = model.getColorNow();
        switch (useNowTool) {
            case CIRCLE:
                shape = new Circle(x, y, size, color);
                break;
            case SQUARE:
                shape = new Square(x, y, size, color);
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
        if (shape != null) { // Если фигура создана
            shape.correctPositionToBounds(getCanvasWidth(), getCanvasHeight()); // Корректировка позиции фигуры
            model.addShapeToStorage(shape);
            lastCreatedShape = shape; // Сохранение ссылки на созданную фигуру (для изменения размера во время создания)
        }
    }

    private Shape lineStart = null; // ссылка на создаваемую линию для обновления конечной точки линии во время создания
    
    // Метод для создания или обновления линии
    private void updateLine(double x, double y) {
        if (lineStart == null) {
            double margin = 100; // Отступ от правого края
            double startX = Math.min(x, getCanvasWidth() - margin);
            double startY = y;
            lineStart = new Line(startX, startY, startX, startY, model.getColorNow());
            model.addShapeToStorage(lineStart);
            lastCreatedShape = lineStart;
        }
        else if (lineStart instanceof Line) { // Если линия уже существует - обновить конечную точку
            Line line = (Line) lineStart;
            double x2 = Math.max(0, Math.min(x, getCanvasWidth()));
            double y2 = Math.max(0, Math.min(y, getCanvasHeight()));
            line.setX2(x2);
            line.setY2(y2);
            line.updateSize(); // пересчет размера линии на основе новых координат
        }
        redraw();
    }

    // Метод перерисовки
    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, getCanvasWidth(), getCanvasHeight()); // очистка полотна
        model.drawAll(gc); // перерисовка фигур
    }
    
    // Метод для рисования прямоугольника выделения области
    private void drawSelectionRectangle(double x1, double y1, double x2, double y2) { // координаты начальной и конечной точки прямоугольника
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        gc.setLineDashes(5, 5);
        gc.strokeRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1)); // прямоугольник выделения
        gc.setLineDashes(); // сброс пунктира
    }
    
    // Метод для выделения всех фигур, которые пересекаются с выделенной областью
    private void selectShapesInArea(double x1, double y1, double x2, double y2) {
        model.selectShapesInArea(x1, y1, x2, y2); // выделение фигур в указанной области
    }
}
