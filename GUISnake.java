package sample;

import java.awt.Font;
        import java.util.Collections;
        import javafx.animation.KeyFrame;
        import javafx.animation.Timeline;
        import javafx.application.Application;
        import javafx.collections.ObservableList;
        import javafx.event.ActionEvent;
        import javafx.event.EventHandler;
        import javafx.fxml.FXMLLoader;
        import javafx.scene.Group;
        import javafx.scene.Parent;
        import javafx.scene.Scene;
        import javafx.scene.layout.Pane;
        import javafx.scene.shape.Circle;
        import javafx.stage.Stage;
        import javafx.scene.Node;
        import javafx.scene.control.Button;
        import javafx.scene.control.Label;
        import javafx.scene.image.Image;
        import javafx.scene.input.MouseEvent;
        import javafx.scene.layout.VBox;
        import javafx.scene.paint.Color;
        import javafx.scene.paint.ImagePattern;
        import javafx.scene.shape.Shape;
        import javafx.scene.shape.Rectangle;
        import javafx.util.Duration;
        import javafx.scene.layout.StackPane;
/*ai pseudocode
similar to snake
need to change directions when approaching wall
need to turn after a random number of moves between 0 and 10
need to turn either up or down or left or right
need to declare a variable outside animation for the count
if its near the corner or the edge,
it must act non-randomly,
else it can act randomly
need to declare a variable for the random number outside the animation
every time the count==the random number,
we must get a new random number and make a turn

to avoid ai collisions with the wall:
handle corners first
then handle approaching the edge
then handle borders
*/
public class GUISnake extends Application {

    //--------------an enum for directions------------------
    public enum Direction{ //pre defined states!
        UP, DOWN, LEFT, RIGHT
    }
    //--------------some basic set-up------------------
    //size of 1 block
    public static final int BLOCK_SIZE = 20;
    //offset from 0,0
    /*
    everything must be offset by M, becuase circles start at their centers
    the circles will go starting on the grid, untill their centers either
    reach the offset, or their centers reach the offset plus one diameter
    less than a multiple of their diameter (this is APP_W-BLOCK_SIZE and
    APP_H-BLOCK_SIZE)
    the border should occur at APP_W+M and APP_H+M to allow for the offset,
    the multiple of the circle, and another offset
    */
    public static final int M=10;
    //screen width
    public static final int APP_W = 35*BLOCK_SIZE+M;
    //screen height
    public static final int APP_H = 35*BLOCK_SIZE+M;
    public static final int APP_W2 = 20*BLOCK_SIZE+M;
    //screen height
    public static final int APP_H2 = 20*BLOCK_SIZE+M;
    private boolean moved = false; // moving (don't allows moving in different directions at the same time
    private boolean running = false; // is our application running
    private Direction direction = Direction.RIGHT;
    private Direction aiDirection = Direction.RIGHT;
    private Timeline timeline = new Timeline(); // our animation
    private ObservableList<Node> snake;
    private ObservableList<Node> ai;
    private int lastScore = 0;
    private int score = 0;
    boolean playing=true;
    double speed=0.1;

