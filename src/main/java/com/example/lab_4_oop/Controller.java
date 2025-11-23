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
// Класс Controller - управляет взаимодействием пользователя с интерфейсом
// implements Initializable - реализует интерфейс для инициализации после загрузки FXML
public class Controller implements Initializable {
    @FXML private Canvas canvas;
    @FXML private Button btnCursor, btnSelect, btnCircle, btnSquare, btnRectangle, btnTriangle, btnLine;
    @FXML private ColorPicker colorPicker;

    private Model model; // model - объект модели, хранящий все фигуры и управляющий ими
    private double mouseStartX, mouseStartY; //координаты точки, где была нажата кнопка мыши. Используются для определения начала действия (перетаскивание, выделение и т.д.)
    private double prevMouseX, prevMouseY; // prevMouseX, prevMouseY - предыдущие координаты мыши. Используются для вычисления смещения при перетаскивании
    private boolean isDragging = false; // флаг, указывающий что сейчас происходит перетаскивание фигуры
    private boolean isSelecting = false; // флаг, указывающий что сейчас происходит выделение области
    private boolean isCreating = false; // флаг, указывающий что сейчас создается новая фигура
    private Tool useNowTool = Tool.CURSOR; // текущий активный инструмент
    private Shape lastCreatedShape = null; // lastCreatedShape - последняя созданная фигура. Используется для изменения размера фигуры во время её создания
    private boolean dragStarted = false; // dragStarted - флаг, указывающий что перетаскивание началось. Используется для предотвращения случайных перемещений при клике
    private long mousePressTime = 0; // время (в миллисекундах) когда была нажата кнопка мыши. Используется для определения был ли это клик или перетаскивание
    private static final long CLICK_THRESHOLD = 200; // пороговое значение времени для определения клика. Если время нажатия < 200 мс, это считается кликом, иначе - перетаскиванием
    
