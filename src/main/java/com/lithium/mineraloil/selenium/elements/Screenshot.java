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

@Slf4j
public class Screenshot {
    private final String screenShotDirectory = getDirectory("screenshots");
    private final String htmlScreenShotDirectory = getDirectory("html-screenshots");
    private final Driver driver;

    public Screenshot(Driver driver) {
        this.driver = driver;
    }

    public void takeScreenshot(String filename) {
        if (log.isDebugEnabled()) {
            takeFullDesktopScreenshot(filename);
        } else {
            if (driver.isDriverStarted()) {
                try {
                    filename +=  "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId() + ".png";
                    File scrFile = driver.takeScreenshot();
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

    public void takeFullDesktopScreenshot(String filename) {
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

    public void takeHTMLScreenshot(String filename) {
        if (!driver.isDriverStarted()) {
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
            writer.write(driver.getHtml());
        } catch (IOException ex) {
            log.info("Unable to write out current state of html");
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                log.info("Unable to close writer");
            }
        }
    }

    private BufferedImage getScreenAsBufferedImage() {
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

    private String getDirectory(String name) {
        String screenshotDirectory = String.format("%s../%s/", ClassLoader.getSystemClassLoader().getSystemResource("").getPath(),name);
        File file = new File(screenshotDirectory);
        if (!file.exists()) file.mkdir();
        log.info("Creating screenshot directory: " + screenshotDirectory);
        return screenshotDirectory;
    }

}
