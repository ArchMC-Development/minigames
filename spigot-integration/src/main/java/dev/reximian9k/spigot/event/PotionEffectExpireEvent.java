package dev.reximian9k.spigot.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;

public class PotionEffectExpireEvent extends PotionEffectEvent implements Cancellable {

    private int duration;

    public PotionEffectExpireEvent(LivingEntity entity, PotionEffect effect) {
        super(entity, effect);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }

    @Override
    public boolean isCancelled() {
        return this.duration > 0;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.duration = cancel ? Integer.MAX_VALUE : 0;
    }

    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

}
