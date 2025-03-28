// Variables globales
let currentMap = [];
let dronePath = [];
let currentStep = 0;
let animationInterval = null;
let dronePosition = [0, 0];
let animationSpeed = 500;

// Elementos del DOM
const gridElement = document.getElementById('grid');
const mapDataElement = document.getElementById('map-data');
const loadBtn = document.getElementById('load-btn');
const algorithmTypeSelect = document.getElementById('algorithm-type');
const algorithmSelect = document.getElementById('algorithm');
const runBtn = document.getElementById('run-btn');
const resultsElement = document.getElementById('results');
const initialMessage = document.getElementById('initial-message');
const resultsContent = document.getElementById('results-content');
const statsElement = document.getElementById('stats');
const pathDisplay = document.getElementById('path-display');
const animateBtn = document.getElementById('animate-btn');
const stopBtn = document.getElementById('stop-btn');
const speedControl = document.getElementById('speed');
const speedValue = document.getElementById('speed-value');
const mapFilesSelect = document.getElementById('map-files');
const loadFileBtn = document.getElementById('load-file-btn');
const uploadFileInput = document.getElementById('upload-file');
const textTab = document.getElementById('text-tab');
const fileTab = document.getElementById('file-tab');
const tabButtons = document.querySelectorAll('.tab-button');

// Event listeners
loadBtn.addEventListener('click', loadMapFromText);
loadFileBtn.addEventListener('click', loadMapFromFile);
uploadFileInput.addEventListener('change', handleFileUpload);
algorithmTypeSelect.addEventListener('change', updateAlgorithmOptions);
runBtn.addEventListener('click', runAlgorithm);
animateBtn.addEventListener('click', startAnimation);
stopBtn.addEventListener('click', stopAnimation);
speedControl.addEventListener('input', updateAnimationSpeed);

// Tab functionality
tabButtons.forEach(button => {
    button.addEventListener('click', () => {
        const tabId = button.getAttribute('data-tab');
        
        // Update active tab button
        tabButtons.forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');
        
        // Update active tab content
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
        });
        document.getElementById(tabId).classList.add('active');
    });
});

// Cargar lista de mapas disponibles
function loadAvailableMaps() {
    fetch('http://localhost:8080/list-maps', {
        mode: 'cors'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al obtener lista de mapas');
            }
            return response.json();
        })
        .then(files => {
            mapFilesSelect.innerHTML = '<option value="">Selecciona un archivo</option>';
            files.forEach(file => {
                const option = document.createElement('option');
                option.value = file;
                option.textContent = file;
                mapFilesSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error al cargar lista de mapas:', error);
            showNotification('Error al cargar lista de mapas: ' + error.message, 'error');
        });
}

// Manejar carga de archivo
function handleFileUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.name.toLowerCase().endsWith('.txt')) {
        showNotification("Solo se permiten archivos .txt", "error");
        return;
    }

    const reader = new FileReader();
    reader.onload = function(e) {
        // Enviar contenido como texto plano
        fetch('http://localhost:8080/upload-map', {
            mode: 'cors',
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain'
            },
            body: e.target.result
        })
        .then(response => {
            if (!response.ok) throw new Error("Error al subir archivo");
            return response.json();
        })
        .then(data => {
            mapDataElement.value = e.target.result;
            loadAvailableMaps();
            showNotification(`Archivo ${file.name} subido correctamente`, "success");
        })
        .catch(error => {
            showNotification(error.message, "error");
        });
    };
    reader.readAsText(file);
}
// Cargar mapa desde archivo seleccionado
// Modifica loadMapFromFile para manejar mejor los errores
function loadMapFromFile() {
    const filename = mapFilesSelect.value;
    if (!filename) {
        showNotification('Por favor selecciona un archivo', 'error');
        return;
    }
    
    fetch(`http://localhost:8080/load-map-file?filename=${encodeURIComponent(filename)}`, {
        mode: 'cors'
        
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { 
                try {
                    const data = JSON.parse(text);
                    throw new Error(data.error || text);
                } catch {
                    throw new Error(text);
                }
            });
        }
        return response.text();
    })
    .then(content => {
        mapDataElement.value = content;
        loadMapFromText();
    })
    .catch(error => {
        showNotification('Error al cargar el archivo: ' + error.message, 'error');
    });
}

