package be.dezijwegel.bettersleeping.util;

import org.bukkit.World;

public class WorldInfo {

    // Ticks info
    private static final long dayStart = 0;
    private static final long dayEnd = 13000;

    // Real time info
    public static final double dayDuration   = 10.833333;
    public static final double nightDuration =  9.166666;

    /**
     * Check whether or not it is day in this world
     *
     * @param world the world to be checked
     * @return true if it is day
     */
    public static boolean isDay( World world )
    {
        return world.getTime() >= dayStart && world.getTime() < dayEnd;
    }

}
