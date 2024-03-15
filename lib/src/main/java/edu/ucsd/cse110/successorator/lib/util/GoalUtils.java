package edu.ucsd.cse110.successorator.lib.util;

import edu.ucsd.cse110.successorator.lib.domain.Context;
import edu.ucsd.cse110.successorator.lib.domain.Goal;

public class GoalUtils {

    public static boolean shouldShowContext(Goal goal, Context context) {
        return goal.context() == context || context == Context.NONE;
    }
}
