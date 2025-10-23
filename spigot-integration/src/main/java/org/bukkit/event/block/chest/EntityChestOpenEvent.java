package org.bukkit.event.block.chest;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author GrowlyX
 * @since 8/26/2024
 */
public class EntityChestOpenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Entity entity;

    public EntityChestOpenEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
