import java.util.*;
import java.util.stream.*;

// 0 -> casilla libre ; 2 -> punto inicio del dron ; 3 -> campo electromagnetico ; 4 -> paquete

public class algorithms{


    // Maneja los algoritmos que no miden costos
    public static class Node{

        private Node father;

        private LinkedList<Integer> position = new LinkedList<>();

        private Integer state;

        private String operator;

        public Node(Node father, LinkedList<Integer> position, Integer state, String operator){
            this.father = father;
            this.position = position;
            this.state = state;
            this.operator = operator;
        }

        public Node(Node father, LinkedList<Integer> position, Integer state){
            this.father = father;
            this.position = position;
            this.state = state;
        }


        public Node(){
        }

        public void SetFather(Node father){
            this.father = father;
        }

        public void SetPosition(LinkedList<Integer> position){
            this.position = position;
        }

        public Node getFather(){
            return this.father;
        }

        public LinkedList<Integer> getPosition(){
            return this.position;
        }

        public void SetState(Integer state){
            this.state = state;
        }

        public Integer getState(){
            return this.state;
        }

        public boolean isFather(Node node){
            return node == this.father;
        }

        public String getOperator(){
            return this.operator;
        }

        public void SetOperator(String operator){
            this.operator = operator;
        }

        @Override
        public String toString() {
            return "("  + this.position + " " + this.state + " " + this.operator + ")" ;
        }       

    }
    // Maneja los algoritmos que miden costos
    public static class weightedNode extends Node {

        private Integer weight;

        public weightedNode(Node father, LinkedList<Integer> position, Integer state, String operator, Integer weight) {
            super(father, position, state, operator); 
            this.weight = weight;
        }

        public weightedNode(){
        }

        public void SetWeight(Integer weight){
            this.weight = weight;
        }

        public Integer getWeight(){
            return this.weight;
        }

        @Override
        public String toString() {
            return super.toString() + " Weight: " + this.weight;
        }

        @Override
        public weightedNode getFather() {
            return (weightedNode) super.getFather(); // Hacer cast en la sobrescritura
        }  

    }

    public static Node findInitialPosition(LinkedList<LinkedList<Integer>> matrix){

        LinkedList<Integer> initPosition = new LinkedList<>();
        Node initNode = new Node();
        initNode.SetFather(null);
        initNode.SetState(0);
        initNode.SetOperator(null);

        for(int i = 0; i < matrix.size(); i++){
            for(int j = 0; j < matrix.get(i).size(); j++){

                if (matrix.get(i).get(j) == 2){ 
                    initPosition.add(i);
                    initPosition.add(j);
                    initNode.SetPosition(initPosition);
                    return initNode;
                }

            }
        }
        return initNode;
    }

    public static weightedNode findInitialPosition_weiNode(LinkedList<LinkedList<Integer>> matrix){

        LinkedList<Integer> initPosition = new LinkedList<>();
        weightedNode initNode = new weightedNode();
        initNode.SetFather(null);
        initNode.SetState(0);
        initNode.SetOperator(null);
        initNode.SetWeight(0);

        for(int i = 0; i < matrix.size(); i++){
            for(int j = 0; j < matrix.get(i).size(); j++){

                if (matrix.get(i).get(j) == 2){ 
                    initPosition.add(i);
                    initPosition.add(j);
                    initNode.SetPosition(initPosition);
                    return initNode;
                }

            }
        }
        return initNode;
    }

    public static LinkedList<LinkedList<Integer>> findBoxes(LinkedList<LinkedList<Integer>> matrix){

        LinkedList<LinkedList<Integer>> positions = new LinkedList<>();

        for(int i = 0; i < matrix.size(); i++){
            for(int j = 0; j < matrix.get(i).size(); j++){

                if (matrix.get(i).get(j) == 4){
                    LinkedList<Integer> temp = new LinkedList<>();
                    temp.add(i);
                    temp.add(j);
                    positions.add(temp);
                }

            }
        }
        return positions;
    }

