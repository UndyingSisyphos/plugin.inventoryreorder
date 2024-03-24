package me.sisyphos.inventoryreorder;

import org.bukkit.Material;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SortingManager {

    private static SortingManager instance;
    private Utils u;

    private final String authorSortFile = "author.txt";
    private final HashMap<String, ArrayList<Material>> sortingOrders;
    private String activeSort;

    // Constructor
    public SortingManager() {
        u = Utils.getInstance();
        sortingOrders = new HashMap<>();
        importDefaultSort();
        importAuthorSort();
        activeSort = "author";
    }
    // Instance
    public static SortingManager getInstance() {
        if(instance == null) {
            instance = new SortingManager();
        }
        return instance;
    }

    // Import Sorting Orders
    private void importDefaultSort() {
        ArrayList<Material> defaultSort = new ArrayList<>(Arrays.asList(Material.values()));
        sortingOrders.put("default", defaultSort);
    }

    private void importAuthorSort() {
        ArrayList<Material> authorSort = new ArrayList<>();
        try {
            InputStreamReader in = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/" + authorSortFile)));
            BufferedReader br = new BufferedReader(in);

            String line;
            while((line = br.readLine()) != null) {
                authorSort.add(Material.matchMaterial(line));
            }
            sortingOrders.put("author", authorSort);

            br.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importCustomSort(String name, String dir) {
        if(Files.exists(Paths.get(dir + name))) {
            ArrayList<Material> sort = new ArrayList<>();
            try {
                InputStreamReader in = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/" + name)));
                BufferedReader br = new BufferedReader(in);

                String line;
                while((line = br.readLine()) != null) {
                    if(!line.startsWith("#")) {
                        line = u.removeYamlComment(line);
                        sort.add(Material.matchMaterial(line));
                    }
                }
                sortingOrders.put(name, sort);

                br.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // File doesn't exist error
        }
    }

    // Getters
    public String getActiveSortName() {
        return activeSort;
    }

    public ArrayList<String> getAllSortingNames() {
        return new ArrayList<>(sortingOrders.keySet());
    }

    public ArrayList<String> getCustomSortingNames() {
        ArrayList<String> custom = new ArrayList<>(sortingOrders.keySet());
        custom.remove("default");
        custom.remove("author");
        return custom;
    }

    // Setters
    public void setActiveSort(String s) {
        activeSort = s;
        InventoryListener.getInstance().setActiveSort(sortingOrders.get(activeSort));
    }

    public void setCustomSort(String[] l, String dir) {
        for (String s: l) {
            importCustomSort(s, dir);
        }
    }

}
