package me.sisyphos.inventoryreorder;

public class Utils {

    private static  Utils instance;

    // Constructor
    public Utils() {
    }

    // Instance
    public static Utils getInstance() {
        if(instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    // String manipulation
    public String removeYamlComment(String s) {
        if(s.contains("#")) {
            s = s.split("#")[0];
        }
        return s;
    }
}
