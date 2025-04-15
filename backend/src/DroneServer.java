import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;

public class DroneServer {
    private static final int PORT = 8080;
    private static final String MAPS_DIR = System.getProperty("user.dir") + "/maps/";
    private static ServerSocket serverSocket;
    private static boolean running = true;

    public static void main(String[] args) {
        try {
            // Crear directorio maps si no existe
            Files.createDirectories(Paths.get(MAPS_DIR));

            serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor iniciado en http://localhost:" + PORT);

            while (running) {
                try (Socket clientSocket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String requestLine = in.readLine();
                    if (requestLine == null)
                        continue;

                    String[] requestParts = requestLine.split(" ");
                    if (requestParts.length < 2)
                        continue;

                    String method = requestParts[0];
                    String path = requestParts[1];

                    // Leer headers
                    Map<String, String> headers = new HashMap<>();
                    String headerLine;
                    while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                        String[] headerParts = headerLine.split(": ", 2);
                        if (headerParts.length == 2) {
                            headers.put(headerParts[0], headerParts[1]);
                        }
                    }

                    // Manejar preflight OPTIONS request
                    if (method.equals("OPTIONS")) {
                        out.println(buildCORSResponse());
                        continue;
                    }

                    // Leer body si existe
                    StringBuilder body = new StringBuilder();
                    if (headers.containsKey("Content-Length")) {
                        int contentLength = Integer.parseInt(headers.get("Content-Length"));
                        char[] buffer = new char[contentLength];
                        in.read(buffer, 0, contentLength);
                        body.append(buffer);
                    }

                    // Procesar la solicitud
                    String response;
                    if (path.equals("/load-map-file") && method.equals("POST")) {
                        response = handleLoadMapFile(body.toString());
                    } else if (path.equals("/load-map") && method.equals("POST")) {
                        response = handleLoadMap(body.toString());
                    } else if (path.equals("/run-algorithm") && method.equals("POST")) {
                        response = handleRunAlgorithm(body.toString());
                    } else if (path.equals("/list-maps") && method.equals("GET")) {
                        response = handleListMaps();
                    } else if (path.equals("/upload-map") && method.equals("POST")) {
                        response = handleUploadMap(body.toString());
                    } else {
                        response = buildErrorResponse("Ruta no encontrada", 404);
                    }

                    // Enviar respuesta
                    out.println(response);
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error en la conexión: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
        }
    }

    private static String buildCORSResponse() {
        return "HTTP/1.1 204 No Content\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Content-Type\r\n" +
                "Access-Control-Max-Age: 86400\r\n" +
                "\r\n";
    }

    private static String buildResponse(String content, String contentType) {
        return "HTTP/1.1 200 OK\r\n" +
                "Content-Type: " + contentType + "\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "\r\n" + content;
    }

    private static String buildErrorResponse(String errorMessage, int statusCode) {
        return "HTTP/1.1 " + statusCode + " Error\r\n" +
                "Content-Type: application/json\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "\r\n" +
                "{\"error\":\"" + errorMessage + "\"}";
    }

    private static String handleLoadMapFile(String path) {
        try {
            Path filePath = Paths.get(MAPS_DIR + path.trim());
            if (!Files.exists(filePath)) {
                return buildErrorResponse("Archivo no encontrado", 404);
            }

            String content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            return buildResponse(content, "text/plain");
        } catch (Exception e) {
            return buildErrorResponse("Error al leer el archivo: " + e.getMessage(), 500);
        }
    }

    private static String handleUploadMap(String body) {
        try {
            String filename = "mapa_" + System.currentTimeMillis() + ".txt";
            Path filePath = Paths.get(MAPS_DIR + filename);
            Files.write(filePath, body.getBytes(StandardCharsets.UTF_8));

            return buildResponse("{\"status\":\"success\", \"message\":\"Mapa guardado\"}", "application/json");
        } catch (Exception e) {
            return buildErrorResponse("Error procesando el mapa: " + e.getMessage(), 500);
        }
    }

    private static String handleListMaps() {
        try {
            File mapsDir = new File(MAPS_DIR);
            File[] files = mapsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

            List<String> mapFiles = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    mapFiles.add(file.getName());
                }
            }

            return buildResponse(new Gson().toJson(mapFiles), "application/json");
        } catch (Exception e) {
            return buildErrorResponse("Error al listar mapas", 500);
        }
    }

    private static String handleLoadMap(String body) {
        try {
            if (body == null || body.trim().isEmpty()) {
                return buildErrorResponse("El contenido del mapa no puede estar vacío", 400);
            }

            String[] rows = body.split("\n");
            if (rows.length != 10) {
                return buildErrorResponse("El mapa debe tener exactamente 10 filas", 400);
            }

            for (int i = 0; i < rows.length; i++) {
                String row = rows[i].trim();
                if (row.isEmpty()) {
                    return buildErrorResponse("La fila " + (i + 1) + " está vacía", 400);
                }
            }

            return buildResponse("{\"status\":\"success\", \"message\":\"Mapa válido\"}", "application/json");
        } catch (Exception e) {
            return buildErrorResponse("Error procesando el mapa: " + e.getMessage(), 500);
        }
    }

    private static String handleRunAlgorithm(String body) {
        try {
            // Parsear el cuerpo JSON
            Gson gson = new Gson();
            Map<String, String> requestData = gson.fromJson(body, Map.class);

            String algorithm = requestData.get("algorithm");
            String mapData = requestData.get("map");

            if (algorithm == null || mapData == null) {
                return buildErrorResponse("Parámetros algorithm y map son requeridos", 400);
            }

            // Convertir el mapa de texto a estructura LinkedList<LinkedList<Integer>>
            LinkedList<LinkedList<Integer>> matrix = parseMapData(mapData);
            LinkedList<String> operators = new LinkedList<>(Arrays.asList("UP", "DOWN", "RIGHT", "LEFT"));

            // Ejecutar el algoritmo correspondiente
            LinkedList<String> pathResult;
            long startTime = System.currentTimeMillis();

            switch (algorithm) {
                case "amplitude":
                    pathResult = algorithms.AmplitudeSearch(matrix, operators, false);
                    break;
                case "uniform-cost":
                    pathResult = algorithms.uniformCostSearch(matrix, operators, false);
                    break;
                case "depth":
                    pathResult = algorithms.DeepSearch(matrix, operators, false);
                    break;
                case "greedy":
                    pathResult = algorithms.AvaraSearch(matrix, operators, false);
                    break;
                case "a-star":
                    pathResult = algorithms.AStarSearch(matrix, operators, false);
                    break;
                default:
                    return buildErrorResponse("Algoritmo no válido", 400);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            // Calcular métricas
            int nodesExpanded = (int) (pathResult.size() * 1.5); // Estimación
            int depth = pathResult.size();
            int cost = calculatePathCost(pathResult, matrix);

            // Crear objeto de resultado
            algorithms.SearchResult result = new algorithms.SearchResult(
                    pathResult,
                    nodesExpanded,
                    depth,
                    executionTime,
                    cost);

            return buildResponse(gson.toJson(result), "application/json");

        } catch (Exception e) {
            e.printStackTrace();
            return buildErrorResponse("Error al ejecutar el algoritmo: " + e.getMessage(), 500);
        }
    }

    private static LinkedList<LinkedList<Integer>> parseMapData(String mapData) {
        LinkedList<LinkedList<Integer>> matrix = new LinkedList<>();
        String[] rows = mapData.split("\n");
        for (String row : rows) {
            LinkedList<Integer> rowList = new LinkedList<>();
            String[] cells = row.trim().split("\\s+");
            for (String cell : cells) {
                rowList.add(Integer.parseInt(cell));
            }
            matrix.add(rowList);
        }
        return matrix;
    }

    private static int calculatePathCost(LinkedList<String> path, LinkedList<LinkedList<Integer>> matrix) {
        // Implementación simplificada - deberías rastrear el costo real durante la
        // ejecución del algoritmo
        int cost = 0;
        int[] pos = findInitialPosition(matrix);

        for (String move : path) {
            switch (move) {
                case "UP":
                    pos[0]--;
                    break;
                case "DOWN":
                    pos[0]++;
                    break;
                case "LEFT":
                    pos[1]--;
                    break;
                case "RIGHT":
                    pos[1]++;
                    break;
            }
            int cellValue = matrix.get(pos[0]).get(pos[1]);
            cost += (cellValue == 3) ? 8 : 1; // Campos electromagnéticos cuestan 8
        }

        return cost;
    }

    private static int[] findInitialPosition(LinkedList<LinkedList<Integer>> matrix) {
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.get(i).size(); j++) {
                if (matrix.get(i).get(j) == 2) {
                    return new int[] { i, j };
                }
            }
        }
        return new int[] { 0, 0 };
    }
}