// Cargar mapa desde texto
function loadMapFromText() {
    const mapData = mapDataElement.value.trim();
    if (!mapData) {
        showNotification("Por favor ingresa los datos del mapa", "error");
        return;
    }

    loadBtn.disabled = true;
    loadBtn.textContent = "Validando...";

    fetch('http://localhost:8080/load-map', {
        mode: 'cors',
        method: 'POST',
        headers: {
            'Content-Type': 'text/plain'
        },
        body: mapData
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                try {
                    const data = JSON.parse(text);
                    throw new Error(data.error || text);
                } catch {
                    throw new Error(text);
                }
            });
        }
        return response.json();
    })
    .then(data => {
        const rows = mapData.split('\n');
        currentMap = rows
            .map(row => row.trim().split(/\s+/).map(cell => parseInt(cell)))
            .filter(row => row.length > 0);
        
        if (currentMap.length === 0) {
            throw new Error("El mapa está vacío después del procesamiento");
        }
        
        findDronePosition();
        renderGrid();
        runBtn.disabled = false;
        showNotification(data.message || "Mapa cargado correctamente", "success");
    })
    .catch(error => {
        showNotification("Error al cargar el mapa: " + error.message, "error");
        console.error("Error detallado:", error);
        currentMap = [];
        renderGrid();
    })
    .finally(() => {
        loadBtn.disabled = false;
        loadBtn.textContent = "Cargar Mapa";
    });
}

// Mostrar notificación
function showNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.classList.add('fade-out');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 3000);
}

// Encontrar posición inicial del dron
function findDronePosition() {
    for (let i = 0; i < currentMap.length; i++) {
        for (let j = 0; j < currentMap[i].length; j++) {
            if (currentMap[i][j] === 2) {
                dronePosition = [i, j];
                return;
            }
        }
    }
    dronePosition = [0, 0]; // Por defecto si no se encuentra
}

// Renderizar el grid
function renderGrid(highlightPosition = null) {
    gridElement.innerHTML = '';
    
    // Verificar si currentMap es válido
    if (!currentMap || currentMap.length === 0 || !currentMap[0] || currentMap[0].length === 0) {
        gridElement.style.gridTemplateColumns = '1fr';
        const messageCell = document.createElement('div');
        messageCell.className = 'cell';
        messageCell.textContent = 'No hay mapa cargado';
        messageCell.style.gridColumn = '1 / -1';
        gridElement.appendChild(messageCell);
        return;
    }
    
    gridElement.style.gridTemplateColumns = `repeat(${currentMap[0].length}, 1fr)`;
    
    for (let i = 0; i < currentMap.length; i++) {
        for (let j = 0; j < currentMap[i].length; j++) {
            const cell = document.createElement('div');
            cell.className = `cell cell-${currentMap[i][j]}`;
            
            if (highlightPosition && highlightPosition[0] === i && highlightPosition[1] === j) {
                cell.classList.add('drone');
            }
            
            switch(currentMap[i][j]) {
                case 0: cell.textContent = ''; break;
                case 1: cell.textContent = '■'; break;
                case 2: cell.textContent = '✈'; break;
                case 3: cell.textContent = '⚡'; break;
                case 4: cell.textContent = '📦'; break;
                default: cell.textContent = '?'; // Para valores inválidos
            }
            
            gridElement.appendChild(cell);
        }
    }
}

// Actualizar opciones de algoritmos
function updateAlgorithmOptions() {
    algorithmSelect.innerHTML = '<option value="">-- Selecciona algoritmo --</option>';
    
    if (algorithmTypeSelect.value === 'uninformed') {
        algorithmSelect.innerHTML += `
            <option value="amplitude">Amplitud</option>
            <option value="uniform-cost">Costo Uniforme</option>
            <option value="depth">Profundidad (evitando ciclos)</option>
        `;
    } else if (algorithmTypeSelect.value === 'informed') {
        algorithmSelect.innerHTML += `
            <option value="greedy">Avara</option>
            <option value="a-star">A*</option>
        `;
    }
    
    algorithmSelect.disabled = algorithmTypeSelect.value === '';
    runBtn.disabled = algorithmSelect.value === '';
}