    public static LinkedList<Node> allFathers(Node node){

        LinkedList<Node> fathers = new LinkedList<>();

        Node actualNode = node;

        while(actualNode != null && actualNode.getFather() != null){

            fathers.add(actualNode.getFather());
            actualNode = actualNode.getFather();

        }

        return fathers;

    }

    public static LinkedList<weightedNode> allFathers_weigthed(weightedNode node){

        LinkedList<weightedNode> fathers = new LinkedList<>();

        weightedNode actualNode = node;

        while(actualNode != null && actualNode.getFather() != null){

            fathers.add(actualNode.getFather());
            actualNode = actualNode.getFather();
        }

        return fathers;

    }

    public static boolean possibleNode(LinkedList<Node> fathers, Node node, LinkedList<Integer> position){

        boolean result = true;

        for (Node actual : fathers){
            if (position.equals(actual.getPosition())){
                if(node.getState().equals(actual.getState()))

                    result = false;

            }
        }

        return result;

    }

    public static boolean possibleNode_weigted(LinkedList<weightedNode> fathers, weightedNode node, LinkedList<Integer> position){

        boolean result = true;

        for (Node actual : fathers){
            if (position.equals(actual.getPosition())){
                if(Objects.equals(node.getState(), actual.getState()))

                result = false;

            }
        }

        return result;

    }

    public static Node evalFathers(LinkedList<Node> fathers, Node actualPosition, LinkedList<Integer> position, String operator){

        if(possibleNode(fathers, actualPosition, position)){

            LinkedList<Integer> newPos = new LinkedList<>(position);
            Node node = new Node(actualPosition,newPos,actualPosition.getState(),operator);

            return node;

        } 

        return null;

    }

    public static weightedNode evalFathers_weigted(LinkedList<weightedNode> fathers, weightedNode actualPosition, 
    LinkedList<Integer> position, String operator, LinkedList<LinkedList<Integer>> matrix){

        if(possibleNode_weigted(fathers, actualPosition, position)){

            LinkedList<Integer> newPos = new LinkedList<>(position);
            weightedNode node = createWeigtedNode(actualPosition, newPos, operator, matrix);

            return node;

        } 

        return null;

    }

    public static weightedNode createWeigtedNode (weightedNode actualPosition, LinkedList<Integer> newPos, String operator,LinkedList<LinkedList<Integer>> matrix){

        weightedNode node = new weightedNode(actualPosition, newPos, actualPosition.getState(), operator, actualPosition.getWeight());

        if(matrix.get(newPos.get(0)).get(newPos.get(1)) == 3){
            node.SetWeight(actualPosition.getWeight() + 8);
        } else {
            node.SetWeight(actualPosition.getWeight() + 1);
        }

        return node;

    }

