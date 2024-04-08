package game;


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
import manager.AudioManager;
import manager.GameContextManager;
import manager.PositionManager;
import manager.RewardManager;
import move.ActionMove;
import move.Direction;
import move.Position;
import network.GameContext;
import network.NetworkManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Random;


public class Game extends SimpleApplication {


    private static LinkedList<Geometry> snakeParts;

    private Geometry food;
    private BitmapText scoreText, statusText, modeText;

    private AudioNode crunch;

    private Direction currentDirection = Direction.RIGHT;

    private boolean inGame = true;

    private Position foodPosition;
    private Position oldPositionHead, oldPositionFood;
    private Position[] snakePosition = new Position[5000];

    private int snakeLength;
    private final int startSnakeLength = GameParameters.DEFAULT_SNAKE_LENGTH;

    private String status;


    public Game() {
        AppSettings settings = new AppSettings(true);

        if (GameParameters.FULL_SCREEN) {
            settings.setResolution(1920, 1080);
            settings.setFullscreen(true);
        }else {
            settings.setResolution(800, 600);
        }
        setSettings(settings);
        init();
        start();
    }
    

    @Override
    public void simpleInitApp() {

        InitializerGame.create(getAssetManager(), rootNode, guiFont, guiNode);
        InitializerGame.initSky();
        InitializerGame.initArea();
        InitializerGame.initCam(flyCam, cam);
        InitializerGame.initAudiManager();

        scoreText = InitializerGame.initText("Score: 0", settings.getWidth() - 120, settings.getHeight());
        statusText = InitializerGame.initText("Status", 10, settings.getHeight() - 10);
        modeText = InitializerGame.initText("Mode", settings.getWidth() - 120, 30);

        crunch = AudioManager.getSoundMap().get(AudioManager.CRUNCH);

        spawnFood(foodPosition);
        initSnake();


    }


    @Override
    public void simpleUpdate(float tpf) {


        if (foodPosition.areAnyCoordinatesChanged(oldPositionFood)) {

            food.setLocalTranslation(foodPosition.ToVector3f());
            oldPositionFood = new Position(foodPosition);

            crunch.stop();
            crunch.play();
            scoreText.setText("Score: " + getScore());
        }

        if (getHeadPosition().areAnyCoordinatesChanged(oldPositionHead)) {

            statusText.setText("Head " + getHeadPosition().toString() + "\n" + "Food " + foodPosition.toString());
            modeText.setText(status);

            int snakePositionLength = 0;
            while (snakePosition[snakePositionLength] != null) {
                snakePositionLength++;
            }
            for (int i = 0; i < snakePositionLength; i++) {
                if (snakeParts.size() <= i) {
                    spawnSnakePart(snakePosition[i]);
                }
                snakeParts.get(i).setLocalTranslation(snakePosition[i].ToVector3f());
            }
            if (snakeParts.size() > snakePositionLength) {
                for (int i = snakeParts.size() - 1; i >= snakePositionLength; i--) {
                    rootNode.detachChild(snakeParts.get(i));
                    snakeParts.remove(i);
                }
            }

            oldPositionHead = new Position(getHeadPosition());

            getObservations();


        }


    }


    private void updateGameLogic() {
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
        if (snakeLength - 1 >= 0) System.arraycopy(snakePosition, 0, snakePosition, 1, snakeLength - 1);

        final Position previousHeadPosition = snakePosition[1];
        snakePosition[0] = PositionManager.getNextPosition(previousHeadPosition, currentDirection);
        updateGameLogic();
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

        int setGridSizeZ = 0;
        if (GameParameters.BOX_DIMENSIONS_Z > 1) {
            setGridSizeZ = GameParameters.BOX_DIMENSIONS_Z / 2;
        }

        for (int i = 0; i < snakeLength; i++) {
            snakePosition[i] = new Position(GameParameters.BOX_DIMENSIONS_X / 2 - i, GameParameters.BOX_DIMENSIONS_Y / 2, setGridSizeZ);
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

    private void getObservations() {
        if (!isOngoing()) {
            return;
        }

        final Position headPosition = getHeadPosition();
        final Position[] observations = new Position[NetworkManager.NUMBER_OF_INPUTS];
        observations[0] = PositionManager.getNextPosition(headPosition, Direction.UP);
        observations[1] = PositionManager.getNextPosition(headPosition, Direction.RIGHT);
        observations[2] = PositionManager.getNextPosition(headPosition, Direction.DOWN);
        observations[3] = PositionManager.getNextPosition(headPosition, Direction.LEFT);
        observations[4] = PositionManager.getNextPosition(headPosition, Direction.IN);
        observations[5] = PositionManager.getNextPosition(headPosition, Direction.OUT);

    }

    private void setNewFoodPosition() {

        Random random = new Random();
        int setGridSizeZ = 0;
        if (GameParameters.BOX_DIMENSIONS_Z > 1) {
            setGridSizeZ = GameParameters.BOX_DIMENSIONS_Z;
            setGridSizeZ = random.nextInt(setGridSizeZ);
        }
        foodPosition = new Position(random.nextInt(GameParameters.BOX_DIMENSIONS_X),
                random.nextInt(GameParameters.BOX_DIMENSIONS_Y),
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
            spawnSnakePart(snakePosition[i]);
        }
    }

    private void spawnSnakePart(Position position) {
        Box box = new Box(0.5f, 0.5f, 0.5f);
        Geometry snakePart = new Geometry("SnakePart", box);

        Texture texture = assetManager.loadTexture("textures/snake/snakeSkin.jpg"); // Wczytujemy teksturę

        // Sprawdzamy, czy dodajemy pierwszą część węża (głowę)
        if (snakeParts.isEmpty()) {

            Material headMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            headMat.setColor("Color", ColorRGBA.Blue); // Ustawiamy kolor na niebieski dla głowy
            snakePart.setMaterial(headMat);
        } else {
            Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            mat.setTexture("ColorMap", texture); // Ustawiamy teksturę jako mapę kolorów
            int redValue = (snakeParts.size());
            int greenValue = (snakeParts.size());
            int blueValue = (snakeParts.size());
            mat.setColor("Color", ColorRGBA.fromRGBA255(redValue, greenValue, blueValue, 255));
            snakePart.setMaterial(mat);
        }

        snakePart.setLocalTranslation(position.ToVector3f());
        snakeParts.add(snakePart);
        rootNode.attachChild(snakePart);
    }


    private void spawnFood(Position position) {
        Sphere sphere = new Sphere(16, 16, 1f);
        food = new Geometry("Food", sphere);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Red);
        food.setMaterial(mat);
        food.setLocalTranslation(position.ToVector3f());
        rootNode.attachChild(food);
    }


    public void setStatusText(String text) {
        status = text;
    }
}
