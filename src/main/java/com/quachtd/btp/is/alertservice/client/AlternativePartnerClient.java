package com.quachtd.btp.is.alertservice.client;

import com.quachtd.btp.is.alertservice.config.CpiProperties;
import com.quachtd.btp.is.alertservice.model.odata.AlternativePartnerResponse;
import com.quachtd.btp.is.alertservice.util.HexEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Component
public class AlternativePartnerClient {

    private final RestClient restClient;
    private final CpiProperties properties;
    private static final Logger log = LoggerFactory.getLogger(AlternativePartnerClient.class);

    public AlternativePartnerClient(RestClient cpiRestClient, CpiProperties properties) {
        this.restClient = cpiRestClient;
        this.properties = properties;
    }

    public String findPid(String sender, String senderInterface) {
        String hexAgency = HexEncoder.encode(sender);
        String hexId = HexEncoder.encode(senderInterface);
        String schemeHex = properties.getSenderInterfaceSchemeHex();

        log.debug("Hexagency: {}, Hexscheme: {}, Hexid: {}", hexAgency, schemeHex, hexId);

        String uri = String.format(
                "/api/v1/AlternativePartners(Hexagency='%s',Hexscheme='%s',Hexid='%s')?$select=Pid",
                hexAgency, schemeHex, hexId);

        try {
            AlternativePartnerResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(AlternativePartnerResponse.class);
            return response != null ? response.getPid() : null;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return null;
            }
            throw ex;
        }
    }
}