    public static LinkedList<weightedNode> getSons_weigtedNode(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, 
    weightedNode actualPosition, boolean back){

        LinkedList<weightedNode> possibleMovements = new LinkedList<>();

        for(int i = 0; i < operators.size(); i++){

            Integer fill = actualPosition.getPosition().get(0);
            Integer row = actualPosition.getPosition().get(1);
            LinkedList<Integer> position = new LinkedList<>();

            LinkedList<weightedNode> fathers = allFathers_weigthed(actualPosition);

            switch (operators.get(i)) {
                case "UP" -> {
                    if(fill > 0 && matrix.get(fill - 1).get(row) != 1 ){
                        position.add(fill - 1);
                        position.add(row);
                        if (back){
                            LinkedList<Integer> newPos = new LinkedList<>(position);
                            weightedNode node = createWeigtedNode(actualPosition, newPos, "UP", matrix);
                            possibleMovements.add(node);
                        } else {
                            weightedNode node = evalFathers_weigted(fathers, actualPosition, position,"UP",matrix);

                            if (node != null){

                                possibleMovements.add(node);

                            }
                        }
                    }
                    position.clear();
                }

                case "DOWN" -> {
                    if(fill + 1 < matrix.size() && matrix.get(fill + 1).get(row) != 1){
                        position.add(fill + 1);
                        position.add(row);

                        if (back){
                            LinkedList<Integer> newPos = new LinkedList<>(position);
                            weightedNode node = createWeigtedNode(actualPosition, newPos, "DOWN", matrix);
                            possibleMovements.add(node);
                        } else {
                            weightedNode node = evalFathers_weigted(fathers, actualPosition, position,"DOWN",matrix);

                            if (node != null){

                                possibleMovements.add(node);

                            }
                        }

                    }
                    position.clear();
                }
                
                case "RIGHT" -> {
                    if(row + 1 < matrix.get(fill).size() && matrix.get(fill).get(row + 1) != 1){
                        position.add(fill);
                        position.add(row + 1);

                        if (back){
                            LinkedList<Integer> newPos = new LinkedList<>(position);
                            weightedNode node = createWeigtedNode(actualPosition, newPos, "RIGHT", matrix);
                            possibleMovements.add(node);
                        } else {
                            weightedNode node = evalFathers_weigted(fathers, actualPosition, position,"RIGHT",matrix);

                            if (node != null){

                                possibleMovements.add(node);

                            }
                        }
                    }
                    position.clear();
                }

                case "LEFT" -> {
                    if(row > 0 && matrix.get(fill).get(row - 1) != 1){
                        position.add(fill);
                        position.add(row - 1);

                        if (back){
                            LinkedList<Integer> newPos = new LinkedList<>(position);
                            weightedNode node = createWeigtedNode(actualPosition, newPos, "LEFT", matrix);
                            possibleMovements.add(node);
                        } else {
                            weightedNode node = evalFathers_weigted(fathers, actualPosition, position,"LEFT",matrix);

                            if (node != null){

                                possibleMovements.add(node);

                            }
                        }
                    }
                    position.clear();
                }
                    
                default -> throw new AssertionError();
            }
            
        }

    return possibleMovements;

    }

    public static LinkedList<Node> getSons_node(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, Node actualPosition, boolean back){

        LinkedList<Node> possibleMovements = new LinkedList<>();

        for(int i = 0; i < operators.size(); i++){

            Integer fill = actualPosition.getPosition().get(0);
            Integer row = actualPosition.getPosition().get(1);
            LinkedList<Integer> position = new LinkedList<>();

            LinkedList<Node> fathers = allFathers(actualPosition);

            switch (operators.get(i)) {
                case "UP" -> {
                    if(fill > 0 && matrix.get(fill - 1).get(row) != 1 ){
                        position.add(fill - 1);
                        position.add(row);

                        if (back){
                            LinkedList<Integer> newPos = new LinkedList<>(position);
                            Node node = new Node(actualPosition,newPos,actualPosition.getState(),"UP");
                            possibleMovements.add(node);
                        } else {
                            Node node = evalFathers(fathers, actualPosition, position,"UP");

                            if (node != null){

                                possibleMovements.add(node);

                            }
                        }
                    }
                    position.clear();
                }

                case "DOWN" -> {
                    if(fill + 1 < matrix.size() && matrix.get(fill + 1).get(row) != 1){
                        position.add(fill + 1);
                        position.add(row);

                        if (back){
                            LinkedList<Integer> newPos = new LinkedList<>(position);
                            Node node = new Node(actualPosition,newPos,actualPosition.getState(),"DOWN");
                            possibleMovements.add(node);
                        } else {
                            Node node = evalFathers(fathers, actualPosition, position,"DOWN");

                            if (node != null){

                                possibleMovements.add(node);

                            }
                        }

                    }
                    position.clear();
                }
                
                case "RIGHT" -> {
                    if(row + 1 < matrix.get(fill).size() && matrix.get(fill).get(row + 1) != 1){
                        position.add(fill);
                        position.add(row + 1);

                        if (back){
                            LinkedList<Integer> newPos = new LinkedList<>(position);
                            Node node = new Node(actualPosition,newPos,actualPosition.getState(),"RIGHT");
                            possibleMovements.add(node);
                        } else {
                            Node node = evalFathers(fathers, actualPosition, position,"RIGHT");

                            if (node != null){

                                possibleMovements.add(node);

                            }
                        }
                    }
                    position.clear();
                }

                case "LEFT" -> {
                    if(row > 0 && matrix.get(fill).get(row - 1) != 1){
                        position.add(fill);
                        position.add(row - 1);

                        if (back){
                            LinkedList<Integer> newPos = new LinkedList<>(position);
                            Node node = new Node(actualPosition,newPos,actualPosition.getState(),"LEFT");
                            possibleMovements.add(node);
                        } else {
                            Node node = evalFathers(fathers, actualPosition, position,"LEFT");

                            if (node != null){

                                possibleMovements.add(node);

                            }
                        }
                    }
                    position.clear();
                }
                    
                default -> throw new AssertionError();
            }
            
        }

    return possibleMovements;

    }

