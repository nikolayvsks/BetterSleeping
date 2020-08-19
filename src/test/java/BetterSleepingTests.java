import be.dezijwegel.bettersleeping.BetterSleeping;
import be.dezijwegel.bettersleeping.hooks.EssentialsHook;
import be.dezijwegel.bettersleeping.permissions.BypassChecker;
import be.dezijwegel.bettersleeping.sleepersneeded.AbsoluteNeeded;
import be.dezijwegel.bettersleeping.sleepersneeded.PercentageNeeded;
import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import org.bukkit.GameMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class BetterSleepingTests {

    private ServerMock server;
    private WorldMock world;
//    private BetterSleeping plugin;

    @Before
    public void setup()
    {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }

    @After
    public void cleanUp()
    {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Absolute needed calculation test")
    public void testAbsoluteNeeded()
    {
        int[] testCases = {-4 , 0, 1, 2, 50};

        for (int numNeeded : testCases)
        {
            numNeeded = Math.abs( numNeeded );

            AbsoluteNeeded calculator = new AbsoluteNeeded(numNeeded);
            if ( numNeeded != calculator.getSetting())
            {
                fail("The saved setting does not match the setting (" + numNeeded + ")");
            }
            else if (calculator.getNumNeeded(world) != numNeeded)
            {
                fail("The required amount of sleepers does not match the set amount (" + numNeeded + ")");
            }
        }
    }

    @Test
    @DisplayName("Percentage needed calculation test")
    public void testPercentageNeeded()
    {
        int[] percentages = {-10, 0, 1, 30, 50, 100, 150};  // Percentages to be tested
        int[] numPlayers = {0, 1, 2, 5, 10};    // Amount of players to be tested for each percentage. MUST be in ascending order!

        for (int players : numPlayers)
        {
            while(world.getPlayers().size() < players)
                server.addPlayer();

            for (int percentage : percentages)
            {
                PercentageNeeded calculator = new PercentageNeeded(
                    percentage,
                    new BypassChecker(
                        false,
                        new EssentialsHook(true, true, 0),
                        new ArrayList<GameMode>()
                    )
                );

                // Constrain percentage to [0, 100]
                percentage = Math.max(percentage, 0);
                percentage = Math.min(percentage, 100);

                System.out.println( server.getOnlinePlayers().size() + " - " + world.getPlayers().size() );

                int setting = calculator.getSetting();
                if (setting != percentage)
                {
                    fail("The set percentage (" + setting + ") is incorrect and should be (" + percentage + ")");
                }

//                if (calculator.getNumNeeded( world ) != calculator.getSetting())
//                {
//                    fail();
//                }
            }
        }

    }

}
