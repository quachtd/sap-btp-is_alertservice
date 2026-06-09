package com.btp.is.alertservice.service;

import com.btp.is.alertservice.client.AlternativePartnerClient;
import com.btp.is.alertservice.model.MessageProcessingLog;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class PidLookupService {

    private final AlternativePartnerClient alternativePartnerClient;
    private static final Logger log = LoggerFactory.getLogger(PidLookupService.class);

    public PidLookupService(AlternativePartnerClient alternativePartnerClient) {
        this.alternativePartnerClient = alternativePartnerClient;
    }

    public Optional<String> findPid(MessageProcessingLog message) {
        String pid = alternativePartnerClient.findPid(message.getSender(), message.getSenderInterface());
        log.debug("sender: {}, senderInterface: {}, pid: {}", message.getSender(), message.getSenderInterface(), pid);
        return Optional.ofNullable(pid);
    }
}
