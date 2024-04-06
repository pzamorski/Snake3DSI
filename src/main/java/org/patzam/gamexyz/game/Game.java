package org.patzam.gamexyz.game;


import com.jme3.app.SimpleApplication;
import com.jme3.audio.AudioNode;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;

import com.jme3.scene.Geometry;

import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;

import org.patzam.gamexyz.ActionMove;
import org.patzam.gamexyz.Direction;
import org.patzam.gamexyz.Position;
import org.patzam.gamexyz.manager.AudioManager;
import org.patzam.gamexyz.manager.GameContextManager;
import org.patzam.gamexyz.manager.PositionManager;
import org.patzam.gamexyz.manager.RewardManager;
import org.patzam.gamexyz.network.GameContext;
import org.patzam.gamexyz.network.NetworkManagerXYZ;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


public class Game extends SimpleApplication implements ActionListener {


    private transient Position[] snakePosition = new Position[5000];
    private static LinkedList<Geometry> snakeParts;
    private Geometry snakePart;
    private Geometry food;


    private Direction currentDirection = Direction.RIGHT;
    private boolean inGame = true;

    private transient Position foodPosition;
    private int snakeLength;

    private static final int GRID_SIZE_X = GameParameters.GAME_DIMENSIONS_X;
    private static final int GRID_SIZE_Y = GameParameters.GAME_DIMENSIONS_Y;
    private static final int GRID_SIZE_Z = GameParameters.GAME_DIMENSIONS_Z;
    private int oldX;
    private int oldY;
    private int oldZ;
    private Position oldPositionHead = new Position(0, 0, 0);
    private BitmapText scoreText, statusText, modeText;


    private int startSnakeLength = 6;
    public String status;

    AudioNode crunch;

    public Game() {
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1200, 800);
//        settings.setResolution(600, 400);
//        settings.setFullscreen(true);
        setSettings(settings);
        init();
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }


    private void initScore() {
        scoreText = new BitmapText(guiFont, false);
        scoreText.setSize(guiFont.getCharSet().getRenderedSize());
        scoreText.setColor(ColorRGBA.White);
        scoreText.setText("Score: 0"); // Początkowa wartość punktów
        scoreText.setLocalTranslation(settings.getWidth() - scoreText.getLineWidth() - 30, settings.getHeight() - 10, 0);

        // Dodaj tekst do interfejsu użytkownika
        guiNode.attachChild(scoreText);
    }

    private void initStatusText() {
        statusText = new BitmapText(guiFont, false);
        statusText.setSize(guiFont.getCharSet().getRenderedSize());
        statusText.setColor(ColorRGBA.White);
        statusText.setLocalTranslation(10, settings.getHeight() - 10, 0);

        // Dodaj tekst do interfejsu użytkownika
        guiNode.attachChild(statusText);
    }

    private void setText() {
        modeText = new BitmapText(guiFont, false);
        modeText.setSize(guiFont.getCharSet().getRenderedSize());
        modeText.setColor(ColorRGBA.White);
        modeText.setLocalTranslation(settings.getWidth() - modeText.getLineWidth() - 80, 30, 0); // Ustawiamy lokalizację tekstu w prawym dolnym rogu

        // Dodaj tekst do interfejsu użytkownika
        guiNode.attachChild(modeText);
    }

    @Override
    public void simpleInitApp() {

        InitializerGame.create(getAssetManager(),rootNode);
        InitializerGame.initSky();
        InitializerGame.initArea();
        InitializerGame.initCam(flyCam,cam);


        spawnFood(foodPosition.getX(), foodPosition.getY(), foodPosition.getZ());
        initSnake();
        initScore();
        setText();
        initStatusText();
        initAudio();



    }

    private void initAudio() {
        AudioManager.init(assetManager, rootNode);
        crunch = AudioManager.getSoundMap().get(AudioManager.CRUNCH);
    }


    @Override
    public void simpleUpdate(float tpf) {

        if (oldX != foodPosition.getX() || oldY != foodPosition.getY() || oldZ != foodPosition.getZ()) {
            food.setLocalTranslation(foodPosition.getX(), foodPosition.getY(), foodPosition.getZ());
            oldX = foodPosition.getX();
            oldY = foodPosition.getY();
            oldZ = foodPosition.getZ();


            crunch.stop();
            crunch.play();

        }

        if (oldPositionHead.getX() != getHeadPosition().getX() || oldPositionHead.getY() != getHeadPosition().getY() || oldPositionHead.getZ() != getHeadPosition().getZ()) {

            scoreText.setText("Score: " + getScore());

            statusText.setText("Head " + getHeadPosition().toString() + "\n" + "Food " + foodPosition.toString());
            modeText.setText(status);
            int j = 0;
            for (; null != snakePosition[j]; j++) {
            }

            try {
                for (int i = 0; null != snakePosition[i]; i++) {
                    if (snakeParts.size() < j) {
                        spawnSnakePart(snakePosition[i].getX(), snakePosition[i].getY(), snakePosition[i].getZ());
                    }
                    if (snakeParts.size() > j) {
                        initSnake();
                    }
                    snakeParts.get(i).setLocalTranslation(snakePosition[i].getX(), snakePosition[i].getY(), snakePosition[i].getZ());
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println(e);
            }
            ;


            oldPositionHead = new Position(getHeadPosition().getX(), getHeadPosition().getY(), getHeadPosition().getZ());
            draw();


        }


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
            // System.out.println("                You tried very hard, but you lost the game.");
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
        snakeLength = startSnakeLength;

        snakePosition = new Position[5000];
        //snakeParts=new LinkedList<>();

        int setGridSizeZ = 0;
        if (GRID_SIZE_Z > 1) {
            setGridSizeZ = GRID_SIZE_Z / 2;
        }

        for (int i = 0; i < snakeLength; i++) {
            snakePosition[i] = new Position(GRID_SIZE_X / 2 - i, GRID_SIZE_Y / 2, setGridSizeZ);
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
                GameContextManager.getStateForDirection(snakePosition, foodPosition, Direction.OUT),
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
        observations[5] = PositionManager.getNextPosition(headPosition, Direction.OUT);
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
        int setGridSizeZ = 0;
        if (GRID_SIZE_Z > 1) {
            setGridSizeZ = GRID_SIZE_Z;
            setGridSizeZ = random.nextInt(setGridSizeZ);
        }
        foodPosition = new Position(random.nextInt(GRID_SIZE_X),
                random.nextInt(GRID_SIZE_Y),
                setGridSizeZ
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

        Texture texture = assetManager.loadTexture("textures/snake/snakeSkin.jpg"); // Wczytujemy teksturę

        // Sprawdzamy, czy dodajemy pierwszą część węża (głowę)
        if (snakeParts.isEmpty()) {

            Material headMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            headMat.setColor("Color", ColorRGBA.Blue); // Ustawiamy kolor na niebieski dla głowy
            snakePart.setMaterial(headMat);
        } else {
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setTexture("ColorMap", texture); // Ustawiamy teksturę jako mapę kolorów
            int redValue=(snakeParts.size());
            int greenValue = (snakeParts.size());
            int blueValue = (snakeParts.size());
            mat.setColor("Color", ColorRGBA.fromRGBA255(redValue, greenValue, blueValue, 255));
            snakePart.setMaterial(mat);
        }

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
