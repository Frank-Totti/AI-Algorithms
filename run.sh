#!/bin/bash

if [ ! -x "$0" ]; then
    chmod +x "$0"
    echo "Permisos actualizados. Volvé a ejecutar el script."
    exit 0
fi


mkdir -p backend/out


javac -cp "backend/lib/*" -d backend/out backend/src/*.java


java -cp "backend/lib/*:backend/out" DroneServer &


xdg-open frontend/index.html

echo "¡Todo listo! Servidor iniciado y frontend abierto 🚀"

