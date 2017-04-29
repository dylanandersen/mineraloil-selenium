package com.lithium.mineraloil.selenium.elements;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.UnreachableBrowserException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Slf4j
public class Screenshot {

    private static final String screenShotDirectory = getDirectory("screenshots");
    private static final String htmlScreenShotDirectory = getDirectory("html-screenshots");
    private static final String consoleLogDirectory = getDirectory("console-logs");

    public static void takeScreenshot(String filename) {
        if (log.isDebugEnabled()) {
            takeFullDesktopScreenshot(filename);
        } else {
            if (DriverManager.INSTANCE.isDriverStarted()) {
                try {
                    filename +=  "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + ".png";
                    File scrFile = DriverManager.INSTANCE.takeScreenshot();
                    log.info("Creating Screenshot: " + screenShotDirectory + filename);
                    FileUtils.copyFile(scrFile, new File(screenShotDirectory + filename));
                } catch (IOException | UnreachableBrowserException e) {
                    log.error(" Unable to take screenshot: " + e.toString());
                }
            } else {
                log.error("Webdriver not started. Unable to take screenshot");
            }
        }
    }

    public static void takeFullDesktopScreenshot(String filename) {
        try {
            filename +=  "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + ".png";
            BufferedImage img = getScreenAsBufferedImage();
            File output = new File(filename);
            ImageIO.write(img, "png", output);
            log.info("Creating FULL SCREEN Screenshot: " + screenShotDirectory + filename);
            FileUtils.copyFile(output, new File(screenShotDirectory + filename));
            FileUtils.deleteQuietly(output);
        } catch (IOException e) {
            log.error(e.toString());
        }
    }

    public static void takeHTMLScreenshot(String filename) {
        if (!DriverManager.INSTANCE.isDriverStarted()) {
            log.error("Webdriver not started. Unable to take html snapshot");
            return;
        }

        filename +=  "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + ".html";

        Writer writer = null;
        log.info("Capturing HTML snapshot: " + htmlScreenShotDirectory + filename);

        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(htmlScreenShotDirectory + filename), "utf-8"));
            writer.write(DriverManager.INSTANCE.getHtml());
        } catch (IOException ex) {
            log.info("Unable to write out current state of html");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                log.info("Unable to close writer");
            }
        }
    }

    public static void saveConsoleLog(String filename){
        if (!DriverManager.INSTANCE.isDriverStarted()){
            log.error("Webdriver not started. Unable to save log.");
            return;
        }

        filename +=  "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + "_" + "browser_console" + ".log";

        Writer writer = null;
        log.info("Capturing Console Log snapshot: " + consoleLogDirectory + filename);

        try {
            writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(consoleLogDirectory + filename), "utf-8"));
            writer.write(DriverManager.INSTANCE.getConsoleLog()
                                               .filter(Level.ALL)
                                               .stream()
                                               .map(logEntry -> logEntry.getLevel().toString()
                                                                        .concat(": ")
                                                                        .concat(logEntry.getMessage())
                                                                        .concat("\n"))
                                               .collect(Collectors.joining()));
        } catch (IOException ex) {
            log.info("Unable to write out current console log");
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                log.info("Unable to close writer");
            }
        }
    }

    private static BufferedImage getScreenAsBufferedImage() {
        BufferedImage img = null;
        try {
            Robot r;
            r = new Robot();
            Toolkit t = Toolkit.getDefaultToolkit();
            Rectangle rect = new Rectangle(t.getScreenSize());
            img = r.createScreenCapture(rect);
        } catch (AWTException e) {
            log.error(e.toString());
        }
        return img;
    }

    private static String getDirectory(String name) {
        String screenshotDirectory = String.format("%s../%s/", ClassLoader.getSystemClassLoader().getSystemResource("").getPath(),name);
        File file = new File(screenshotDirectory);
        if (!file.exists()) file.mkdir();
        log.info("Creating screenshot directory: " + screenshotDirectory);
        return screenshotDirectory;
    }

}
