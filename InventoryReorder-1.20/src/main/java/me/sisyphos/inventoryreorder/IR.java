package me.sisyphos.inventoryreorder;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Objects;

public final class IR extends JavaPlugin {

    // General
    private Utils u;
    private SortingManager sm;
    private InventoryListener il;

    // Directories
    private String dirSplit;
    private String workingDir;
    private String pluginDir;
    private String configDir;

    // Config
    private String activeSort;
    private String[] customSortNames;

    public void onEnable() {

        // Plugin general startup
        u = Utils.getInstance();
        sm = SortingManager.getInstance();
        il = InventoryListener.getInstance();
        this.getServer().getPluginManager().registerEvents((Listener) il, (Plugin) this);

        // Config file import
        setDirectories();
        mkConfig();
        sm.setActiveSort(activeSort);
        sm.setCustomSort(customSortNames, pluginDir);

        Bukkit.getLogger().info("Loaded!!!");
    }


    public void onDisable() {
        // Plugin shutdown logic
        exportConfig();
    }

    // Directories
    public void setDirectories() {
        establishOS();
        try {
            workingDir = new File(".").getCanonicalPath();
            pluginDir = workingDir + dirSplit + "plugins" + dirSplit + "InventoryReorder" + dirSplit;
            configDir = pluginDir + "config.yml";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void establishOS() {
        String OS = System.getProperty("os.name").toLowerCase();
        if (OS.contains("win")) {
            dirSplit = "\\";
        } else if (OS.contains("mac") || OS.contains("nix") || OS.contains("nux") || OS.contains("aix") || OS.contains("sunos")) {
            dirSplit = "/";
        } else {
            dirSplit = null;
        }
    }

    // Config file
    public void mkConfig() {
        File config = new File(configDir);
        if(config.mkdir()) {
            // Message of creating new Plugin folder
        } else {
            // Message of finding existing Plugin folder
        }
        try {
            if(config.createNewFile()) {
                exportConfig();
                // Message of creating new config file
            } else {
                importConfig();
                // Message of finding existing config file
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportConfig() {
        try {
            FileWriter fw = new FileWriter(configDir);
            BufferedWriter bw = new BufferedWriter(fw);
            int num;

            // Active sorting order and available ones
            num = 0;
            bw.write("# If you want to change the active sorting order change it in this setting,\n" +
                         "# choosing from the available ones below or your own added in the next setting\n");
            bw.write("# Available sorting orders: [");
            for(String n: sm.getAllSortingNames()) {
                if(num > 0) {
                    bw.write(",");
                }
                bw.write(" " + n + " ");
                num++;
            }
            bw.write("]\n");
            bw.write("active-sort: " + sm.getActiveSortName() + "\n\n");

            // Custom orders list
            num = 0;
            bw.write("# If you have created your own sorting order in a file make sure the name of the file is in the list below\n" +
                         "# Make sure the name of the file names contain the extension and are separated by a comma\n");
            bw.write("custom-sort-filenames: [");
            for(String n: sm.getCustomSortingNames()) {
                if(num > 0) {
                    bw.write(",");
                }
                bw.write(" " + n + " ");
                num++;
            }
            bw.write("]\n\n");

            bw.flush();
            fw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importConfig() {
        try {
            InputStreamReader in = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(configDir)));
            BufferedReader br = new BufferedReader(in);

            String line;
            while((line = br.readLine()) != null) {
                if(!line.startsWith("#")) {
                    line = u.removeYamlComment(line);
                    String setting = line.split(":")[0].toLowerCase().replace(" ", "");
                    String args[] = line.split(":")[1].replace(" ", "").replace("[", "").replace("]", "").split(",");

                    switch (setting) {
                        case("active-sort"):
                            activeSort = args[0];
                            break;
                        case("custom-sort-filenames"):
                            customSortNames = args.clone();
                            break;
                        default:
                            break;
                    }
                }
            }

            br.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
