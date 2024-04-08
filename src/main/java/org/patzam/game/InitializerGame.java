package org.patzam.game;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.FlyByCamera;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.WireBox;
import com.jme3.util.SkyFactory;
import org.patzam.manager.AudioManager;

public class InitializerGame {

    private static AssetManager assetManager;
    private static Node rootNode;
    private static BitmapFont guiFont;
    private static Node guiNode;

    public static void create(AssetManager getAssetManager, Node getRootNode, BitmapFont getGuiFont, Node getGuiNode){
        assetManager = getAssetManager;
        rootNode = getRootNode;
        guiFont = getGuiFont;
        guiNode = getGuiNode;
    }


    public static void initSky() {
        rootNode.attachChild(SkyFactory.createSky(assetManager, "textures/sky/BrightSky.dds", SkyFactory.EnvMapType.CubeMap));
    }

    public static void initArea() {
        WireBox wirebox = new WireBox(
                GameParameters.BOX_DIMENSIONS_X * 0.5f + GameParameters.PLAYER_SIZE,
                GameParameters.BOX_DIMENSIONS_Y * 0.5f + GameParameters.PLAYER_SIZE,
                GameParameters.BOX_DIMENSIONS_Z * 0.5f + GameParameters.PLAYER_SIZE);

        Geometry boxGeometry = new Geometry("Wirebox", wirebox);
        Material boxMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMaterial.setColor("Color", ColorRGBA.Black);
        boxGeometry.setMaterial(boxMaterial);


        boxGeometry.setLocalTranslation(GameParameters.BOX_DIMENSIONS_X / 2,
                GameParameters.BOX_DIMENSIONS_Y / 2,
                GameParameters.BOX_DIMENSIONS_Z / 2);

        rootNode.attachChild(boxGeometry);

    }

    public static void initCam(FlyByCamera flyCam,Camera cam) {
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(60f);
        cam.setLocation(new Vector3f(GameParameters.BOX_DIMENSIONS_X / 2f,
                GameParameters.BOX_DIMENSIONS_Y / 2f,
                GameParameters.BOX_DIMENSIONS_Z * 2));

        cam.lookAt(new Vector3f(GameParameters.BOX_DIMENSIONS_X / 2f,
                GameParameters.BOX_DIMENSIONS_Y / 2f,
                0), Vector3f.UNIT_Z);
    }

    public static void initAudiManager() {
        AudioManager.init(assetManager, rootNode);
    }

    public static BitmapText initText(String text, float x, float y) {
        BitmapText bitmapText = new BitmapText(guiFont, false);
        bitmapText.setSize(guiFont.getCharSet().getRenderedSize());
        bitmapText.setColor(ColorRGBA.White);
        bitmapText.setText(text);
        bitmapText.setLocalTranslation(x,  y, 0);

        guiNode.attachChild(bitmapText);

        return bitmapText;
    }
}
