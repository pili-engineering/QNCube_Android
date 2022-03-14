package com.hapi.ut;

/**
 * Created by xx on 8/17/16.
 */

public final class FlagUtils {
    private FlagUtils() {
        // DISABLED.
    }


    /**
     * Test whether a flag is set or not.
     * @param value The set of flags represent as a int value.
     * @param flag The flag to be test.
     * @return true indicate the flag is set.
     */
    public static boolean hasFlag(int value, int flag) {
        // [Solution 0]
        //  0 != (flag & value)
        //  failed to work while the flag is 0.
        //
        // [Solution 1]
        //  (flag & value) == flag
        //  work on the case while the flag is 0,
        //  but error prone to confuse the order of the arguments.
        //
        return (flag & value) == flag;
    }

    /**
     * Test whether a flag is set or not.
     * @param value The set of flags represent as a long value.
     * @param flag The flag to be test.
     * @return true indicate the flag is set.
     */
    public static boolean hasFlag(long value, long flag) {
        // [Solution 0]
        //  0 != (flag & value)
        //  failed to work while the flag is 0.
        //
        // [Solution 1]
        //  (flag & value) == flag
        //  work on the case while the flag is 0,
        //  but error prone to confuse the order of the arguments.
        //
        return (flag & value) == flag;
    }
}
