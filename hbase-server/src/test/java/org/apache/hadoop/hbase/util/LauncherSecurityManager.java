package org.apache.hadoop.hbase.util;

import java.security.Permission;

public class LauncherSecurityManager extends SecurityManager {

    private static boolean exitInvoked;
    private static int exitCode;
    private SecurityManager securityManager;

    public LauncherSecurityManager() {
        reset();
        securityManager = System.getSecurityManager();
        System.setSecurityManager(this);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (securityManager != null) {
            // check everything with the original SecurityManager
            securityManager.checkPermission(perm, context);
        }
    }

    @Override
    public void checkPermission(Permission perm) {
        if (securityManager != null) {
            // check everything with the original SecurityManager
            securityManager.checkPermission(perm);
        }
    }

    @Override
    public void checkExit(int status) throws SecurityException {
        exitInvoked = true;
        exitCode = status;
        throw new SecurityException("Intercepted System.exit(" + status + ")");
    }

    public static boolean getExitInvoked() {
        return exitInvoked;
    }

    public static int getExitCode() {
        return exitCode;
    }

    public static void reset() {
        exitInvoked = false;
        exitCode = 0;
    }

}
