package org.bukkit.event.block.chest;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author GrowlyX
 * @since 8/26/2024
 */
public class EntityChestCloseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Entity entity;
    private ChestCloseSoundAction chestCloseSoundSetting = ChestCloseSoundAction.BROADCAST;
    private boolean shouldSendCloseAnimation = true;

    public EntityChestCloseEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    public ChestCloseSoundAction getChestCloseSoundSetting() {
        return chestCloseSoundSetting;
    }

    public void setChestCloseSoundSetting(ChestCloseSoundAction chestCloseSoundSetting) {
        this.chestCloseSoundSetting = chestCloseSoundSetting;
    }

    public boolean isShouldSendCloseAnimation() {
        return shouldSendCloseAnimation;
    }

    public void setShouldSendCloseAnimation(boolean shouldSendCloseAnimation) {
        this.shouldSendCloseAnimation = shouldSendCloseAnimation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
