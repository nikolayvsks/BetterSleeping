package be.dezijwegel.events;

import be.dezijwegel.bettersleeping.BetterSleeping;
import be.dezijwegel.bettersleeping.Management;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class SleepTracker {

    private Map<UUID, Long> sleepList;
    private Map<World, Integer> numSleeping;
    private Map<World, Long> lastSetToDay;

    private Management management;

    private boolean multiworld;
    private int bedEnterDelay;
    private int percentageNeeded;

    public SleepTracker(Management management)
    {
        sleepList = new HashMap<UUID, Long>();
        numSleeping = new HashMap<World, Integer>();
        lastSetToDay = new HashMap<World, Long>();

        this.management = management;

        this.multiworld = management.getBooleanSetting("multiworld_support");
        this.bedEnterDelay = management.getIntegerSetting("bed_enter_delay");
        this.percentageNeeded = management.getIntegerSetting("percentage_needed");
    }

    /**
     * Get how many seconds ago the time was set to day
     * @param world
     * @return
     */
    public int getTimeSinceLastSetToDay(World world)
    {
        if (lastSetToDay.containsKey(world)) {
            return (int) ( System.currentTimeMillis() / 1000 - lastSetToDay.get(world) );
        } else return 3600;
    }

    /**
     * Add the current time to the list of lastSetToDay
     * @param world
     */
    public void worldWasSetToDay(World world)
    {
        lastSetToDay.put(world, System.currentTimeMillis()/1000);
    }

    /**
     * Check whether or not a Player (by uuid) must wait a while before they can sleep (again)
     * @param uuid
     * @return
     */
    public boolean checkPlayerSleepDelay (UUID uuid)
    {
        long currentTime = System.currentTimeMillis() / 1000L;
        if (sleepList.containsKey(uuid))
        {
            if (whenCanPlayerSleep(uuid) < 1)
            {
                sleepList.put(uuid, currentTime);
                return true;
            } else {
                return false;
            }
        } else {
            sleepList.put(uuid, currentTime);
            return true;
        }
    }

    /**
     * Gets the time (seconds) a Player must wait before they can sleep again
     * @param uuid
     * @return
     */
    public long whenCanPlayerSleep(UUID uuid)
    {
        if (sleepList.containsKey(uuid))
        {
            long currentTime = System.currentTimeMillis() / 1000L;
            if(bedEnterDelay < ( currentTime - sleepList.get(uuid) ) )
                return bedEnterDelay - (currentTime - sleepList.get(uuid));
            else return 0L;

        } else {
            return 0L;
        }
    }

    /**
     * Get the number of players that should be sleeping for the night to be skipped
     * This method also considers the 'multiworld_support' setting in config.yml
     * @param world
     * @return
     */
    public int getTotalSleepersNeeded(World world)
    {
        int numPlayers = 0;
        if (multiworld)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                if (player.getLocation().getWorld().equals(world))
                    numPlayers++;
            }
        }
        else
        {
            numPlayers = Bukkit.getOnlinePlayers().size();
        }

        return (int) Math.ceil((double)(percentageNeeded * numPlayers) / 100);
    }

    /**
     * Get the number of relevant players that are currently sleeping
     * The 'multiworld_support' option will be considered
     * @param world
     * @return
     */
    public int getNumSleepingPlayers(World world)
    {
        int numSleeping;
        if (multiworld)
        {
            numSleeping = this.numSleeping.get(world);
        }
        else
        {
            numSleeping = 0;
            for (Map.Entry<World, Integer> entry : this.numSleeping.entrySet())
            {
                numSleeping += entry.getValue();
            }
        }

        return numSleeping;
    }

    /**
     * Add a player to the sleeping list
     * @param world
     */
    public void addSleepingPlayer(World world)
    {
        if (numSleeping.containsKey(world))
        {
            int num = numSleeping.get(world) + 1;
            numSleeping.put(world, num);
        } else
        {
            numSleeping.put(world, 1);
        }
    }

    /**
     * Remove a player from the sleeping list
     * @param world
     */
    public void removeSleepingPlayer(World world)
    {
        if (numSleeping.containsKey(world))
        {
            int num = numSleeping.get(world) -1;
            numSleeping.put(world, num);
        } else
        {
            numSleeping.put(world, 0);
        }
    }

    /**
     * Get a list of players that are relevant to the given world
     * Does take 'multiworld_support' into account
     * @param world
     * @return
     */
    public List<Player> getRelevantPlayers(World world)
    {
        List<Player> list = new LinkedList<Player>();
        if (multiworld)
        {
            for (Player player : Bukkit.getOnlinePlayers())
                if (player.getLocation().getWorld().equals(world)) list.add(player);
        }
        else
        {
            for (Player player : Bukkit.getOnlinePlayers())
                list.add(player);
        }

        if (BetterSleeping.debug)
        {
            System.out.println("-----");
            System.out.println("Getting relevant players...");
            System.out.println("Multiworld: " + multiworld + " Num found: " + list.size());
            System.out.println("World: \"" + world.getName() + "\"");
            int i = 0;
            for (Player p : list)
            {
                i++;
                if (i > 10) System.out.println("");
                System.out.print(p.getName() + "(" + p.getWorld().getName() + ") ");
            }
            System.out.println("-----");
        }

        return list;
    }

    /**
     * Check if a Player meets the requirements to sleep
     * Also checks if the World meets the requirements
     * Sends messages to players if needed
     * @return
     */
    public boolean playerMaySleep(Player player)
    {
        World worldObj = player.getWorld();
        if (worldObj.getTime() > 12500 || worldObj.hasStorm() || worldObj.isThundering()) {

            UUID uuid = player.getUniqueId();

            if (checkPlayerSleepDelay(uuid))
                return true;
            else {
                Map<String, String> replace = new LinkedHashMap<String, String>();
                replace.put("<time>", Long.toString(whenCanPlayerSleep(player.getUniqueId())));
                management.sendMessage("sleep_spam", player, replace);

            }
        }
        return false;
    }
}
