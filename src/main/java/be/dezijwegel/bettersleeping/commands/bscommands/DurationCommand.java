package be.dezijwegel.bettersleeping.commands.bscommands;

import be.dezijwegel.bettersleeping.messaging.Messenger;
import be.dezijwegel.bettersleeping.messaging.MsgEntry;
import be.dezijwegel.bettersleeping.runnables.SleepersRunnable;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class DurationCommand extends BsCommand {

    final Map<String, SleepersRunnable> worldRunnables;

    public DurationCommand(Messenger messenger, Map<String, SleepersRunnable> worldRunnables)
    {
        super(messenger);
        this.worldRunnables = worldRunnables;
    }

    @Override
    public boolean execute(CommandSender commandSender, Command command, String alias, String[] arguments) {

        if ( ! commandSender.hasPermission( getPermission() ))
        {
            messenger.sendMessage(commandSender, "no_permission", true, new MsgEntry("<var>", "/bs " + arguments[0]));
            return true;
        }

        if ( ! (commandSender instanceof Player) )
            return false;

        Player player = (Player) commandSender;

        if (arguments.length != 3)
            return false;

        if (arguments[1].equalsIgnoreCase("day") || arguments[1].equalsIgnoreCase("night"))
        {

            boolean doChangeDayDuration = arguments[1].equalsIgnoreCase("day");
            int durationInMinutes = Integer.parseInt( arguments[2] );

            if (durationInMinutes < 0)
                return false;

            String worldName = player.getWorld().getName();
            if ( ! this.worldRunnables.containsKey( worldName ))
                return false;

            SleepersRunnable runnable = this.worldRunnables.get( worldName );

            if (doChangeDayDuration)
                runnable.setDayDuration( durationInMinutes );
            else
                runnable.setNightDuration( durationInMinutes );

            return true;
        }
        else return false;
    }

    @Override
    public String getPermission() {
        return "bettersleeping.duration";
    }

    @Override
    public String getDescription() {
        return "Change the duration of the day or night in your world. Used as /bs3 duration [day/night] [duration in minutes]. For example: /bs3 duration night 20";
    }
}
