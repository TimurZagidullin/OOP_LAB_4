@echo off
echo Starting JavaFX Vector Editor with working icons...
echo.

REM Create target directory if it doesn't exist
if not exist "target\classes" mkdir "target\classes"

REM Compile all files
javac -cp "target/classes;C:\Users\zagid\.m2\repository\org\openjfx\javafx-controls\17.0.6\javafx-controls-17.0.6.jar;C:\Users\zagid\.m2\repository\org\openjfx\javafx-graphics\17.0.6\javafx-graphics-17.0.6.jar;C:\Users\zagid\.m2\repository\org\openjfx\javafx-base\17.0.6\javafx-base-17.0.6.jar;C:\Users\zagid\.m2\repository\org\openjfx\javafx-fxml\17.0.6\javafx-fxml-17.0.6.jar" -d target/classes src\main\java\com\example\lab_4_oop\Storage.java src\main\java\com\example\lab_4_oop\shapes\Shape.java src\main\java\com\example\lab_4_oop\shapes\Circle.java src\main\java\com\example\lab_4_oop\shapes\Square.java src\main\java\com\example\lab_4_oop\shapes\Ellipse.java src\main\java\com\example\lab_4_oop\shapes\Rectangle.java src\main\java\com\example\lab_4_oop\shapes\Triangle.java src\main\java\com\example\lab_4_oop\shapes\Line.java src\main\java\com\example\lab_4_oop\Model.java src\main\java\com\example\lab_4_oop\Controller.java src\main\java\com\example\lab_4_oop\Main.java

if %ERRORLEVEL% equ 0 (
    echo Compilation successful! Starting application...
    echo.
    echo Features:
    echo - App icon in window title bar
    echo - Icons for all tools and shapes (loaded programmatically)
    echo - Click to select shapes
    echo - Click on selected shape to keep it selected
    echo - Drag to move all selected shapes
    echo - Shift + drag to resize all selected shapes (smooth resizing)
    echo - For lines: click on end to resize/rotate, click on middle to move
    echo - Group operations work correctly
    echo - Canvas resizes with window
    echo - Ctrl+A to select all, Delete to remove
    echo - +/- keys to resize selected shapes
    echo - Arrow keys to move selected shapes
    echo.
    java -cp "target/classes;C:\Users\zagid\.m2\repository\org\openjfx\javafx-controls\17.0.6\javafx-controls-17.0.6.jar;C:\Users\zagid\.m2\repository\org\openjfx\javafx-graphics\17.0.6\javafx-graphics-17.0.6.jar;C:\Users\zagid\.m2\repository\org\openjfx\javafx-base\17.0.6\javafx-base-17.0.6.jar;C:\Users\zagid\.m2\repository\org\openjfx\javafx-fxml\17.0.6\javafx-fxml-17.0.6.jar;C:\Users\zagid\OneDrive\Рабочий стол\Java\JavaFX\JavaFX\javafx-sdk-24.0.2\lib\javafx.controls.jar;C:\Users\zagid\OneDrive\Рабочий стол\Java\JavaFX\JavaFX\javafx-sdk-24.0.2\lib\javafx.graphics.jar;C:\Users\zagid\OneDrive\Рабочий стол\Java\JavaFX\JavaFX\javafx-sdk-24.0.2\lib\javafx.fxml.jar;C:\Users\zagid\OneDrive\Рабочий стол\Java\JavaFX\JavaFX\javafx-sdk-24.0.2\lib\javafx.base.jar" com.example.lab_4_oop.Main
) else (
    echo Compilation failed!
)
pause
