package org.patzam.gamexyz;


import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapText;
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
import java.util.*;

import static org.patzam.gamexyz.GameParameters.WITH_HELPER_LINE;


public class Game2Snake extends SimpleApplication implements ActionListener {


    private transient Position[] snakePosition = new Position[1600];
    private static LinkedList<Geometry> snakeParts; // Ciało węża
    private Geometry snakePart;// Segment weza
    private Geometry food; // Jedzenie
    private Geometry test; // Jedzenie

    private Direction currentDirection = Direction.RIGHT;
    private boolean inGame = true;

    private transient Position foodPosition;
    private int snakeLength;

    private static final int GRID_SIZE_X = GameParameters.GAME_DIMENSIONS_X;
    private static final int GRID_SIZE_Y = GameParameters.GAME_DIMENSIONS_Y;
    private static final int GRID_SIZE_Z = GameParameters.GAME_DIMENSIONS_Z;
    private static final int GRID_SIZE = GRID_SIZE_X;
    private int oldX;
    private int oldY;
    private int oldZ;
    private Position oldPositionHead = new Position(0, 0, 0);
    private BitmapText scoreText;
    private BitmapText statusText;

    private float radius = GRID_SIZE_X*3; // Promień orbity
    private float angle = 20f;   // Początkowy kąt
    private float rotationSpeed = 0.1f; // Szybkość obrotu kamery
    private int startSnakeLength =6;

    public Game2Snake() {
        AppSettings settings = new AppSettings(true);
//       settings.setResolution(1920, 1080);
        settings.setResolution(600, 400);
//        settings.setFullscreen(true);
        setSettings(settings);
        init();
    }

    public static void main(String[] args) {
        Game2Snake game2Snake = new Game2Snake();
        game2Snake.start();
    }


    private void initScore(){
        scoreText = new BitmapText(guiFont, false);
        scoreText.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText.setColor(ColorRGBA.White);
        scoreText.setText("Score: 0" ); // Początkowa wartość punktów
        scoreText.setLocalTranslation(settings.getWidth() - scoreText.getLineWidth() - 10, settings.getHeight() - 10, 0);

        // Dodaj tekst do interfejsu użytkownika
        guiNode.attachChild(scoreText);
    }

    private void initStatusText(){
        statusText = new BitmapText(guiFont, false);
        statusText.setSize(guiFont.getCharSet().getRenderedSize());
        statusText.setColor(ColorRGBA.White);
        //scoreText.setText("Score: 0" ); // Początkowa wartość punktów
        statusText.setLocalTranslation(10, settings.getHeight() - 10, 0);

        // Dodaj tekst do interfejsu użytkownika
        guiNode.attachChild(statusText);
    }


    @Override
    public void simpleInitApp() {
        getRootNode().attachChild(SkyFactory.createSky(getAssetManager(), "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
        assetManager.registerLocator("assets", FileLocator.class);

        if (WITH_HELPER_LINE) {
            showGameArea();
        }
        showGameAreaOneCube();

        initCam();
        spawnFood(foodPosition.getX(), foodPosition.getY(), foodPosition.getZ());
        initSnake();
        initScore();
        initStatusText();

    }


    @Override
    public void simpleUpdate(float tpf) {

        if (oldX != foodPosition.getX() || oldY != foodPosition.getY() || oldZ != foodPosition.getZ()) {
            food.setLocalTranslation(foodPosition.getX(), foodPosition.getY(), foodPosition.getZ());
            oldX = foodPosition.getX();
            oldY = foodPosition.getY();
            oldZ = foodPosition.getZ();
        }

        if (oldPositionHead.getX() != getHeadPosition().getX() || oldPositionHead.getY() != getHeadPosition().getY() || oldPositionHead.getZ() != getHeadPosition().getZ()) {

            scoreText.setText("Score: " + getScore());
            statusText.setText("Head "+getHeadPosition().toString()+"\n"+"Food "+foodPosition.toString());

            int j = 0;
            for (; null != snakePosition[j]; j++) {
            }

            for (int i = 0; null != snakePosition[i]; i++) {
                if (snakeParts.size() < j) {
                    spawnSnakePart(snakePosition[i].getX(), snakePosition[i].getY(), snakePosition[i].getZ());
                }
                if (snakeParts.size() > j) {
                    initSnake();
                }
                snakeParts.get(i).setLocalTranslation(snakePosition[i].getX(), snakePosition[i].getY(), snakePosition[i].getZ());
            }

            oldPositionHead = new Position(getHeadPosition().getX(), getHeadPosition().getY(), getHeadPosition().getZ());
            draw();
        }


//        angle += rotationSpeed * tpf;
//        float x = FastMath.sin(angle) * radius;
//        float z = FastMath.cos(angle) * radius;
//        cam.setLocation(new Vector3f(GRID_SIZE_X/2+x, GRID_SIZE_Y/2, GRID_SIZE_Z/2+z));
//        cam.lookAt(new Vector3f(GRID_SIZE_X/2,GRID_SIZE_Y,GRID_SIZE_Z/2), Vector3f.UNIT_Y);


    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (isOngoing()) {
            if (isFoodEaten()) {
                snakeLength++;

                setNewFoodPosition();
            } else {
                final Position headPosition = getHeadPosition();
                inGame = !headPosition.isOutsideTheArea();

                if (inGame) {
                    checkIfPlayerHeadIsCollidingWithOtherBodyParts(headPosition);
                }
            }
        }

        if (!inGame) {
            System.out.println("                You tried very hard, but you lost the game.");
        }

    }


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
            case MOVE_IN:
                if (currentDirection == Direction.OUT) break;
                currentDirection = Direction.IN;
                break;
            case MOVE_OUT:
                if (currentDirection == Direction.IN) break;
                currentDirection = Direction.OUT;
                break;
        }
    }

