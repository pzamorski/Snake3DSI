package org.patzam.gamexy;


import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;


public class Game2Snake extends SimpleApplication implements ActionListener {


    private transient Position[] snakePosition = new Position[800];
    private static LinkedList<Geometry> snakeParts; // Ciało węża
    private Geometry snakePart;// Segment weza
    private Geometry food; // Jedzenie
    private Direction currentDirection = Direction.RIGHT;
    private boolean inGame = true;

    private transient Position foodPosition;
    private int snakeLength;

    private static final int GRID_SIZE_X = GameParameters.GAME_DIMENSIONS;
    private static final int GRID_SIZE_Y = GameParameters.GAME_DIMENSIONS;
    private static final int GRID_SIZE_Z = 1;
    private static final int GRID_SIZE = GRID_SIZE_X;
    private int oldX;
    private int oldY;
    private Position oldPositionHead = new Position(0, 0);
    private int oldSnakeLength;
    private int startSnakeLength = 3;

    public Game2Snake() {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1920, 1000);
//        settings.setResolution(800, 600);
        settings.setFullscreen(true);
        setSettings(settings);
       init();
    }

    public static void main(String[] args) {
        Game2Snake game2Snake = new Game2Snake();

        game2Snake.start();


    }

    @Override
    public void simpleInitApp() {
        getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
        assetManager.registerLocator("assets", FileLocator.class);

//        showGameArea();
        showGameAreaOneCube();

        initCam();
        spawnFood(foodPosition.getX(), foodPosition.getY());
        initSnake();


    }


    @Override
    public void simpleUpdate(float tpf) {

        if (oldX != foodPosition.getX() || oldY != foodPosition.getY()) {
            food.setLocalTranslation(foodPosition.getX(), foodPosition.getY(), 0);
            oldX = foodPosition.getX();
            oldY = foodPosition.getY();
        }

        if (oldPositionHead.getX() != getHeadPosition().getX() || oldPositionHead.getY() != getHeadPosition().getY()) {
            draw();
            int j = 0;
            for (; null != snakePosition[j]; j++) {
            }

            for (int i = 0; null != snakePosition[i]; i++) {
                if (snakeParts.size() < j) {
                    spawnSnakePart(snakePosition[i].getX(), snakePosition[i].getY());
                }
                if (snakeParts.size() > j) {
                    initSnake();
                }
                snakeParts.get(i).setLocalTranslation(snakePosition[i].getX(), snakePosition[i].getY(), 0);
            }

            oldPositionHead = new Position(getHeadPosition().getX(), getHeadPosition().getY());

        }

    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (isOngoing()) {
            if (isFoodEaten()) {
                // Increase player length
                snakeLength++;


                // Set food on a new position
                setFoodPosition();
            } else {
                final Position headPosition = getHeadPosition();
                inGame = !headPosition.isOutsideTheArea();

                if (inGame) { // We only need to check for body part collision if we are still in the game
                    checkIfPlayerHeadIsCollidingWithOtherBodyParts(headPosition);
                }
            }
        }

        if (!inGame) {
            System.out.println("You tried very hard, but you lost the game.");
        }

        // repaint();
    }