    // Перечисление Tool - список всех доступных инструментов редактора
    // enum - специальный тип для создания списка константных значений
    public enum Tool { CURSOR, SELECT, CIRCLE, SQUARE, RECTANGLE, TRIANGLE, LINE }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = new Model();
        model.setOnModelChanged(this::redraw); // ссылка на метод redraw класса model
        setupToolbar();
        setupCanvas();
        setupKeyboard();
        redraw();
    }

    // Метод setupToolbar - настраивает панель инструментов
    private void setupToolbar() {
        loadIcon(btnCursor, "/com/example/lab_4_oop/cursor.png");
        loadIcon(btnSelect, "/com/example/lab_4_oop/highlight.png");
        loadIcon(btnCircle, "/com/example/lab_4_oop/circle.png");
        loadIcon(btnSquare, "/com/example/lab_4_oop/square.png");
        loadIcon(btnRectangle, "/com/example/lab_4_oop/rectangle.png");
        loadIcon(btnTriangle, "/com/example/lab_4_oop/triangle.png");
        loadIcon(btnLine, "/com/example/lab_4_oop/line.png");

        // Устанавливаем обработчики событий для кнопок инструментов. setOnAction - метод устанавливает функцию, которая вызывается при нажатии кнопки. При нажатии кнопки вызывается метод setTool с соответствующим инструментом
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
        setTool(Tool.CURSOR); // по умолчанию
    }

    // Метод установки иконки на кнопку
    private void loadIcon(Button button, String resourcePath) {
        java.io.InputStream img = getClass().getResourceAsStream(resourcePath);
        if (img == null) return;
        try (java.io.InputStream stream = img) {
            Image icon = new Image(stream);
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
            imageView.setPreserveRatio(true); // Сохранение пропорций изображения
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
            activeButton.setStyle("-fx-background-color: #0078d4; -fx-text-fill: white;"); // цвет фона и текста для кнопки
        }
    }

    // Метод настройки холста
    private void setupCanvas() {
        canvas.setFocusTraversable(true); // холст может получать фокус клавиатуры
        Platform.runLater(() -> { // выполняет код после полной инициализации JavaFX
            if (canvas.getScene() != null && canvas.getScene().getWindow() != null) { // Проверяем, что холст уже добавлен в сцену и сцена в окне
                Stage stage = (Stage) canvas.getScene().getWindow(); // Получаем ссылку на главное окно приложения
                canvas.widthProperty().bind(stage.widthProperty().subtract(20)); // Привязываем ширину холста к ширине окна (с отступом 20 пикселей). bind - создает связь: при изменении ширины окна автоматически меняется ширина холста
                canvas.heightProperty().bind(stage.heightProperty().subtract(105)); // Привязываем высоту холста к высоте окна (с отступом 105 пикселей)
            }
        });

        canvas.setOnMousePressed(this::handleMousePressed); // Обработчик события нажатия кнопки мыши // this::handleMousePressed - ссылка на метод handleMousePressed этого класса
        canvas.setOnMouseDragged(this::handleMouseDrag); // Обработчик события перетаскивания мышью
        canvas.setOnMouseReleased(this::handleMouseReleased); // Обработчик события отпускания кнопки мыши
    }

    // Метод обработки нажатия кнопки мыши на холсте
    private void handleMousePressed(MouseEvent e) {
        canvas.requestFocus(); // Запрашиваем фокус для холста (чтобы обрабатывать нажатия клавиш)
        mouseStartX = e.getX();// Сохраняем координаты точки, где была нажата кнопка мыши // e.getX() - координата X точки клика относительно холста
        mouseStartY = e.getY();
        prevMouseX = e.getX(); // Сохраняем текущие координаты как предыдущие (для вычисления смещения при перетаскивании)
        prevMouseY = e.getY();
        mousePressTime = System.currentTimeMillis(); // Запоминаем время нажатия кнопки мыши (в миллисекундах)
        Shape shape = model.findShapeAt(e.getX(), e.getY()); // Ищем фигуру в точке клика

        if (e.getButton() == MouseButton.PRIMARY) { // Проверяем, что была нажата левая кнопка мыши
            if (shape != null) { // Если клик был по фигуре
                mouseShapeClick(shape, e);
            }
            else if (!e.isControlDown()) { // Если клик был не по фигуре и не зажат Ctrl (обычный клик в пустое место)
                model.clearSelection(); // Очищаем все выделения
            }
            handleToolAction(e, shape); // Обрабатываем действие в зависимости от выбранного инструмента
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
                isSelecting = true; // Устанавливаем флаг начала выделения области
                break;
            case LINE: // Если выбран инструмент создания линии
                if (shape == null) { // Если клик был не по фигуре (в пустое место)
                    isCreating = true; // Устанавливаем флаг создания новой фигуры
                    updateLine(e.getX(), e.getY()); // Создаем или обновляем линию
                }
                break;
            case CIRCLE: // Если выбран инструмент создания фигуры (круг, квадрат и т.д.)
            case SQUARE:
            case RECTANGLE:
            case TRIANGLE:
                if (shape == null) { // Если клик был не по фигуре (в пустое место)
                    isCreating = true; // Устанавливаем флаг создания новой фигуры
                    createShape(e.getX(), e.getY()); // Создаем новую фигуру в точке клика
                }
                break;
        }
    }

    // Метод обработки перетаскивания мыши
    private void handleMouseDrag(MouseEvent e) {
        if (isDragging) { // Если происходит перетаскивание фигуры
            handleDrag(e); // Обрабатываем перетаскивание (перемещение фигуры)
        }
        else if (isSelecting) { // Если происходит выделение области
            redraw(); // Перерисовываем холст (чтобы убрать предыдущий прямоугольник выделения)
            drawSelectionRectangle(mouseStartX, mouseStartY, e.getX(), e.getY()); // Рисуем прямоугольник выделения от начальной точки до текущей позиции мыши
        }
        else if (isCreating && lastCreatedShape != null) { // Если происходит создание новой фигуры
            handleShapeCreation(e); // Обрабатываем создание фигуры (изменение размера во время создания)
        }
    }

    // Метод обработки перетаскивания фигуры
    private void handleDrag(MouseEvent e) {
        double dx = e.getX() - prevMouseX; // Вычисляем смещение по оси X (на сколько пикселей сдвинулась мышь) // dx - разница между текущей и предыдущей координатой X
        double dy = e.getY() - prevMouseY;
        if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) { // Проверяем, что смещение достаточно большое (больше 0.1 пикселя) // Это предотвращает случайные микродвижения при клике // Math.abs() - абсолютное значение (модуль числа, убирает знак минус)
            if (!dragStarted) { // Если перетаскивание еще не началось
                dragStarted = true; // Устанавливаем флаг начала перетаскивания
            }
            // Перемещаем все выделенные фигуры на вычисленное смещение
            // moveSelected - метод модели, перемещает все выделенные фигуры
            // getCanvasWidth(), getCanvasHeight() - актуальные размеры холста (для проверки границ)
            model.moveSelected(dx, dy, getCanvasWidth(), getCanvasHeight());
            prevMouseX = e.getX(); // Сохраняем текущие координаты как предыдущие для следующего движения
            prevMouseY = e.getY();
        }
    }

    // Метод обработки создания новой фигуры (изменение размера во время создания)
    private void handleShapeCreation(MouseEvent e) {
        if (useNowTool == Tool.LINE) { // Если создается линия
            updateLine(e.getX(), e.getY()); // Обновляем конечную точку линии до текущей позиции мыши
        }
        else { // Если создается обычная фигура (круг, квадрат и т.д.)
            // Вычисляем расстояние от центра создаваемой фигуры до текущей позиции мыши
            // Math.hypot - вычисляет гипотенузу (расстояние между двумя точками)
            double radius = Math.hypot(e.getX() - lastCreatedShape.getXcentrOfShape(), e.getY() - lastCreatedShape.getYcentrOfShape());
            double newSize = radius * 2; // Вычисляем новый размер фигуры (диаметр = расстояние * 2) // radius * 2 - размер фигуры равен удвоенному расстоянию от центра до мыши
            if (newSize > 10 && newSize < 500) { // Проверяем, что размер в допустимых пределах
                lastCreatedShape.resizeShape(newSize, getCanvasWidth(), getCanvasHeight()); // Изменяем размер создаваемой фигуры
                redraw();
            }
        }
    }

    // Метод обработки отпускания кнопки мыши
    private void handleMouseReleased(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            long clickDuration = System.currentTimeMillis() - mousePressTime; // Вычисляем длительность нажатия кнопки мыши (в миллисекундах) // clickDuration - разница между временем отпускания и временем нажатия
            if (isSelecting) { // Если происходило выделение области
                selectShapesInArea(mouseStartX, mouseStartY, e.getX(), e.getY()); // Выделяем все фигуры, которые полностью находятся в выделенной области
                redraw();
            }
            else if (isCreating) { // Если происходило создание новой фигуры
                if (useNowTool == Tool.LINE && lineStart != null) { // Если создавалась линия
                    if (clickDuration < CLICK_THRESHOLD && lineStart.getSizeOfShape() == 0) { // Если это был быстрый клик (без перетаскивания) и линия еще точка (длина = 0)
                        // Автоматически устанавливаем конечную точку (горизонтальная линия длиной 100)
                        updateLine(lineStart.getXcentrOfShape() + 100, lineStart.getYcentrOfShape());
                    }
                    lineStart = null; // Очищаем ссылку на создаваемую линию
                }
            }
        }
        resetMouseState(); // Сбрасываем все флаги состояния мыши (завершаем все действия)
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
        Platform.runLater(() -> { // Platform.runLater - выполняет код после полной инициализации JavaFX  // Используется потому что на момент initialize() сцена может быть еще не готова
            if (canvas.getScene() != null) { // Проверяем, что холст уже добавлен в сцену
                canvas.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> { // Используем addEventFilter для перехвата событий стрелок ДО обработки меню
                    if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {  // Обрабатываем стрелки - перехватываем их до обработки меню
                        moveWithArrowKeys(e.getCode());
                        e.consume(); // Потребляем событие, чтобы меню его не обработало
                    }
                });
                
                // Обработчик для остальных клавиш на canvas (когда canvas в фокусе)
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

    // Метод для очистки холста (вызывается из меню и горячей клавиши Ctrl+N)
    @FXML
    private void createNew() {
        model.clearAll();
        redraw();
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
        model.moveSelected(dx, dy, getCanvasWidth(), getCanvasHeight()); // Перемещаем все выделенные фигуры на вычисленное смещение
    }

    // Метод изменения размера выделенных фигур
    private void resizeSelected(double scale) { // double scale - коэффициент масштабирования (1.1 = увеличить на 10%, 0.9 = уменьшить на 10%)
        model.resizeSelected(scale, getCanvasWidth(), getCanvasHeight()); // Вызываем метод модели для изменения размера всех выделенных фигур
    }

    // Метод выделения всех фигур на холсте
    private void selectAll() {
        model.selectAllShapes(); // Вызываем метод модели для выделения всех фигур
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
        if (shape != null) { // Если фигура была создана
            shape.correctPositionToBounds(getCanvasWidth(), getCanvasHeight()); // Корректируем позицию фигуры, чтобы она не выходила за границы холста // correctPositionToBounds - метод фигуры, сдвигает её если она выходит за границы
            model.addShapeToStorage(shape); // Добавляем фигуру в модель (в список всех фигур)
            lastCreatedShape = shape; // Сохраняем ссылку на созданную фигуру (для изменения размера во время создания)
        }
    }

    private Shape lineStart = null; // Переменная lineStart - ссылка на создаваемую линию // Используется для обновления конечной точки линии во время создания
    
    // Метод для создания или обновления линии
    private void updateLine(double x, double y) {
        if (lineStart == null) { // Если линия еще не создана - создаем новую
            double margin = 100; // Отступ от правого края // Корректируем координату X, чтобы точка не была слишком близко к правому краю
            double startX = Math.min(x, getCanvasWidth() - margin);
            double startY = y; // Y координата без изменений
            lineStart = new Line(startX, startY, startX, startY, model.getColorNow());
            model.addShapeToStorage(lineStart);
            lastCreatedShape = lineStart;
        }
        else if (lineStart instanceof Line) { // Если линия уже существует - обновляем конечную точку
            Line line = (Line) lineStart;
            double x2 = Math.max(0, Math.min(x, getCanvasWidth()));
            double y2 = Math.max(0, Math.min(y, getCanvasHeight()));
            line.setX2(x2);
            line.setY2(y2);
            line.updateSize(); // Пересчитываем размер линии на основе новых координат
        }
        redraw();
    }

    // Метод перерисовки
    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D(); // Получаем объект GraphicsContext для рисования на холсте // getGraphicsContext2D() - метод холста, возвращает объект для 2D рисования
        gc.clearRect(0, 0, getCanvasWidth(), getCanvasHeight()); // clearRect - очищает прямоугольную область // getCanvasWidth(), getCanvasHeight() - актуальные ширина и высота области для очистки
        model.drawAll(gc); // Просим модель перерисовать все фигуры
    }
    
    // Метод для рисования прямоугольника выделения области
    private void drawSelectionRectangle(double x1, double y1, double x2, double y2) { // double x1, y1 - координаты начальной точки прямоугольника // double x2, y2 - координаты конечной точки прямоугольника
        GraphicsContext gc = canvas.getGraphicsContext2D(); // Получаем объект GraphicsContext для рисования
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        gc.setLineDashes(5, 5); // Устанавливаем пунктирную линию (пунктир 5 пикселей, пробел 5 пикселей) // setLineDashes - метод для создания пунктирной линии
        gc.strokeRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1)); // Рисуем прямоугольник выделения // strokeRect - рисует только контур прямоугольника (не залитый) // Math.min(x1, x2), Math.min(y1, y2) - координаты левого верхнего угла (минимальные значения)// Math.abs(x2 - x1) - ширина прямоугольника (абсолютное значение разницы координат)         // Math.abs(y2 - y1) - высота прямоугольника
        gc.setLineDashes(); // Отключаем пунктирную линию (возвращаем к сплошной линии) // setLineDashes() без параметров - сбрасывает пунктир
    }
    
    // Метод для выделения всех фигур, которые пересекаются с выделенной областью
    private void selectShapesInArea(double x1, double y1, double x2, double y2) {
        model.selectShapesInArea(x1, y1, x2, y2); // Просим модель выделить фигуры в указанной области
    }
}
