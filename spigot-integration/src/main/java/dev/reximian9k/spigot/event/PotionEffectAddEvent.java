package dev.reximian9k.spigot.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;

public class PotionEffectAddEvent extends PotionEffectEvent implements Cancellable {

    private final Cause cause;

    private boolean cancelled;

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public PotionEffectAddEvent(LivingEntity target, PotionEffect effect, Cause cause) {
        super(target, effect);
        this.cause = cause;
    }

    public Cause getCause() {
        return cause;
    }

    public enum Cause {
        POTION_CONSUME,
        POTION_SPLASH,
        BEACON,
        WITHER_SKELETON,
        WITHER_SKULL,
        PUFFER_FISH,
        GOLDEN_APPLE,
        SUPER_GOLDEN_APPLE,
        PLUGIN,
        COMMAND,
        UNKNOWN
    }

    private static final HandlerList handlers = new HandlerList();
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }

}