//    @Override
//    protected void paintComponent(final Graphics graphics) {
//        super.paintComponent(graphics);
//        draw(graphics);
//    }

    public int getScore() {
        return snakeLength - startSnakeLength;
    }


    public void move() {
        // Copy positions (e.g. head position is not moved to the top of the body, body - 1 is not body - 2 and so on)
        if (snakeLength - 1 >= 0) System.arraycopy(snakePosition, 0, snakePosition, 1, snakeLength - 1);
        // Previous head position is currently at index 1
        final Position previousHeadPosition = snakePosition[1];

        // Set new head position
        snakePosition[0] = PositionManager.getNextPosition(previousHeadPosition, currentDirection);
        // As we do not use any key pressed events to move our player we need "manually" notify about performed action
        actionPerformed(null);
    }


    public void changeDirection(final ActionMove action) {
//        System.out.println("Action: "+action);

        switch (action) {
            case MOVE_UP:
                if (currentDirection == Direction.DOWN) break;
                currentDirection = Direction.UP;
                break;
            case MOVE_RIGHT:
                if (currentDirection == Direction.LEFT) break;
                currentDirection = Direction.RIGHT;
                break;
            case MOVE_DOWN:
                if (currentDirection == Direction.UP) break;
                currentDirection = Direction.DOWN;
                break;
            case MOVE_LEFT:
                if (currentDirection == Direction.RIGHT) break;
                currentDirection = Direction.LEFT;
                break;
        }
    }

    public GameContext init() {
        Random random = new Random();
        snakeLength = startSnakeLength;



        snakePosition = new Position[900];
        //snakeParts=new LinkedList<>();

        for (int i = 0; i < snakeLength; i++) {
            snakePosition[i] = new Position(GameParameters.GAME_DIMENSIONS / 2 - i, GameParameters.GAME_DIMENSIONS / 2);
//            System.out.println("172: " + snakePosition[i]);
        }


        setFoodPosition();

        inGame = true;

        return buildStateObservation();
    }

    public boolean isOngoing() {
        return inGame;
    }

    public void endGame() {
        this.inGame = false;
    }

    public GameContext buildStateObservation() {
        return new GameContext(new double[]{

                GameContextManager.getStateForDirection(snakePosition, foodPosition, Direction.UP),
                GameContextManager.getStateForDirection(snakePosition, foodPosition, Direction.RIGHT),
                GameContextManager.getStateForDirection(snakePosition, foodPosition, Direction.DOWN),
                GameContextManager.getStateForDirection(snakePosition, foodPosition, Direction.LEFT),
        });
    }

    public double calculateRewardForActionToTake(final ActionMove action) {
        return RewardManager.calculateRewardForActionToTake(action, snakePosition, foodPosition);
    }

    private void draw() {
        if (!isOngoing()) {
            return;
        }

        // Draw food
        //graphics.drawImage(FOOD_IMAGE, foodPosition.getX(), foodPosition.getY(), this);

        // Draw snake
        for (int i = 0; i < snakeLength; i++) {
            // Position of one of the snake parts (head or tail)
            final Position pos = snakePosition[i];
            if (pos == null) {
                continue;
            }

            // First item is always head

            //graphics.drawImage(i == 0 ? HEAD_IMAGE : TAIL_IMAGE, pos.getX(), pos.getY(), this);

        }

        final Position headPosition = getHeadPosition();
        final Position[] observations = new Position[NetworkManagerXY.NUMBER_OF_INPUTS];
        observations[0] = PositionManager.getNextPosition(headPosition, Direction.UP);
        observations[1] = PositionManager.getNextPosition(headPosition, Direction.RIGHT);
        observations[2] = PositionManager.getNextPosition(headPosition, Direction.DOWN);
        observations[3] = PositionManager.getNextPosition(headPosition, Direction.LEFT);
        for (int i = 0; i < observations.length; i++) {
            final Position pos = observations[i];
            if (pos == null) {
                continue;
            }

        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void setFoodPosition() {
        Random random = new Random();
        foodPosition = new Position(random.nextInt(GameParameters.GAME_DIMENSIONS),
                random.nextInt(GameParameters.GAME_DIMENSIONS)
        );

        if (Arrays.asList(snakePosition).contains(foodPosition)) {
            setFoodPosition();
        }
    }

    private boolean isFoodEaten() {
        return foodPosition.equals(getHeadPosition());
    }

    private void checkIfPlayerHeadIsCollidingWithOtherBodyParts(final Position headPosition) {
        // Count how many times is snake head contained in the snake position array. We expect only to find it once.
        // If there is more then one that means that the head is overlapping the body and we can end the game.
        final long matches = Arrays.stream(snakePosition)
                .filter(Objects::nonNull)
                .filter(pos -> pos.equals(headPosition))
                .count();

        if (matches > 1) {
            endGame();
        }
    }

    private Position getHeadPosition() {
        return snakePosition[0];
    }


    private void showGameArea() {
        // Tworzenie obszaru gry z bloków 1x1x1
        for (int x = 0; x < GRID_SIZE_X; x++) {
            for (int y = 0; y < GRID_SIZE_Y; y++) {
                for (int z = 0; z < GRID_SIZE_Z; z++) {
                    // Tworzenie pojedynczego
                    WireBox wirebox = new WireBox(0.5f, 0.5f, 0.5f); // Box o wymiarach 1x1x1
                    Geometry boxGeometry = new Geometry("Wirebox", wirebox);
                    Material boxMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    boxMaterial.setColor("Color", ColorRGBA.Gray);
                    boxGeometry.setMaterial(boxMaterial);

                    // Ustawianie pozycji bloku w obszarze gry
//                    float posX = x - GRID_SIZE / 2f + 0.5f;
//                    float posY = y - GRID_SIZE / 2f + 0.5f;
//                    float posZ = z - GRID_SIZE / 2f + 0.5f;
                    boxGeometry.setLocalTranslation(x, y, z);

                    // Dodawanie bloku do sceny
                    rootNode.attachChild(boxGeometry);
                }
            }
        }
    }

    private void initCam() {
        flyCam.setMoveSpeed(60f);
        flyCam.setEnabled(true);
        cam.setLocation(new Vector3f(GRID_SIZE / 2f, GRID_SIZE / 2f, GRID_SIZE * 2)); // Ustawienie pozycji kamery
        cam.lookAt(new Vector3f(GRID_SIZE / 2f, GRID_SIZE / 2f, 0), Vector3f.UNIT_Z); // Skierowanie
    }

    private void initSnake() {

        // Sprawdź czy snakeParts zostało zainicjowane
        if (snakeParts == null) {
            snakeParts = new LinkedList<>();
        } else {
            // Usuń istniejące części węża
            for (Geometry part : snakeParts) {
                if (part != null) {
                    rootNode.detachChild(part);
                }
            }
            snakeParts.clear(); // Wyczyść listę części węża
        }

        // Inicjuj nowego węża na podstawie pozycji z tablicy snakePosition
        for (int i = 0; i < snakePosition.length && snakePosition[i] != null; i++) {
            spawnSnakePart(snakePosition[i].getX(), snakePosition[i].getY());
        }
    }

    private void spawnSnakePart(int x, int y) {
        Box box = new Box(0.5f, 0.5f, 0.5f);
        snakePart = new Geometry("SnakePart", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        int redValue=255-(snakeParts.size()*2);
        int blueValue=(snakeParts.size()*2);
        mat.setColor("Color", ColorRGBA.fromRGBA255(redValue,0,blueValue,255));
        snakePart.setMaterial(mat);
        snakePart.setLocalTranslation(x, y, 0);
        snakeParts.add(snakePart);
        rootNode.attachChild(snakePart);

    }

    private void spawnFood(int x, int y) {
        Sphere sphere = new Sphere(16, 16, 0.5f);
        food = new Geometry("Food", sphere);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Green);
        food.setMaterial(mat);
        food.setLocalTranslation(x, y, 0);
        rootNode.attachChild(food);
        System.out.println("Food init");
    }

    private void showGameAreaOneCube() {

        WireBox wirebox = new WireBox(
                GRID_SIZE_X*0.5f+ org.patzam.gamexyz.GameParameters.PLAYER_SIZE,
                GRID_SIZE_Y*0.5f+ org.patzam.gamexyz.GameParameters.PLAYER_SIZE,
                GRID_SIZE_Z*0.5f+ org.patzam.gamexyz.GameParameters.PLAYER_SIZE);

        Geometry boxGeometry = new Geometry("Wirebox", wirebox);
        Material boxMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMaterial.setColor("Color", ColorRGBA.Black);
        boxGeometry.setMaterial(boxMaterial);


        boxGeometry.setLocalTranslation(GRID_SIZE_X/2, GRID_SIZE_Y/2, GRID_SIZE_Z/2);

        // Dodawanie bloku do sceny
        rootNode.attachChild(boxGeometry);




    }

    public void setSnakeLength(int length) {
        startSnakeLength=length;
    }
}