    private static LinkedList<String> reconstructPath (Node lastNode) {

        LinkedList<String> path = new LinkedList<>();
        Node temp = lastNode;

        while (temp != null && temp.getOperator() != null) {
            path.addFirst(temp.getOperator()); 
            temp = temp.getFather(); 
        }

        return path;
    }  

    public static LinkedList<String> AmplitudeSearch(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, boolean back){

        // Cola que se utilizará para la ejecución del algoritmo
        Queue<Node> queue = new ArrayDeque<>();

        // Lista donde se almacenera el orden de los operadores a aplicar
        LinkedList<String> path = new LinkedList<>(); 

        // Nodo inicial desde donde se inicia la busqueda
        Node initPosition = findInitialPosition(matrix);

        // Lista con las posiciones de las cajas
        LinkedList<LinkedList<Integer>> boxes = findBoxes(matrix);

        //Map<Node, Node> parents = new HashMap<>();

        if(boxes.isEmpty()){

             throw new IllegalArgumentException("No hay cajas que buscar");
        }

        queue.add(initPosition);

        //parents.put(initPosition, null);

        while (!boxes.isEmpty()){

            //System.err.println(queue);

            int currentQueueSize = queue.size();

            for(int i = 0; i < currentQueueSize; i++){

                Node current = queue.poll();

                if ( boxes.contains(current.getPosition())){

                    boxes.remove(current.getPosition());

                    queue.clear();

                    current.SetState(current.getFather().getState() + 1);

                    queue.add(current);

                    if (boxes.isEmpty()) {
                        path = reconstructPath(current);
                    }

                }

                else{

                    LinkedList<Node> newPositions = getSons_node(matrix, operators, current,back);


                    for (Node newPosition : newPositions) {
                        //if (!parents.containsKey(newPosition)) {
                        queue.add(newPosition);
                            //parents.put(newPosition, current);
                        //}
                    }
                   
                }

            }
            
        }

        return path;
    }
  
    public static LinkedList<LinkedList<String>> AmplitudeSearchPartial(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, boolean back){

        // Cola que se utilizará para la ejecución del algoritmo
        Queue<Node> queue = new ArrayDeque<>();

        // Lista donde se almacenera el orden de los operadores a aplicar
        LinkedList<LinkedList<String>> path = new LinkedList<>(); 

        // Nodo inicial desde donde se inicia la busqueda
        Node initPosition = findInitialPosition(matrix);

        // Lista con las posiciones de las cajas
        LinkedList<LinkedList<Integer>> boxes = findBoxes(matrix);

        //Map<Node, Node> parents = new HashMap<>();

        if(boxes.isEmpty()){

             throw new IllegalArgumentException("No hay cajas que buscar");
        }

        queue.add(initPosition);

        while (!boxes.isEmpty()){

            int currentQueueSize = queue.size();

            for(int i = 0; i < currentQueueSize; i++){

                Node current = queue.poll();

                if ( boxes.contains(current.getPosition())){

                    boxes.remove(current.getPosition());

                    current.SetState(current.getFather().getState() + 1);

                    path.add(reconstructPath(current));

                }

                else{

                    LinkedList<Node> newPositions = getSons_node(matrix, operators, current,back);


                    for (Node newPosition : newPositions) {
                        queue.add(newPosition);

                    }
                   
                }

            }
            
        }

        return path;
    }