    //------------the action of creating the game content for the window--------------
    private Parent createContent(){
        //------------setting up the display--------------
        //the pane is where the content lives
        StackPane stackRoot=new StackPane();
        Pane root=new Pane();
        Pane borderRoot=new Pane();
        root.setTranslateX(20);
        root.setTranslateY(20);
        //set window size
        root.setPrefSize(APP_W+M,APP_H+M);
        borderRoot.setPrefSize(APP_W+M+2*BLOCK_SIZE,APP_H+M+2*BLOCK_SIZE);

        //------------create the snake--------------
        //new group
        Group snakeBody = new Group();
        Group aiBody = new Group();
        //create a snake "variable" of type Group that will be an
        //"ObservableList<Node>"
        snake = snakeBody.getChildren();
        ai = aiBody.getChildren();


        //Set score
        Label scoreVal = new Label() ;
        scoreVal.setText("Score: " + score);
        scoreVal.setTranslateY(0);
        scoreVal.setTranslateX(300);
        //------------create a randomly placed food object--------------
        //create a food object
        Circle food=new Circle(10);
        food.setFill(Color.GRAY);

        /*
        Image body = new Image("SnakePackage/Untitled.png");
        ImagePattern ip = new ImagePattern(body);
        background.setFill(ip);
        */
        Rectangle lowerWall=new Rectangle(APP_W+M+BLOCK_SIZE, BLOCK_SIZE);
        lowerWall.setFill(Color.CRIMSON);
        lowerWall.setTranslateX(0);
        lowerWall.setTranslateY(APP_H+M);
        Rectangle rightWall=new Rectangle(BLOCK_SIZE, APP_H+M);
        rightWall.setFill(Color.CRIMSON);
        rightWall.setTranslateX(APP_W+M);
        rightWall.setTranslateY(0);
        Rectangle upperWall=new Rectangle(APP_W+M+BLOCK_SIZE, BLOCK_SIZE);
        upperWall.setFill(Color.CRIMSON);
        upperWall.setTranslateX(0);
        upperWall.setTranslateY(0);
        Rectangle leftWall=new Rectangle(BLOCK_SIZE, APP_H+M);
        leftWall.setFill(Color.CRIMSON);
        leftWall.setTranslateX(0);
        leftWall.setTranslateY(0);
        //setting x, and y of food to random value
        //integer division by blocks makes it an integer of blocks,
        //multiplying by blocks makes it big again (the right scale)
        food.setTranslateX((int)(Math.random() * (APP_W - BLOCK_SIZE+1))/BLOCK_SIZE*BLOCK_SIZE+M);
        food.setTranslateY((int)(Math.random() * (APP_H - BLOCK_SIZE+1))/BLOCK_SIZE*BLOCK_SIZE+M);


        //------------the animation--------------
        KeyFrame frame = new KeyFrame(Duration.seconds(speed), event -> {
            if(!running)
                return; //if not running just simple return


            //is snake two or greater?
            boolean toRemove = snake.size() > 1;
            boolean aiToRemove = ai.size() > 1;
            //if at least 2 blocks long, do what?
            //when an observable list calls "get", we get the element at that index
            //when it calls remove, it removes the element at the specified
            //index in the last
            //so if it is at least 2 blocks long, we remove the last block
            //and make it the tail, otherwise the first block is the tail
            Node tail = toRemove ? snake.remove(snake.size()-1) : snake.get(0);
            Node aiTail = toRemove ? ai.remove(ai.size()-1) : ai.get(0);

            double tailX = tail.getTranslateX();
            double tailY = tail.getTranslateY();

            double aiTailX = aiTail.getTranslateX();
            double aiTailY = aiTail.getTranslateY();



            //basis for animating movement of the snake
            switch (direction) {
                case UP:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY() - BLOCK_SIZE);
                    break;
                case DOWN:
                    tail.setTranslateX(snake.get(0).getTranslateX());
                    tail.setTranslateY(snake.get(0).getTranslateY()+BLOCK_SIZE);
                    break;
                case LEFT:
                    tail.setTranslateX(snake.get(0).getTranslateX()-BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;
                case RIGHT:
                    tail.setTranslateX(snake.get(0).getTranslateX()+BLOCK_SIZE);
                    tail.setTranslateY(snake.get(0).getTranslateY());
                    break;

            }
            /*
            if (aiDirection==Direction.RIGHT){
                aiDirection=Direction.DOWN;
            }
            else if (aiDirection==Direction.DOWN){
                aiDirection=Direction.LEFT;
            }
            else if (aiDirection==Direction.LEFT){
                aiDirection=Direction.UP;
            }
            else if (aiDirection==Direction.UP){
                aiDirection=Direction.RIGHT;
            }
            */

            //----------------8 corner cases--------------
            //0,0 up
            if (ai.get(0).getTranslateX()==M&&ai.get(0).getTranslateY()==M&&aiDirection==Direction.UP){
                aiDirection=Direction.RIGHT;
            }
            //0,0 right
            else if (ai.get(0).getTranslateX()==M&&ai.get(0).getTranslateY()==M&&aiDirection==Direction.LEFT){
                aiDirection=Direction.DOWN;
            }
            //0,APP_H down
            else if (ai.get(0).getTranslateX()==M&&ai.get(0).getTranslateY()>=APP_H-BLOCK_SIZE&&aiDirection==Direction.DOWN){
                aiDirection=Direction.RIGHT;
            }
            //0,APP_H left
            else if (ai.get(0).getTranslateX()==M&&ai.get(0).getTranslateY()>=APP_H-BLOCK_SIZE&&aiDirection==Direction.LEFT){
                aiDirection=Direction.UP;
            }
            //APP_W,0 right
            else if (ai.get(0).getTranslateX()>=APP_W-BLOCK_SIZE&&ai.get(0).getTranslateY()==M&&aiDirection==Direction.RIGHT){
                aiDirection=Direction.DOWN;
            }
            //APP_W,0 up
            else if (ai.get(0).getTranslateX()>=APP_W-BLOCK_SIZE&&ai.get(0).getTranslateY()==M&&aiDirection==Direction.UP){
                aiDirection=Direction.LEFT;
            }
            //APP_W,APP_H right
            else if (ai.get(0).getTranslateX()>=APP_W-BLOCK_SIZE&&ai.get(0).getTranslateY()>=APP_H-BLOCK_SIZE&&aiDirection==Direction.RIGHT){
                aiDirection=Direction.UP;
            }
            //APP_W, APP_H down
            else if (ai.get(0).getTranslateX()>=APP_W-BLOCK_SIZE&&ai.get(0).getTranslateY()>=APP_H-BLOCK_SIZE&&aiDirection==Direction.DOWN){
                aiDirection=Direction.LEFT;
            }
            //----------------8 sideline  cases--------------
            else if (ai.get(0).getTranslateX()==M&&(aiDirection==Direction.UP||aiDirection==Direction.DOWN)){
                boolean changeDirection=tenPercentChance();
                if (changeDirection==true){
                    aiDirection=Direction.RIGHT;
                }
            }
            else if (ai.get(0).getTranslateX()>=APP_W-BLOCK_SIZE&&(aiDirection==Direction.DOWN||aiDirection==Direction.UP)){
                boolean changeDirection=tenPercentChance();
                if (changeDirection==true){
                    aiDirection=Direction.LEFT;
                }
            }
            else if (ai.get(0).getTranslateY()==M&&(aiDirection==Direction.RIGHT||aiDirection==Direction.LEFT)){
                boolean changeDirection=tenPercentChance();
                if (changeDirection==true){
                    aiDirection=Direction.DOWN;
                }
            }
            else if (ai.get(0).getTranslateY()>=APP_H-BLOCK_SIZE&&(aiDirection==Direction.LEFT||aiDirection==Direction.RIGHT)){
                boolean changeDirection=tenPercentChance();
                if (changeDirection==true){
                    aiDirection=Direction.UP;
                }
            }
            //----------------4 sideline approach cases--------------
            else if (ai.get(0).getTranslateX()==M&&aiDirection==Direction.LEFT){
                int random=(int)(Math.random()*2);
                if (random==0)
                    aiDirection=Direction.UP;
                else
                    aiDirection=Direction.DOWN;
            }
            else if (ai.get(0).getTranslateX()>=APP_W-BLOCK_SIZE&&aiDirection==Direction.RIGHT){
                int random=(int)(Math.random()*2);
                if (random==0)
                    aiDirection=Direction.UP;
                else
                    aiDirection=Direction.DOWN;
            }
            else if (ai.get(0).getTranslateY()==M&&aiDirection==Direction.UP){
                int random=(int)(Math.random()*2);
                if (random==0)
                    aiDirection=Direction.LEFT;
                else
                    aiDirection=Direction.RIGHT;
            }
            else if (ai.get(0).getTranslateY()>=APP_H-BLOCK_SIZE&&aiDirection==Direction.DOWN){
                int random=(int)(Math.random()*2);
                if (random==0)
                    aiDirection=Direction.LEFT;
                else
                    aiDirection=Direction.RIGHT;
            }
            else{
                boolean changeDirection=tenPercentChance();
                if (changeDirection==true){
                    int num=(int)(Math.random()*2);
                    switch (aiDirection){
                        case UP:
                            if (num==0){
                                aiDirection=Direction.LEFT;
                            }
                            else
                                aiDirection=Direction.RIGHT;

                            break;
                        case DOWN:
                            if (num==0){
                                aiDirection=Direction.LEFT;
                            }
                            else
                                aiDirection=Direction.RIGHT;

                            break;
                        case LEFT:
                            if (num==0){
                                aiDirection=Direction.UP;
                            }
                            else
                                aiDirection=Direction.DOWN;

                            break;
                        case RIGHT:
                            if (num==0){
                                aiDirection=Direction.UP;
                            }
                            else
                                aiDirection=Direction.DOWN;

                            break;
                    }
                }

            }

            switch (aiDirection) {
                case UP:
                    aiTail.setTranslateX(ai.get(0).getTranslateX());
                    aiTail.setTranslateY(ai.get(0).getTranslateY() - BLOCK_SIZE);
                    break;
                case DOWN:
                    aiTail.setTranslateX(ai.get(0).getTranslateX());
                    aiTail.setTranslateY(ai.get(0).getTranslateY()+BLOCK_SIZE);
                    break;
                case LEFT:
                    aiTail.setTranslateX(ai.get(0).getTranslateX()-BLOCK_SIZE);
                    aiTail.setTranslateY(ai.get(0).getTranslateY());
                    break;
                case RIGHT:
                    aiTail.setTranslateX(ai.get(0).getTranslateX()+BLOCK_SIZE);
                    aiTail.setTranslateY(ai.get(0).getTranslateY());
                    break;

            }
            //----------------putting the tail at the front--------------
            if(toRemove)
                snake.add(0, tail); // we put tail in front -- the zeroth element
            //so the tail is constantly being moved to the front--is this how we
            //get the affect of it turning?
            if(aiToRemove)
                ai.add(0, aiTail);
            //----------------to detect collision with itself--------------
            for(Node rect : snake) {
                if(rect != tail && tail.getTranslateX() == rect.getTranslateX() && tail.getTranslateY() == rect.getTranslateY()) { // tail name is little confusing, cause it must be a head now!!!
                    //then you lose!
                    //restart game
                    lastScore=score;
                    score = 0;
                    scoreVal.setText("Score: " + score);
                    playing=false;
                    restartGame(lastScore);
                    break;
                }
            }
            for(Node rect2 : ai) {
                if(rect2 != aiTail && aiTail.getTranslateX() == rect2.getTranslateX() && aiTail.getTranslateY() == rect2.getTranslateY()) { // tail name is little confusing, cause it must be a head now!!!

                    respawnAI(aiTail);
                    break;
                }
            }
            for(Node rect2 : ai) {
                if(tail.getTranslateX() == rect2.getTranslateX() && tail.getTranslateY() == rect2.getTranslateY()) { // tail name is little confusing, cause it must be a head now!!!
                    //then you lose!
                    //restart game
                    lastScore=score;
                    score = 0;
                    scoreVal.setText("Score: " + score);
                    playing=false;
                    restartGame(lastScore);
                    break;
                }
            }
            for(Node rect : snake) {
                if(aiTail.getTranslateX() == rect.getTranslateX() && aiTail.getTranslateY() == rect.getTranslateY()) { // tail name is little confusing, cause it must be a head now!!!
                    //then you lose!
                    //restart game
                    respawnAI(aiTail);
                    break;
                }
            }
            //----------------to detect collision with the walls--------------
            if (tail.getTranslateX() < M || tail.getTranslateX() >= APP_W
                    || tail.getTranslateY() < M || tail.getTranslateY() >= APP_H) {
                lastScore=score;
                score = 0;
                scoreVal.setText("Score: " + score);
                playing=false;
                restartGame(lastScore);
                food.setTranslateX((int)(Math.random() * (APP_W - BLOCK_SIZE+1))/BLOCK_SIZE*BLOCK_SIZE+M);
                food.setTranslateY((int)(Math.random() * (APP_H - BLOCK_SIZE+1))/BLOCK_SIZE*BLOCK_SIZE+M);
            }

            //--------------the "eating" mechanism------------------
            //if x and y location of snake=that of the food, add 1 to the tail and
            //create a new food object
            if (tail.getTranslateX() == food.getTranslateX() && tail.getTranslateY() == food.getTranslateY()) {
                food.setTranslateX((int)(Math.random() * (APP_W - BLOCK_SIZE+1))/BLOCK_SIZE*BLOCK_SIZE+M);
                food.setTranslateY((int)(Math.random() * (APP_H - BLOCK_SIZE+1))/BLOCK_SIZE*BLOCK_SIZE+M);
                // 20 point for each food eat
                score += 20;
                scoreVal.setText("Score: " + score);
                //Color of the snake
                //User snake
                NewBody rect = new NewBody(tailX,tailY,1);
                //computer snake
                NewBody rect2 = new NewBody(aiTailX,aiTailY,2);

                snake.add(rect); //adding rectangle to snake
                ai.add(rect2);
            }
            playing=false;
        });
        //--------------to get things running------------------
        timeline.getKeyFrames().addAll(frame); // add frame to the timeline KeyFrames
        timeline.setCycleCount(Timeline.INDEFINITE); // it will always run same frame(there is any one frame to run
        NewBody head = new NewBody(100+M, 100+M,1);
        NewBody aiHead = new NewBody(20+M, 20+M,2);
        snake.add(head);
        ai.add(aiHead);
        timeline.play();
        running = true;
        //displays the content
        root.getChildren().addAll(food, snakeBody, aiBody, scoreVal);
        borderRoot.getChildren().addAll(lowerWall, rightWall, upperWall, leftWall);
        //so the content can be displayed(?)

        stackRoot.getChildren().addAll(borderRoot, root);

        return stackRoot;
    }