async function runAlgorithm() {
    const algorithm = algorithmSelect.value;
    if (!algorithm) return;

    stopAnimation();
    
    runBtn.disabled = true;
    runBtn.textContent = "Ejecutando...";
    
    try {
        const queryParams = new URLSearchParams();
        queryParams.append('algorithm', algorithm);
        queryParams.append('map', currentMap.map(row => row.join(' ')).join('\n'));
        
        const response = await fetch(`http://localhost:8080/run-algorithm?${queryParams.toString()}`, {
            mode: 'cors',
            headers: {
                'Accept': 'application/json'
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Error HTTP: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (!data || !data.path) {
            throw new Error("El servidor no devolvió un camino válido");
        }
        
        // Asegurarse de que los datos tengan el formato esperado
        const processedData = {
            algorithm: algorithm,
            path: data.path,
            nodesExpanded: data.nodesExpanded || 0,
            depth: data.depth || 0,
            time: `${data.executionTime || 0} ms`,
            cost: data.cost || 0
        };
        
        processResults(processedData);
    } catch (error) {
        console.error("Error detallado:", error);
        showNotification("Error al ejecutar el algoritmo: " + error.message, "error");
    } finally {
        runBtn.disabled = false;
        runBtn.textContent = "Ejecutar Algoritmo";
    }
}
// Procesar resultados del algoritmo
function processResults(data) {
    dronePath = data.path;
    currentStep = 0;
    
    // Mostrar resultados
    initialMessage.style.display = 'none';
    resultsContent.style.display = 'block';
    
    // Mostrar estadísticas
    statsElement.innerHTML = `
        <div class="stat-item">
            <strong>Algoritmo:</strong> ${getAlgorithmName(data.algorithm)}
        </div>
        <div class="stat-item">
            <strong>Nodos expandidos:</strong> ${data.nodesExpanded}
        </div>
        <div class="stat-item">
            <strong>Profundidad del árbol:</strong> ${data.depth}
        </div>
        <div class="stat-item">
            <strong>Tiempo de cómputo:</strong> ${data.time}
        </div>
        ${data.cost ? `
        <div class="stat-item">
            <strong>Costo de la solución:</strong> ${data.cost}
        </div>
        ` : ''}
    `;
    
    // Mostrar camino
    pathDisplay.innerHTML = data.path.map(move => 
        `<span class="path-move">${move}</span>`
    ).join(' → ');
    
    // Configurar botón de animación
    animateBtn.onclick = startAnimation;
    animateBtn.textContent = "Ver Animación";
    animateBtn.disabled = false;
    stopBtn.disabled = true;
}

// Obtener nombre completo del algoritmo
function getAlgorithmName(algo) {
    const names = {
        'amplitude': 'Búsqueda en Amplitud',
        'uniform-cost': 'Costo Uniforme',
        'depth': 'Búsqueda en Profundidad',
        'greedy': 'Búsqueda Avara',
        'a-star': 'Algoritmo A*'
    };
    return names[algo] || algo;
}

// Iniciar animación del dron
function startAnimation() {
    stopAnimation();
    
    animateBtn.disabled = true;
    stopBtn.disabled = false;
    animateBtn.textContent = "Animando...";
    
    // Calcular todas las posiciones del camino
    let animationSteps = calculateAnimationSteps();
    let step = 0;
    
    // Ejecutar primer paso inmediatamente
    renderGrid(animationSteps[step]);
    step++;
    
    // Continuar con el intervalo
    animationInterval = setInterval(() => {
        if (step >= animationSteps.length) {
            stopAnimation();
            animateBtn.textContent = "Repetir Animación";
            animateBtn.disabled = false;
            return;
        }
        
        renderGrid(animationSteps[step]);
        step++;
    }, animationSpeed);
}

// Calcular pasos de animación
function calculateAnimationSteps() {
    let steps = [];
    let currentPos = [...dronePosition];
    steps.push([...currentPos]);
    
    for (const move of dronePath) {
        switch(move) {
            case "UP": currentPos[0]--; break;
            case "DOWN": currentPos[0]++; break;
            case "LEFT": currentPos[1]--; break;
            case "RIGHT": currentPos[1]++; break;
        }
        steps.push([...currentPos]);
    }
    
    return steps;
}

// Detener animación
function stopAnimation() {
    if (animationInterval) {
        clearInterval(animationInterval);
        animationInterval = null;
    }
    
    animateBtn.disabled = false;
    stopBtn.disabled = true;
}

// Actualizar velocidad de animación
function updateAnimationSpeed() {
    animationSpeed = 2100 - speedControl.value; // Invertir el valor (100-2000 -> 2000-100)
    speedValue.textContent = animationSpeed + ' ms';
}

// Cargar mapa inicial al abrir la página
document.addEventListener('DOMContentLoaded', () => {
    // Cargar mapa de ejemplo
    mapDataElement.value = `1 1 0 0 0 0 0 1 1 1
1 1 0 1 0 1 0 1 1 1
0 2 0 3 4 4 0 0 0 0
0 1 1 1 0 1 1 1 1 0
0 1 1 1 0 0 0 0 0 0
3 3 0 1 0 1 1 1 1 1
1 1 0 1 0 0 0 0 0 0
1 1 0 1 1 1 1 1 1 0
1 1 0 0 0 0 4 0 0 0
1 1 1 1 1 1 1 1 1 1`;
    
    // Cargar lista de mapas disponibles
    loadAvailableMaps();
});