    public GameContext init() {
        System.out.println("Init");
        snakeLength = startSnakeLength;

        snakePosition = new Position[1600];
        //snakeParts=new LinkedList<>();

        for (int i = 0; i < snakeLength; i++) {
            snakePosition[i] = new Position(GRID_SIZE_X/ 2 - i, GRID_SIZE_Y / 2, GRID_SIZE_Z/2);
        }


        setNewFoodPosition();

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
                GameContextManager.getStateForDirection(snakePosition, foodPosition, Direction.IN),
              //GameContextManager.getStateForDirection(snakePosition, foodPosition, Direction.OUT),
        });
    }

    public double calculateRewardForActionToTake(final ActionMove action) {
        return RewardManager.calculateRewardForActionToTake(action, snakePosition, foodPosition);
    }

    private void draw() {
        if (!isOngoing()) {
            return;
        }

        // Draw snake
        for (int i = 0; i < snakeLength; i++) {
            // Position of one of the snake parts (head or tail)
            final Position pos = snakePosition[i];
            if (pos == null) {
                continue;
            }

        }

        final Position headPosition = getHeadPosition();
        final Position[] observations = new Position[NetworkManagerXYZ.NUMBER_OF_INPUTS];
        observations[0] = PositionManager.getNextPosition(headPosition, Direction.UP);
        observations[1] = PositionManager.getNextPosition(headPosition, Direction.RIGHT);
        observations[2] = PositionManager.getNextPosition(headPosition, Direction.DOWN);
        observations[3] = PositionManager.getNextPosition(headPosition, Direction.LEFT);
        observations[4] = PositionManager.getNextPosition(headPosition, Direction.IN);
        //observations[5] = PositionManager.getNextPosition(headPosition, Direction.OUT);
//        for (int i = 0; i < observations.length; i++) {
//            final Position pos = observations[i];
//            if (pos == null) {
//                continue;
//            }
//
//        }
        Toolkit.getDefaultToolkit().sync();
    }

    private void setNewFoodPosition() {

        Random random = new Random();
        foodPosition = new Position(random.nextInt(GRID_SIZE_X),
                random.nextInt(GRID_SIZE_Y),
                random.nextInt(GRID_SIZE_Z)
        );

        if (Arrays.asList(snakePosition).contains(foodPosition)) {
            setNewFoodPosition();
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

    private void showGameAreaOneCube() {

                    WireBox wirebox = new WireBox(
                            GRID_SIZE_X*0.5f+GameParameters.PLAYER_SIZE,
                            GRID_SIZE_Y*0.5f+GameParameters.PLAYER_SIZE,
                            GRID_SIZE_Z*0.5f+GameParameters.PLAYER_SIZE);

                    Geometry boxGeometry = new Geometry("Wirebox", wirebox);
                    Material boxMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    boxMaterial.setColor("Color", ColorRGBA.Black);
                    boxGeometry.setMaterial(boxMaterial);


                    boxGeometry.setLocalTranslation(GRID_SIZE_X/2, GRID_SIZE_Y/2, GRID_SIZE_Z/2);

                    // Dodawanie bloku do sceny
                    rootNode.attachChild(boxGeometry);




    }

    private void initCam() {
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(60f);
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
            spawnSnakePart(snakePosition[i].getX(), snakePosition[i].getY(), snakePosition[i].getZ());
        }
    }

    private void spawnSnakePart(int x, int y, int z) {
        Box box = new Box(0.5f, 0.5f, 0.5f);
        snakePart = new Geometry("SnakePart", box);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        int greenValue=255-(snakeParts.size()*2);
        int blueValue=(snakeParts.size()*2);
        mat.setColor("Color", ColorRGBA.fromRGBA255(0,greenValue,blueValue,255));
        snakePart.setMaterial(mat);
        snakePart.setLocalTranslation(x, y, z);
        snakeParts.add(snakePart);
        rootNode.attachChild(snakePart);

    }

    private void spawnFood(int x, int y, int z) {
        Sphere sphere = new Sphere(16, 16, 0.5f);
        food = new Geometry("Food", sphere);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        food.setMaterial(mat);
        food.setLocalTranslation(x, y, z);
        rootNode.attachChild(food);
        System.out.println("Food init");
    }


}
