package com.ems.bootstrap;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

public final class AppLauncher {
    private static final int DEFAULT_PORT = 8081;
    private static final String DEFAULT_CONTEXT_PATH = "/event-management-system";

    private AppLauncher() {
    }

    public static void main(String[] args) throws Exception {
        File projectDir = new File(System.getProperty("user.dir"));
        File webappDir = new File(projectDir, "src/main/webapp");
        File baseDir = new File(projectDir, ".embedded-tomcat");
        File compiledClassesDir = resolveCompiledClassesDir(projectDir);

        if (!webappDir.isDirectory()) {
            throw new IllegalStateException("Webapp folder not found: " + webappDir.getAbsolutePath());
        }
        if (!compiledClassesDir.isDirectory()) {
            throw new IllegalStateException("Compiled classes folder not found. Build the project once in IntelliJ first.");
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(resolvePort());
        tomcat.setBaseDir(baseDir.getAbsolutePath());

        Context context = tomcat.addWebapp(resolveContextPath(), webappDir.getAbsolutePath());
        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(
                resources,
                "/WEB-INF/classes",
                compiledClassesDir.getAbsolutePath(),
                "/"
        ));
        context.setResources(resources);

        tomcat.getConnector();
        tomcat.start();

        System.out.println("Event Management System started at http://localhost:" + resolvePort() + resolveContextPath() + "/login");
        tomcat.getServer().await();
    }

    private static int resolvePort() {
        String value = System.getenv("EMS_PORT");
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return DEFAULT_PORT;
        }
    }

    private static String resolveContextPath() {
        String value = System.getenv("EMS_CONTEXT_PATH");
        if (value == null || value.isBlank()) {
            return DEFAULT_CONTEXT_PATH;
        }

        String normalized = value.trim();
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private static File resolveCompiledClassesDir(File projectDir) {
        File ideaOutput = new File(projectDir, "out/production/classes");
        if (ideaOutput.isDirectory()) {
            return ideaOutput;
        }

        File ideaModuleOutput = new File(projectDir, "out/production/EventManagementSystem");
        if (ideaModuleOutput.isDirectory()) {
            return ideaModuleOutput;
        }

        File mavenOutput = new File(projectDir, "target/classes");
        if (mavenOutput.isDirectory()) {
            return mavenOutput;
        }

        File runtimeLocation;
        try {
            runtimeLocation = new File(AppLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception exception) {
            return mavenOutput;
        }
        return runtimeLocation;
    }
}
