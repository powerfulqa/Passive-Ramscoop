package ramscoop;

import com.fs.starfarer.api.BaseModPlugin;

public class TestModPlugin extends BaseModPlugin {
    static {
        System.out.println("TEST: TestModPlugin static initializer");
        try {
            java.io.FileWriter debugFile = new java.io.FileWriter("test_ramscoop_debug.txt", true);
            debugFile.write("TEST MODPLUGIN LOADED SUCCESSFULLY\n");
            debugFile.write("Time: " + new java.util.Date() + "\n");
            debugFile.close();
        } catch (Exception e) {
            // Ignore
        }
    }
    
    public TestModPlugin() {
        System.out.println("TEST: TestModPlugin constructor called");
    }
    
    @Override
    public void onApplicationLoad() {
        System.out.println("TEST: TestModPlugin.onApplicationLoad() called");
    }
}
