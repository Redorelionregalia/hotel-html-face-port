@echo off
rem Makes Customer.exe and Staff.exe from HotelApp.jar using Launch4j (free: https://launch4j.sourceforge.net)
set L4J=
if exist "C:\Program Files (x86)\Launch4j\launch4jc.exe" set L4J=C:\Program Files (x86)\Launch4j\launch4jc.exe
if exist "C:\Program Files\Launch4j\launch4jc.exe" set L4J=C:\Program Files\Launch4j\launch4jc.exe
if "%L4J%"=="" (
  echo Launch4j was not found. Install it, or edit the L4J path in this file.
  pause
  exit /b 1
)
"%L4J%" "%~dp0customer-exe.xml"
"%L4J%" "%~dp0staff-exe.xml"
echo.
echo Done. Customer.exe and Staff.exe are in this folder.
pause