    //------------sets up the window--------------
    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new Scene(new Group());
        stage.setWidth(APP_W+M+2*BLOCK_SIZE);
        stage.setHeight(APP_H+M+3*BLOCK_SIZE);

        VBox vbox = new VBox();
        VBox vbox1 = new VBox();
        VBox vbox2 = new VBox();
        VBox vbox3 = new VBox();
        VBox vbox4 = new VBox();

        vbox.setLayoutX(APP_W/2 - 40);
        vbox.setLayoutY(APP_H/2);
        vbox1.setLayoutX(APP_W/2 - 20);
        vbox1.setLayoutY(50+APP_H/2);

        vbox2.setLayoutX(APP_W/2 - 20);
        vbox2.setLayoutY(-50+APP_H/2);
        vbox3.setLayoutX(APP_W/2 - 20);
        vbox3.setLayoutY(-100+APP_H/2);
        vbox4.setLayoutX(APP_W/2 - 20);
        vbox4.setLayoutY(-150+APP_H/2);

        final Button button1 = new Button(" START GAME ");
        final Button button2 = new Button(" QUIT ");
        final Button button3 = new Button(" FAST ");
        final Button button4 = new Button(" MEDIUM ");
        final Button button5 = new Button(" SLOW ");

        button1.setStyle("-fx-base: CCE5FF;");
        button2.setStyle("-fx-base: FFCCCC;");