    public static LinkedList<String> DeepSearch(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, boolean back){

        // Cola que se utilizará para la ejecución del algoritmo
        Stack<Node> stack = new Stack<>();

        // Lista donde se almacenera el orden de los operadores a aplicar
        LinkedList<String> path = new LinkedList<>(); 

        // Nodo inicial desde donde se inicia la busqueda
        Node initPosition = findInitialPosition(matrix);

        // Lista con las posiciones de las cajas
        LinkedList<LinkedList<Integer>> boxes = findBoxes(matrix);

        Map<Node, Node> parents = new HashMap<>();

        if(boxes.isEmpty()){

             throw new IllegalArgumentException("No hay cajas que buscar");
        }

        stack.push(initPosition);

        parents.put(initPosition, null);

        while (!boxes.isEmpty()){

            int currentQueueSize = stack.size();

            for(int i = 0; i < currentQueueSize; i++){

                Node current = stack.pop();

                if ( boxes.contains(current.getPosition())){

                    boxes.remove(current.getPosition());

                    stack.clear();

                    current.SetState(current.getFather().getState() + 1);

                    stack.push(current);

                    if (boxes.isEmpty()) {
                        path = reconstructPath(current);
                    }

                }

                else{

                    LinkedList<Node> newPositions = getSons_node(matrix, operators, current,back);

                    for (Node newPosition : newPositions) {
                        if (!parents.containsKey(newPosition)) {
                            stack.push(newPosition);
                            parents.put(newPosition, current);
                        }
                    }
                   
                }

            }
            
        }

        return path;
    }

    public static LinkedList<LinkedList<String>> DeepSearchPartial(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, boolean back){

        // Cola que se utilizará para la ejecución del algoritmo
        Stack<Node> stack = new Stack<>();

        // Lista donde se almacenera el orden de los operadores a aplicar
        LinkedList<LinkedList<String>> path = new LinkedList<>(); 

        // Nodo inicial desde donde se inicia la busqueda
        Node initPosition = findInitialPosition(matrix);

        // Lista con las posiciones de las cajas
        LinkedList<LinkedList<Integer>> boxes = findBoxes(matrix);

        if(boxes.isEmpty()){

             throw new IllegalArgumentException("No hay cajas que buscar");
        }

        stack.push(initPosition);

        while (!boxes.isEmpty()){

            int currentQueueSize = stack.size();

            for(int i = 0; i < currentQueueSize; i++){

                Node current = stack.pop();

                if ( boxes.contains(current.getPosition())){

                    boxes.remove(current.getPosition());

                    current.SetState(current.getFather().getState() + 1);

                    path.add(reconstructPath(current));

                }

                else{

                    LinkedList<Node> newPositions = getSons_node(matrix, operators, current,back);

                    for (Node newPosition : newPositions) {

                        stack.push(newPosition);

                    }
                   
                }

            }
            
        }


        return path;
    }

    public static Integer distance(Node actualPosition, LinkedList<Integer> box) {

        Integer fill = actualPosition.getPosition().get(0);
        Integer row = actualPosition.getPosition().get(1);

        return Math.abs(fill - box.get(0)) + Math.abs(row -  box.get(1));

    }

    public static LinkedList<Integer> soonBoxDistance(Node actualPosition,LinkedList<LinkedList<Integer>> boxes){

        LinkedList<Integer> distances = boxes.stream()
                                              .map(n -> distance(actualPosition,n))
                                              .collect(Collectors.toCollection(LinkedList::new));

        Integer minDistance = Collections.min(distances);

        int index = distances.indexOf(minDistance);

        return boxes.get(index);

    }

