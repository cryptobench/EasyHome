package com.homeessentials.util;

import com.homeessentials.data.Home;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Manages teleport warmups with movement cancellation.
 */
public class WarmupManager {
    private static final int WARMUP_SECONDS = 3;
    private static final double MOVEMENT_THRESHOLD = 0.5;

    private final ScheduledExecutorService scheduler;
    private final Map<UUID, WarmupData> activeWarmups;

    public WarmupManager() {
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.activeWarmups = new ConcurrentHashMap<>();
    }

    public void startWarmup(PlayerRef playerData,
                           Ref<EntityStore> playerRef,
                           Store<EntityStore> store,
                           World currentWorld,
                           Home home,
                           boolean bypassWarmup) {
        UUID playerId = playerData.getUuid();

        // Cancel any existing warmup
        cancelWarmup(playerId);

        if (bypassWarmup) {
            executeTeleport(playerData, playerRef, store, currentWorld, home);
            return;
        }

        // Get starting position for movement check
        TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
        Vector3d startPos = transform.getPosition();

        // Send warmup message
        playerData.sendMessage(Messages.warmupStarted(home.getName(), WARMUP_SECONDS));

        // Create warmup data
        WarmupData data = new WarmupData(playerData, playerRef, store, currentWorld, home,
                startPos.getX(), startPos.getY(), startPos.getZ());

        // Schedule position checks every 500ms
        ScheduledFuture<?> checkFuture = scheduler.scheduleAtFixedRate(() -> {
            checkMovement(playerId, data);
        }, 500, 500, TimeUnit.MILLISECONDS);

        // Schedule teleport after warmup
        ScheduledFuture<?> teleportFuture = scheduler.schedule(() -> {
            doTeleport(playerId);
        }, WARMUP_SECONDS, TimeUnit.SECONDS);

        data.checkFuture = checkFuture;
        data.teleportFuture = teleportFuture;
        activeWarmups.put(playerId, data);
    }

    private void checkMovement(UUID playerId, WarmupData data) {
        if (!activeWarmups.containsKey(playerId)) {
            return;
        }

        try {
            // Run position check on world thread
            data.currentWorld.execute(() -> {
                try {
                    TransformComponent transform = data.store.getComponent(data.playerRef, TransformComponent.getComponentType());
                    if (transform == null) {
                        return;
                    }

                    Vector3d currentPos = transform.getPosition();
                    double dx = currentPos.getX() - data.startX;
                    double dy = currentPos.getY() - data.startY;
                    double dz = currentPos.getZ() - data.startZ;
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    if (distance > MOVEMENT_THRESHOLD) {
                        data.playerData.sendMessage(Messages.teleportCancelled());
                        cancelWarmup(playerId);
                    }
                } catch (Exception e) {
                    // Ignore errors during movement check
                }
            });
        } catch (Exception e) {
            // Ignore
        }
    }

    private void doTeleport(UUID playerId) {
        WarmupData data = activeWarmups.remove(playerId);
        if (data == null) {
            return;
        }

        // Cancel the check task
        if (data.checkFuture != null) {
            data.checkFuture.cancel(false);
        }

        // Execute teleport
        executeTeleport(data.playerData, data.playerRef, data.store, data.currentWorld, data.home);
    }

    public void cancelWarmup(UUID playerId) {
        WarmupData data = activeWarmups.remove(playerId);
        if (data != null) {
            if (data.checkFuture != null) {
                data.checkFuture.cancel(false);
            }
            if (data.teleportFuture != null) {
                data.teleportFuture.cancel(false);
            }
        }
    }

    public boolean hasActiveWarmup(UUID playerId) {
        return activeWarmups.containsKey(playerId);
    }

    public void shutdown() {
        for (UUID playerId : activeWarmups.keySet()) {
            cancelWarmup(playerId);
        }
        scheduler.shutdown();
    }

    private void executeTeleport(PlayerRef playerData,
                                Ref<EntityStore> playerRef,
                                Store<EntityStore> store,
                                World currentWorld,
                                Home home) {
        if (!currentWorld.getName().equals(home.getWorld())) {
            playerData.sendMessage(Messages.worldNotFound(home.getWorld()));
            return;
        }

        // Execute teleport on world thread
        currentWorld.execute(() -> {
            try {
                Vector3d position = new Vector3d(home.getX(), home.getY(), home.getZ());
                Vector3f rotation = new Vector3f(home.getYaw(), home.getPitch(), 0);

                Teleport teleport = new Teleport(currentWorld, position, rotation);
                store.addComponent(playerRef, Teleport.getComponentType(), teleport);

                playerData.sendMessage(Messages.teleportedTo(home.getName()));
            } catch (Exception e) {
                playerData.sendMessage(Messages.worldNotFound(home.getWorld()));
            }
        });
    }

    private static class WarmupData {
        final PlayerRef playerData;
        final Ref<EntityStore> playerRef;
        final Store<EntityStore> store;
        final World currentWorld;
        final Home home;
        final double startX, startY, startZ;
        ScheduledFuture<?> checkFuture;
        ScheduledFuture<?> teleportFuture;

        WarmupData(PlayerRef playerData, Ref<EntityStore> playerRef, Store<EntityStore> store,
                  World currentWorld, Home home, double startX, double startY, double startZ) {
            this.playerData = playerData;
            this.playerRef = playerRef;
            this.store = store;
            this.currentWorld = currentWorld;
            this.home = home;
            this.startX = startX;
            this.startY = startY;
            this.startZ = startZ;
        }
    }
}
