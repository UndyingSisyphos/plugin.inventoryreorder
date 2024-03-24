package me.sisyphos.inventoryreorder;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeMap;

public class InventoryListener implements Listener {

    private static InventoryListener instance;
    private final ArrayList<InventoryType> allowedInventories = new ArrayList<InventoryType>() {{
        add(InventoryType.CREATIVE);
        add(InventoryType.BARREL);
        add(InventoryType.CHEST);
        add(InventoryType.ENDER_CHEST);
        add(InventoryType.PLAYER);
        add(InventoryType.SHULKER_BOX);
    }};
    private ArrayList<Material> activeSort;

    // Constructor
    public InventoryListener() {
    }

    // Instance
    public static InventoryListener getInstance() {
        if(instance == null) {
            instance = new InventoryListener();
        }
        return instance;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Bukkit.getLogger().info("Click " + e.getClick().toString());
        if(e.getClick().equals(ClickType.DOUBLE_CLICK)) {
            Bukkit.getLogger().info("Border");
            if(allowedInventories.contains(Objects.requireNonNull(e.getClickedInventory()).getType())) {
                Bukkit.getLogger().info("Allowed");
                if(e.getWhoClicked() instanceof Player) {
                    Bukkit.getLogger().info("Player");
                    sortInv(e);
                }
            }
        }
    }

    // Sort
    public void sortInv (InventoryClickEvent e) {
        int start = 0;
        int end = e.getClickedInventory().getSize();
        if(e.getClickedInventory().equals(e.getWhoClicked().getInventory())) {
            start += 9;
            end -= 5;
        }

        ItemStack[] inv = e.getClickedInventory().getContents();
        TreeMap<Integer,ArrayList<ItemStack>> sorted = new TreeMap<>();

        // Sort ItemStacks by Material
        for(int c = start; c < inv.length; c++) {
            if(inv[c] != null) {
                int key = activeSort.indexOf(inv[c].getType());
                if(sorted.containsKey(key)) {
                    sorted.get(key).add(inv[c]);
                } else {
                    ArrayList<ItemStack> is = new ArrayList<>();
                    is.add(inv[c]);
                    sorted.put(key, is);
                }
            }
        }

        // Group Similar ItemStacks together and simplify values
        for(ArrayList<ItemStack> list: sorted.values()) {
            for(int c = 0; c < list.size()-1; c++) {
                ItemStack i = list.get(c);
                ItemStack j;
                for(int k = list.size()-1; k > c; k--) {
                    if(i.isSimilar(list.get(k))) {
                        j = list.get(k).clone();
                        list.remove(k);

                        int transfer = Math.min(j.getAmount(), (i.getMaxStackSize() - i.getAmount()));
                        i.setAmount(i.getAmount() + transfer);

                        if(transfer != j.getAmount()) {
                            j.setAmount(j.getAmount() - transfer);
                            list.add(c + 1, j);
                            break;
                        }
                    }
                }
            }

            list.sort(Comparator.comparingInt(ItemStack::getAmount).reversed());
        }

        // Sort by ItemMeta
        for(ArrayList<ItemStack> list: sorted.values()) {

            list.sort(Comparator.comparingInt(ItemStack::getDurability).reversed());

            TreeMap<String,ItemStack> listed = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            ArrayList<ItemStack> noMeta = new ArrayList<>();

            int index = 0;
            for(ItemStack i: list) {
                if(i.hasItemMeta()) {
                    ItemMeta m = i.getItemMeta();
                    int rank = 0;
                    String s = "";

                    if(m instanceof SkullMeta) {
                        SkullMeta sm = (SkullMeta) m;
                        s = ((SkullMeta) m).getOwningPlayer().getName();
                    }
                    if(m.hasDisplayName()) {
                        rank += 1;
                        s = m.getDisplayName();
                    }
                    if(m.hasLore()) {
                        rank += 2;
                    }
                    if(m.hasEnchants()) {
                        rank += 4;
                    }
                    if(!s.equals("")) {
                        s = "\uE000" + s;
                    }

                    listed.put(rank + "-" + s + "-" + index, i);
                    index++;
                } else {
                    noMeta.add(i);
                }
            }

            list.clear();
            list.addAll(noMeta);
            list.addAll(listed.values());
        }

        // Clear inventory and fill it with the sorted stuff
        for(int c = start; c < end; c++) {
            e.getClickedInventory().clear(c);
        }
        for(ArrayList<ItemStack> l: sorted.values()) {
            for(ItemStack i: l) {
                e.getClickedInventory().setItem(start, i);
                start++;
            }
        }
    }

    // Setters
    public void setActiveSort(ArrayList<Material> list) {
        activeSort = new ArrayList<>(list);
    }
}
