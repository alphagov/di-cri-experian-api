package uk.gov.di.ipv.cri.experian.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.gateway.crosscore.CrossCoreGateway;

public class IdentityVerificationService {
    private final CrossCoreGateway crossCoreGateway;
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityVerificationService.class);

    public IdentityVerificationService(CrossCoreGateway crossCoreGateway) {
        this.crossCoreGateway = crossCoreGateway;
    }

    public String verifyIdentity(PersonIdentity personIdentity) {
        try {
            return crossCoreGateway.performIdentityCheck(personIdentity);
        } catch (InterruptedException ie) {
            LOGGER.error("Error occurred when attempting to invoke experian api", ie);
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            LOGGER.error("Error occurred when attempting to invoke experian api", e);
            return null;
        }
    }
}
