package ramscoop;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class serves as a compatibility layer for save files when the mod is disabled or removed.
 * It has the exact same serialization signature as the main Ramscoop class, 
 * allowing the game to deserialize saved instances without crashing.
 */
public class RamscoopSaveProxy implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Dummy fields to match the serialization structure
    private ArrayList<?> compatibilityList = new ArrayList<>();
    
    // Empty constructor
    public RamscoopSaveProxy() {}
    
    // Getters and setters to handle conversion errors
    public void setScripts(ArrayList<?> list) {
        compatibilityList = list != null ? list : new ArrayList<>();
    }
    
    public ArrayList<?> getScripts() {
        return compatibilityList;
    }
}
