import be.dezijwegel.bettersleeping.BetterSleeping;
import be.dezijwegel.bettersleeping.sleepersneeded.AbsoluteNeeded;
import be.dezijwegel.bettersleeping.sleepersneeded.PercentageNeeded;
import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
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
        int[] testCases = {4 , -4, 0, 8, 50};

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
//        PercentageNeeded percentageNeeded = new PercentageNeeded(percentage, );
//        if (calculator.getNumNeeded( world ) != calculator.getSetting())
//        {
//            fail();
//        }
    }

}
