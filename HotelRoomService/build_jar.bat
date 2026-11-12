@echo off
rem Rebuilds HotelApp.jar after you edit the Java files in src\hotel (needs a JDK 11 or newer)
cd /d "%~dp0"
if exist build rmdir /s /q build
mkdir build
javac -encoding UTF-8 -d build src\hotel\model\*.java src\hotel\data\*.java src\hotel\ui\*.java src\hotel\app\*.java || (echo Compile failed & pause & exit /b 1)
jar cf HotelApp.jar -C build .
rmdir /s /q build
echo HotelApp.jar rebuilt. Now run build_exe.bat to make the .exe files.
pause