    public static LinkedList<String> uniformCostSearch(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, boolean back){

        // Cola que se utilizará para la ejecución del algoritmo
        PriorityQueue<weightedNode> queue = new PriorityQueue<>(Comparator.comparingInt(weightedNode::getWeight));

        // Lista donde se almacenera el orden de los operadores a aplicar
        LinkedList<String> path = new LinkedList<>(); 

        // Nodo inicial desde donde se inicia la busqueda
        weightedNode initPosition = findInitialPosition_weiNode(matrix);

        // Lista con las posiciones de las cajas
        LinkedList<LinkedList<Integer>> boxes = findBoxes(matrix);

        //Map<Node, Node> parents = new HashMap<>();

        if(boxes.isEmpty()){

             throw new IllegalArgumentException("No hay cajas que buscar");
        }

        queue.add(initPosition);

        while (!boxes.isEmpty()){

            int currentQueueSize = queue.size();

            //System.err.println(queue);

            //System.err.println(boxes);

            for(int i = 0; i < currentQueueSize; i++){

                weightedNode current = queue.poll(); 


                if ( boxes.contains(current.getPosition())){

                    boxes.remove(current.getPosition());

                    queue.clear();

                    current.SetState(current.getFather().getState() + 1);

                    queue.add(current);

                    //path.add(reconstructPath(current));

                    if (boxes.isEmpty()) {
                        path = reconstructPath(current);
                    }

                }

                else{

                    LinkedList<weightedNode> newPositions = getSons_weigtedNode(matrix, operators, current,back);


                    for (weightedNode newPosition : newPositions) {
                        queue.add(newPosition);

                    }
                   
                }

            }
            
        }

        return path;
    }

    public static LinkedList<LinkedList<String>> uniformCostSearchPartial(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, boolean back){

        // Cola que se utilizará para la ejecución del algoritmo
        PriorityQueue<weightedNode> queue = new PriorityQueue<>(Comparator.comparingInt(weightedNode::getWeight));

        // Lista donde se almacenera el orden de los operadores a aplicar
        LinkedList<LinkedList<String>> path = new LinkedList<>(); 

        // Nodo inicial desde donde se inicia la busqueda
        weightedNode initPosition = findInitialPosition_weiNode(matrix);

        // Lista con las posiciones de las cajas
        LinkedList<LinkedList<Integer>> boxes = findBoxes(matrix);

        //Map<Node, Node> parents = new HashMap<>();

        if(boxes.isEmpty()){

             throw new IllegalArgumentException("No hay cajas que buscar");
        }

        queue.add(initPosition);

        while (!boxes.isEmpty()){

            int currentQueueSize = queue.size();

            //System.err.println(queue);

            //System.err.println(boxes);

            for(int i = 0; i < currentQueueSize; i++){

                weightedNode current = queue.poll(); 


                if ( boxes.contains(current.getPosition())){

                    boxes.remove(current.getPosition());

                    //queue.clear();

                    current.SetState(current.getFather().getState() + 1);

                    //queue.add(current);

                    path.add(reconstructPath(current));

                    //if (boxes.isEmpty()) {
                        //path.add(reconstructPath(current));
                    //}

                }

                else{

                    LinkedList<weightedNode> newPositions = getSons_weigtedNode(matrix, operators, current,back);


                    for (weightedNode newPosition : newPositions) {
                        queue.add(newPosition);

                    }
                   
                }

            }
            
        }

        return path;
    }

    public static LinkedList<String> AvaraSearch(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, boolean back){

        // Cola que se utilizará para la ejecución del algoritmo
        LinkedList<Node> queue = new LinkedList<>();
        //PriorityQueue<weightedNode> queue = new PriorityQueue<>(Comparator.comparingInt(weightedNode::getWeight));

        // Lista donde se almacenera el orden de los operadores a aplicar
        LinkedList<String> path = new LinkedList<>(); 

        // Nodo inicial desde donde se inicia la busqueda
        Node initPosition = findInitialPosition(matrix);

        // Lista con las posiciones de las cajas
        LinkedList<LinkedList<Integer>> boxes = findBoxes(matrix);

        if(boxes.isEmpty()){

             throw new IllegalArgumentException("No hay cajas que buscar");
        }

        queue.add(initPosition);

        LinkedList<Integer> goal = soonBoxDistance(initPosition, boxes);

        //boxes.remove(goal);

        //System.err.println(boxes);

        while (!boxes.isEmpty()){

            int currentQueueSize = queue.size();

            LinkedList<Integer> finalGoal = goal;

            //System.err.println(goal);

            for(int i = 0; i < currentQueueSize; i++){

                LinkedList<Integer> distances = queue.stream()
                                              .map(n -> distance(n,finalGoal))
                                              .collect(Collectors.toCollection(LinkedList::new));

                Integer minDistance = Collections.min(distances);

                int index = distances.indexOf(minDistance);

                Node current = queue.get(index);

                queue.remove(index);

                if (boxes.contains(current.getPosition())){

                    goal = soonBoxDistance(initPosition, boxes);

                    boxes.remove(goal);

                    //System.err.println(boxes);

                    queue.clear();

                    current.SetState(current.getFather().getState() + 1);

                    queue.add(current);

                    if (boxes.isEmpty()) {
                        path = reconstructPath(current);
                    }

                } else {

                    LinkedList<Node> newPositions = getSons_node(matrix, operators, current,back);


                    for (Node newPosition : newPositions) {
                            queue.add(newPosition);
                    }
                   
                }

            }
            
        }

        return path;
    }

