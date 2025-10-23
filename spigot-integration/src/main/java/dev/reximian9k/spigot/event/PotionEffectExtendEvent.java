package dev.reximian9k.spigot.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;

public class PotionEffectExtendEvent extends PotionEffectAddEvent implements Cancellable {

    private final PotionEffect oldEffect;
    private int duration;

    public PotionEffectExtendEvent(LivingEntity target, PotionEffect effect, PotionEffect oldEffect, Cause cause) {
        super(target, effect, cause);
        this.oldEffect = oldEffect;
    }

    public PotionEffect getOldEffect() {
        return oldEffect;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }

    @Override
    public boolean isCancelled() {
        return duration > 0;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.duration = cancelled ? Integer.MAX_VALUE : 0;
    }

    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

}
