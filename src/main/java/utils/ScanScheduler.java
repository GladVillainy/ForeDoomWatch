package utils;

import dao.SoftwareDAO;
import entities.Software;
import exceptions.ApiException;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScanScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ScanScheduler.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final SoftwareDAO softwareDAO;
    private final Service service;

    public ScanScheduler(SoftwareDAO softwareDAO, Service service) {
        this.softwareDAO = softwareDAO;
        this.service = service;
    }

    public void start() {
        int initialDelayInHours = 1;
        int delayInHours = 5;
        scheduler.scheduleWithFixedDelay(this::runScan, initialDelayInHours, delayInHours, TimeUnit.HOURS);
    }


    private void runScan() {
        int pauseBetweenScans = 6000; // millisekunder

        try {
            for (Software s : softwareDAO.readAll()) {
                try {
                    service.findVulnerabilities(s.getID());
                } catch (ApiException e) {
                    //NVP afviser
                    if (e.getCode() == HttpStatus.TOO_MANY_REQUESTS
                            || e.getCode() == HttpStatus.FORBIDDEN) {
                        logger.warn("NVD denied call with error message:" + e.getCode());
                        return;
                    }
                } catch (RuntimeException e) {
                    // Håndtere runtime så den ikke stopper loppet
                    logger.error("Scan failed", e);
                }
                //Delay pr kald
                Thread.sleep(pauseBetweenScans);
            }
        } catch (Exception e) {
            logger.error("Scan run failed", e);
        }
    }


    public void stop() {
        scheduler.shutdownNow();
    }
}
