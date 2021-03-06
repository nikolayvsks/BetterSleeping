package be.dezijwegel.bettersleeping.events.handlers;

import be.dezijwegel.bettersleeping.events.custom.TimeSetToDayEvent;
import be.dezijwegel.bettersleeping.messaging.ConsoleLogger;
import be.dezijwegel.bettersleeping.messaging.Messenger;
import be.dezijwegel.bettersleeping.messaging.MsgEntry;
import be.dezijwegel.bettersleeping.permissions.BypassChecker;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BuffsHandler implements Listener {


    private final ConsoleLogger logger;
    private final Messenger messenger;
    private final BypassChecker bypassChecker;

    private final Set<PotionEffect> sleepingBuffs;
    private final Set<PotionEffect> sleepingDebuffs;

    private final List<String> sleepingCommands;
    private final List<String> nonSleepingCommands;


    /**
     * Event handler for {@link TimeSetToDayEvent}
     * @param buffsConfig the config file containing all buffs and debuffs
     */
    public BuffsHandler(ConsoleLogger logger, Messenger messenger, BypassChecker bypassChecker, FileConfiguration buffsConfig)
    {
        this.logger = logger;
        this.messenger = messenger;
        this.bypassChecker = bypassChecker;

        this.sleepingBuffs   = readPotions(buffsConfig, "sleeper_buffs");
        this.sleepingDebuffs = readPotions(buffsConfig, "non_sleeper_debuffs");

        this.sleepingCommands    = buffsConfig.getStringList( "sleeper_commands" );
        this.nonSleepingCommands = buffsConfig.getStringList( "non_sleeper_commands" );
    }


    public Set<PotionEffect> getBuffs()
    {
        return sleepingBuffs;
    }

    public Set<PotionEffect> getDebuffs()
    {
        return sleepingDebuffs;
    }


    @EventHandler
    public void onSetToDay(TimeSetToDayEvent event)
    {
        // Only handle buffs if players (possibly) slept
        if (event.getCause() == TimeSetToDayEvent.Cause.OTHER)
            return;

        if (sleepingBuffs.size() > 0)
        {
            messenger.sendMessage(event.getPlayersWhoSlept(), "buff_received", false, new MsgEntry("<var>", "" + sleepingBuffs.size()));
            giveEffects(event.getPlayersWhoSlept(), sleepingBuffs, sleepingCommands);
        }

        if (sleepingDebuffs.size() > 0)
        {
            List<Player> nonSleepers = new ArrayList<>();
            for (Player player : event.getPlayersWhoDidNotSleep())
            {
                if ( ! bypassChecker.isPlayerBypassed( player ))
                    nonSleepers.add( player );
            }

            messenger.sendMessage(nonSleepers, "debuff_received", false, new MsgEntry("<var>", "" + sleepingDebuffs.size()));
            giveEffects(nonSleepers, sleepingDebuffs, nonSleepingCommands);
        }
    }


    private void giveEffects(List<Player> players, Set<PotionEffect> effects, List<String> commands)
    {
        for (Player player : players)
        {
            // Execute each command
            for (String command : commands)
            {
                command = command.replace("<user>", player.getName());
                Bukkit.getServer().dispatchCommand( Bukkit.getConsoleSender(), command );
            }

            // Add (de)buffs
            player.addPotionEffects(effects);
        }
    }


    /**
     * Read all {@link PotionEffect}s in a configuration section
     * @param config the config file to be read
     * @param section the section to be searched for the BetterSleeping potion effect format
     * @return A set of all valid potions in this section
     */
    private Set<PotionEffect> readPotions(FileConfiguration config, String section)
    {
        Set<PotionEffect> potions = new HashSet<PotionEffect>();

        // Prevent reading faulty config
        if ( ! config.isConfigurationSection(section) )
            return potions;

        // Only read effects when the config section exists
        ConfigurationSection configSection = config.getConfigurationSection(section);
        if (configSection != null)
        {
            for (String path : configSection.getKeys(false)) {
                int time = config.getInt(section + "." + path + ".time");
                int level = config.getInt(section + "." + path + ".level");

                PotionEffectType type = PotionEffectType.getByName(path.toUpperCase());


                // Only add if all fields are valid
                if (type != null && time > 0 && level > 0)
                    potions.add(new PotionEffect(type, 20 * time, level - 1));
                else
                    logger.log("Faulty (de)buff: '" + path + "' of duration '" + time + "' and level '" + level + "'");
            }
        }

        return potions;
    }
}
