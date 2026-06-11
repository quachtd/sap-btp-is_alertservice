package com.quachtd.btp.is.alertservice.job;

import com.quachtd.btp.is.alertservice.client.EventMeshClient;
import com.quachtd.btp.is.alertservice.model.MplFetchResult;
import com.quachtd.btp.is.alertservice.model.Part1Result;
import com.quachtd.btp.is.alertservice.model.Part2Result;
import com.quachtd.btp.is.alertservice.service.MplQueryService;
import com.quachtd.btp.is.alertservice.service.MplWatermarkService;
import com.quachtd.btp.is.alertservice.service.Part2TransformationService;
import com.quachtd.btp.is.alertservice.service.RecipientAggregationService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertJob {

    private static final Logger log = LoggerFactory.getLogger(AlertJob.class);

    private final MplQueryService mplQueryService;
    private final MplWatermarkService mplWatermarkService;
    private final RecipientAggregationService recipientAggregationService;
    private final Part2TransformationService part2TransformationService;
    private final EventMeshClient eventMeshClient;

    public AlertJob(
            MplQueryService mplQueryService,
            MplWatermarkService mplWatermarkService,
            RecipientAggregationService recipientAggregationService,
            Part2TransformationService part2TransformationService,
            EventMeshClient eventMeshClient) {
        this.mplQueryService = mplQueryService;
        this.mplWatermarkService = mplWatermarkService;
        this.recipientAggregationService = recipientAggregationService;
        this.part2TransformationService = part2TransformationService;
        this.eventMeshClient = eventMeshClient;
    }

    @Scheduled(cron = "${alert.schedule.cron}")
    public void processAlerts() {
        log.info("Starting scheduled alert processing");
        try {
            MplFetchResult mplFetchResult = mplQueryService.fetchMessages();
            Part1Result part1Result = recipientAggregationService.process(mplFetchResult.getMessages());
            log.info("Completed Part 1 with {} recipient mappings",
                    part1Result.getRecipientToErrorEvents().size());
            log.info("Pid to error event counts: {}", part1Result.getPidToErrorEventCounts());

            Part2Result part2Result = part2TransformationService.transform(part1Result);
            eventMeshClient.publish(part2Result);

            if (mplFetchResult.isWatermarkEligible()) {
                mplWatermarkService.save(mplFetchResult.getWindowEnd());
            }

            log.info("Completed scheduled alert processing with {} alert entries",
                    part2Result.getEntries().size());
        } catch (Exception ex) {
            log.error("Alert processing failed", ex);
        }
    }

    @PostConstruct
    public void runOnStartup() {
        log.info("Running alert processing on startup");
        processAlerts();
        log.info("Completed alert processing on startup");
    }
}
