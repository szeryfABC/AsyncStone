package me.lemurxd.asyncstone.listeners.gui;

import me.lemurxd.asyncstone.generators.StoneCacheManager;
import me.lemurxd.asyncstone.generators.StoneGenerator;
import me.lemurxd.asyncstone.generators.gui.GeneratorMenu;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.Method;

public class GeneratorInteractEvent implements Listener {

    private final StoneCacheManager cacheManager;

    private static Method getHandMethod;
    private static boolean hasOffHand = false;

    static {
        try {
            getHandMethod = PlayerInteractEvent.class.getMethod("getHand");
            hasOffHand = true;
        } catch (NoSuchMethodException e) {
            hasOffHand = false;
        }
    }

    public GeneratorInteractEvent(StoneCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!isMainHand(event)) return;

        if (!player.isSneaking()) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        StoneGenerator generator = cacheManager.getGeneratorAt(clickedBlock.getLocation());

        if (generator != null) {
            event.setCancelled(true);

            GeneratorMenu menu = new GeneratorMenu(player, generator);
            player.openInventory(menu.getInventory());
        }
    }

    private boolean isMainHand(PlayerInteractEvent event) {
        if (!hasOffHand) return true;

        try {
            Object hand = getHandMethod.invoke(event);

            return hand != null && hand.toString().equals("HAND");
        } catch (Exception e) {
            return true;
        }
    }
}
