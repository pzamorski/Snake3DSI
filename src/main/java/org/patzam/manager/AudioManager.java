package org.patzam.manager;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;

import java.util.HashMap;
import java.util.Map;

public class AudioManager {


    public static String CRUNCH = "audio/chrup.wav";

    private static Node rootNode;
    private static AssetManager assetManager;
    private static final Map<String, AudioNode> soundPaths = new HashMap<>();

//    public AudioManager() {
//        this.rootNode = rootNode;
//        this.assetManager=assetManager;
//        init();;
//    }

    public static void init(AssetManager getAssetManager, Node getRootNode) {
        assetManager=getAssetManager;
        rootNode=getRootNode;
        soundPaths.put(AudioManager.CRUNCH, new AudioNode(assetManager, AudioManager.CRUNCH, false));

        addToRootNode();
    }


    public static Map<String, AudioNode> getSoundMap() {
        return soundPaths;
    }

    private static void addToRootNode() {
        // Dodaj każdy AudioNode do rootNode
        for (AudioNode audioNode : soundPaths.values()) {
            audioNode.setVolume(100);
            rootNode.attachChild(audioNode);
        }
    }
}
