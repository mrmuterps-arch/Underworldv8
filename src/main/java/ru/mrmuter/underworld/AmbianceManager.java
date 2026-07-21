package ru.mrmuter.underworld;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * Звуковая атмосфера Мёртвой пустоши:
 *  - при входе в мир — гул портала;
 *  - раз в ~20 минут — жуткий "большой" звук (рёв варденов вдалеке);
 *  - случайно и часто рядом с игроком — звук появления вардена БЕЗ вардена,
 *    шёпоты скалка, дальний гул. Держит в напряжении.
 */
public class AmbianceManager extends BukkitRunnable {

    private final UnderworldPlugin plugin;
    private final Random random = new Random();
    private long tick = 0L;
    private long nextBigScare = 0L;

    private static final Sound[] CREEPY = {
            Sound.ENTITY_WARDEN_AMBIENT,
            Sound.ENTITY_WARDEN_LISTENING_ANGRY,
            Sound.ENTITY_WARDEN_NEARBY_CLOSE,
            Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD,
            Sound.BLOCK_SCULK_SHRIEKER_SHRIEK,
            Sound.ENTITY_ENDERMAN_STARE
    };

    public AmbianceManager(UnderworldPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        World w = plugin.getUnderworld();
        if (w == null) return;
        tick += 40L; // задача крутится раз в 40 тиков (2 сек)

        // Большой скример раз в ~20 минут
        if (tick >= nextBigScare) {
            for (Player p : w.getPlayers()) {
                p.playSound(p, Sound.ENTITY_WARDEN_ROAR, 1f, 0.6f);
            }
            nextBigScare = tick + 20L * 60L * 20L; // 20 минут
        }

        // Частый случайный звук появления вардена (БЕЗ вардена) рядом с игроком
        for (Player p : w.getPlayers()) {
            if (random.nextInt(100) < 12) { // ~раз в несколько циклов
                Location near = p.getLocation().clone().add(
                        random.nextInt(21) - 10, random.nextInt(5) - 2, random.nextInt(21) - 10);
                // именно "спавн вардена" как ты просил — без самого вардена
                p.playSound(near, Sound.ENTITY_WARDEN_EMERGE, 0.9f, 0.8f + random.nextFloat() * 0.3f);
            } else if (random.nextInt(100) < 20) {
                // фоновые жути
                Location near = p.getLocation().clone().add(
                        random.nextInt(31) - 15, random.nextInt(7) - 3, random.nextInt(31) - 15);
                p.playSound(near, CREEPY[random.nextInt(CREEPY.length)], 0.7f, 0.6f + random.nextFloat() * 0.4f);
            }
        }
    }

    /** Вызывается при попадании игрока в подмир (из VoidFallListener). */
    public static void playEntrySound(Player player) {
        player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, 1f, 0.5f);
        player.playSound(player, Sound.ENTITY_WARDEN_EMERGE, 1f, 0.7f);
    }
}
