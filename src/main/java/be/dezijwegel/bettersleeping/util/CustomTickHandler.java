package be.dezijwegel.bettersleeping.util;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class CustomTickHandler {

    private final World world;

    // Day/Night duration settings. Default duration will be used when <= 0
    private long previousTime;
    private double currentTime;
    private long dayDuration = 0;
    private long nightDuration = 0;
    private double dayRatio = 1;
    private double nightRatio = 1;
    private final List<Long> essentialTicks = new ArrayList<Long>(){{
        add(0L);
        add(11342L);
        add(12542L);
        add(23999L);
    }};

    public CustomTickHandler(World world)
    {
        this.world = world;

        this.currentTime = world.getTime();
        this.previousTime = world.getTime();
    }

    /**
     * Set the duration of days in this world (in minutes)
     *
     * @param duration how many minutes are in each minecraft day
     */
    public void setDayDuration(long duration)
    {

        if (duration > 0)
        {
            this.dayDuration = duration;
            this.dayRatio = WorldInfo.dayDuration / duration;
        }
        else
        {
            this.dayDuration = 0;
            this.dayRatio = 1;
        }
    }

    /**
     * Set the duration of nights in this world (in minutes)
     *
     * @param duration how many minutes are in each minecraft night
     */
    public void setNightDuration(long duration)
    {
        if (duration > 0)
        {
            this.nightDuration = duration;
            this.nightRatio = WorldInfo.nightDuration / duration;
        }
        else
        {
            this.nightDuration = 0;
            this.nightRatio = 1;
        }
    }

    public void handleCustomTick()
    {
        // Handle custom day/night time

        // Reset state if the time was altered
        final long lastTime = this.world.getTime() - 1;
        if (this.previousTime != lastTime)
        {
            this.previousTime = lastTime;
            this.currentTime = lastTime;
        }

        // Calculate the new time
        boolean isDay = WorldInfo.isDay( this.world );
        double timeStep = (isDay ? this.dayRatio : this.nightRatio);
        this.currentTime += timeStep;

//        // Make sure that no essential tick times are skipped
//        for (int i = 0; i < this.essentialTicks.size(); i++)
//        {
//            long ticks = this.essentialTicks.get( i );
//            if (this.previousTime < ticks && this.currentTime > ticks) {
//                this.currentTime = ticks;
//                break;
//            }
//        }
//
//        // Correct the time if there was an overshoot
        if (this.currentTime >= 24000)
            this.currentTime = 0;

        // Update the time
        long newTime = (long) Math.floor(this.currentTime);

        Bukkit.getLogger().info("----");
        Bukkit.getLogger().info( "" + timeStep );
        Bukkit.getLogger().info( "" + currentTime );
        Bukkit.getLogger().info( "" + newTime );

        if (newTime != world.getTime())
            this.world.setTime(newTime);
        this.previousTime = this.world.getTime();
    }

}
