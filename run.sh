#!/bin/bash

# Verifica si el script es ejecutable y se da permisos si no lo es
if [ ! -x "$0" ]; then
    chmod +x "$0"
    echo "Permisos actualizados. Volvé a ejecutar el script."
    exit 0
fi

# Crear carpeta de salida si no existe
mkdir -p backend/out

# Compilar los archivos .java
javac -cp "backend/lib/*" -d backend/out backend/src/*.java

# Ejecutar el servidor
java -cp "backend/lib/*:backend/out" DroneServer &

# Abrir el index.html en el navegador por defecto
xdg-open frontend/index.html

echo "¡Todo listo! Servidor iniciado y frontend abierto 🚀"