    public static LinkedList<String> AStarSearch(LinkedList<LinkedList<Integer>> matrix,LinkedList<String> operators, boolean back){

        // Cola que se utilizará para la ejecución del algoritmo
        LinkedList<weightedNode> queue = new LinkedList<>();

        // Lista donde se almacenera el orden de los operadores a aplicar
        LinkedList<String> path = new LinkedList<>(); 

        // Nodo inicial desde donde se inicia la busqueda
        weightedNode initPosition = findInitialPosition_weiNode(matrix);

        // Lista con las posiciones de las cajas
        LinkedList<LinkedList<Integer>> boxes = findBoxes(matrix);

        if(boxes.isEmpty()){

             throw new IllegalArgumentException("No hay cajas que buscar");
        }

        queue.add(initPosition);

        LinkedList<Integer> goal = soonBoxDistance(initPosition, boxes);

        //boxes.remove(goal);

        //System.err.println(boxes);

        while (!boxes.isEmpty()){

            int currentQueueSize = queue.size();

            LinkedList<Integer> finalGoal = goal;

            //System.err.println(goal);

            for(int i = 0; i < currentQueueSize; i++){

                LinkedList<Integer> distances = queue.stream()
                                              .map(n -> distance(n,finalGoal) + n.getWeight())
                                              .collect(Collectors.toCollection(LinkedList::new));

                Integer minDistance = Collections.min(distances);

                int index = distances.indexOf(minDistance);

                weightedNode current = queue.get(index);

                queue.remove(index);

                if (boxes.contains(current.getPosition())){

                    goal = soonBoxDistance(initPosition, boxes);

                    boxes.remove(goal);

                    queue.clear();

                    current.SetState(current.getFather().getState() + 1);

                    queue.add(current);

                    if (boxes.isEmpty()) {
                        path = reconstructPath(current);
                    }

                } else {

                    LinkedList<weightedNode> newPositions = getSons_weigtedNode(matrix, operators, current,back);


                    for (weightedNode newPosition : newPositions) {
                            queue.add(newPosition);
                    }
                   
                }

            }
            
        }

        return path;
    }

    public static void main(String[] args) {

        LinkedList<LinkedList<Integer>> map = new LinkedList<>();

        map.add(new LinkedList<>(Arrays.asList(1, 0, 0, 0)));
        map.add(new LinkedList<>(Arrays.asList(1, 0, 1, 4)));
        map.add(new LinkedList<>(Arrays.asList(0, 2, 0, 3)));
        map.add(new LinkedList<>(Arrays.asList(4, 1, 1, 1)));
        // map.add(new LinkedList<>(Arrays.asList(0, 0, 0, 0, 4)));

        LinkedList<String> operators = new LinkedList<>();

        operators.add("UP");
        operators.add("DOWN");
        operators.add("RIGHT");
        operators.add("LEFT");

        System.err.println(AmplitudeSearch(map,operators,false));
        System.err.println(DeepSearch(map,operators,false));
        //System.err.println(AmplitudeSearchPartial(map,operators,false));
        //System.err.println(DeepSearchPartial(map,operators,false));
        System.err.println(uniformCostSearch(map, operators, false));
        //System.err.println(uniformCostSearchPartial(map, operators, false));
        System.err.println(AvaraSearch(map, operators, false));
        System.err.println(AStarSearch(map, operators, false));


        
    }

}