        /*Glow glow = new Glow();
        button1.setEffect(glow);
        Lighting light = new Lighting();
        button2.setEffect(light);*/



        button1.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override

                    public void handle(MouseEvent e) {

                        Scene scene = new Scene(createContent());
                        //let's the user change the direction at any time
                        directionKey(scene);
                        //set window title
                        stage.setTitle("Snake Eats, Snake Dies");
                        //need a primary stage--a window
                        stage.setScene(scene);
                        //show the window
                        stage.show();
                    }
                });

        button3.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override

                    public void handle(MouseEvent e) {

                        speed=0.1;
                    }
                });
        button4.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override

                    public void handle(MouseEvent e) {

                        speed=0.15;
                    }
                });
        button5.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override

                    public void handle(MouseEvent e) {

                        speed=0.2;
                    }
                });

        button2.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override

                    public void handle(MouseEvent e) {
                        System.exit(0);
                    }
                });


        vbox.getChildren().add(button1);
        ((Group) scene.getRoot()).getChildren().add(vbox);

        vbox1.getChildren().add(button2);
        ((Group) scene.getRoot()).getChildren().add(vbox1);

        vbox2.getChildren().add(button3);
        ((Group) scene.getRoot()).getChildren().add(vbox2);

        vbox3.getChildren().add(button4);
        ((Group) scene.getRoot()).getChildren().add(vbox3);

        vbox4.getChildren().add(button5);
        ((Group) scene.getRoot()).getChildren().add(vbox4);

        stage.setScene(scene);
        scene.setFill(Color.BLACK);
        stage.show();
    }
      /*  //needed a place to display stuff, a "Scene"--the content
        //will be determined by createContent
        Scene scene=new Scene(createContent());
        //let's the user change the direction at any time
        directionKey(scene);
        //set window title
        primaryStage.setTitle("Snake Eats, Snake Dies");
        //need a primary stage--a window
        primaryStage.setScene(scene);
        //show the window
        primaryStage.show();*/


    //------------launches the GUI--------------
    public static void main(String[] args){
        //actually launches the GUI
        Application.launch(args);
    }

    //------------allows the user to change the snake's direction------------
    private void directionKey(Scene scene) {
        scene.setOnKeyPressed(event -> {

            switch (event.getCode()) {
                case W:
                    if (direction != Direction.DOWN)
                        direction = Direction.UP;
                    break;
                case UP:
                    if (direction != Direction.DOWN)
                        direction = Direction.UP;
                    break;
                case S:
                    if(direction != Direction.UP)
                        direction = Direction.DOWN;
                    break;
                case DOWN:
                    if(direction != Direction.UP)
                        direction = Direction.DOWN;
                    break;
                case A:
                    if(direction != Direction.RIGHT)
                        direction = Direction.LEFT;
                    break;
                case LEFT:
                    if(direction != Direction.RIGHT)
                        direction = Direction.LEFT;
                    break;
                case D:
                    if(direction != Direction.LEFT)
                        direction = Direction.RIGHT;
                    break;
                case RIGHT:
                    if(direction != Direction.LEFT)
                        direction = Direction.RIGHT;
                    break;

            }
        });

    }
    //--------------restart a game-----------------
    private void restartGame(int score) {
        stopGame();
        Label playAgain = new Label() ;
        playAgain.setText("Play Again?");
        playAgain.setStyle("-fx-font: 40 arial;");
        playAgain.setTextFill(Color.WHITE);
        playAgain.setTranslateY(0);
        playAgain.setTranslateX(APP_W2/2-80);
        Label changeSpeed = new Label() ;
        changeSpeed.setText("To change speed, please\nquit and restart the program.");
        changeSpeed.setStyle("-fx-font: 30 arial;");
        changeSpeed.setTextFill(Color.WHITE);
        changeSpeed.setTranslateY(APP_H2-100);
        Label lastScore = new Label() ;
        lastScore.setText("Last Score:" + score);
        lastScore.setStyle("-fx-font: 50 arial;");
        lastScore.setTextFill(Color.WHITE);
        lastScore.setTranslateY(100);
        lastScore.setTranslateX(APP_W2/2-120);
        Stage stage=new Stage();
        Scene scene = new Scene(new Group());
        stage.setWidth(APP_W2+M+2*BLOCK_SIZE);
        stage.setHeight(APP_H2+M+3*BLOCK_SIZE);

        VBox vbox = new VBox();
        VBox vbox1 = new VBox();

        vbox.setLayoutX(APP_W2/2 - 40);
        vbox.setLayoutY(APP_H2/2);
        vbox1.setLayoutX(APP_W2/2 - 20);
        vbox1.setLayoutY(50+APP_H2/2);


        final Button button1 = new Button(" START GAME ");
        final Button button2 = new Button(" QUIT ");


        button1.setStyle("-fx-base: CCE5FF;");
        button2.setStyle("-fx-base: FFCCCC;");

        /*Glow glow = new Glow();
        button1.setEffect(glow);
        Lighting light = new Lighting();
        button2.setEffect(light);*/



        button1.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override

                    public void handle(MouseEvent e) {

                        stage.close();
                        startGame();
                    }
                });

        button2.addEventHandler(MouseEvent.MOUSE_CLICKED,
                new EventHandler<MouseEvent>() {
                    @Override

                    public void handle(MouseEvent e) {
                        System.exit(0);
                    }
                });


        vbox.getChildren().add(button1);
        ((Group) scene.getRoot()).getChildren().add(vbox);

        vbox1.getChildren().add(button2);
        ((Group) scene.getRoot()).getChildren().add(vbox1);

        ((Group) scene.getRoot()).getChildren().addAll(playAgain, changeSpeed, lastScore);

        stage.setScene(scene);
        scene.setFill(Color.BLACK);
        stage.show();


    }
    //----------------stop a game--------------------
    private void stopGame() {
        running = false;
        timeline.stop();
        snake.clear(); // clear element
        ai.clear();
    }
    //----------------start a game-------------------
    private void startGame() {
        running=true;
        direction = Direction.RIGHT;
        NewBody head = new NewBody(100+M, 100+M,1);
        snake.add(head);
        NewBody head2 = new NewBody(20+M, 20+M,2);
        ai.add(head2);
        timeline.play();
    }
    //----------------respawn the ai-------------------
    private void respawnAI(Node aiTail){
        aiTail.setTranslateX(M);
        aiTail.setTranslateY(18*BLOCK_SIZE+M);
    }
    //----------------10% chance of returning true-------------------
    private boolean tenPercentChance(){
        int random=(int)(Math.random()*10);
        switch (random){
            case 9:
                return true;
        }
        return false;
    }
}
//------------for the head--------------
// a class that will have to do with the Pane
class NewBody extends Pane {
    //the rectangle or circle
    private Circle rOut1;
    //constructor with location
    public NewBody(double x, double y, int color){
        this.setTranslateX(x);
        this.setTranslateY(y);
        //size of new rectangle
        rOut1 = new Circle (10);
        switch(color){
            case 1:
                int colr = (int)(Math.random()*4 + 1);

                switch (colr) {
                    case 1:
                        rOut1.setFill(Color.DARKGREEN);
                        break;
                    case 2:
                        rOut1.setFill(Color.DARKOLIVEGREEN);
                        break;
                    case 3:
                        rOut1.setFill(Color.FORESTGREEN);
                        break;
                    case 4:
                        rOut1.setFill(Color.DARKSEAGREEN);
                        break;
                }
                break;
            case 2: rOut1.setFill(Color.BLACK);
                break;
            default: rOut1.setFill(Color.BLACK);
                break;
        }
        //Image body = new Image("sample/body.png");
        //ImagePattern ip = new ImagePattern(body);
        //rOut1.setFill(ip);

        //add it to the super
        super.getChildren().addAll(rOut1);
    }


    //public void setrOut1(ImagePattern imp) {
    //    this.rOut1.setFill(imp);

    //}
}
/*
the circle starts in the middle for everything
*/