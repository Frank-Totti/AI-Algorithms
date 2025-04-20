@echo off
echo === Compilando el servidor Java...

javac -cp ".;backend/lib/gson-2.8.9.jar;backend/lib/json-20231013.jar" ^
      backend/src/DroneServer.java backend/src/algorithms.java ^
      -d backend/bin

if %ERRORLEVEL% NEQ 0 (
    echo Error al compilar.
    pause
    exit /b
)

echo === Ejecutando el servidor...
start cmd /k java -cp "backend/bin;backend/lib/gson-2.8.9.jar;backend/lib/json-20231013.jar" DroneServer

timeout /t 1 >nul

echo === Abriendo index.html en el navegador...
start "" "frontend/index.html"
