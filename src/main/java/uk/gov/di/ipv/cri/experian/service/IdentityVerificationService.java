package uk.gov.di.ipv.cri.experian.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.gateway.ExperianGateway;

public class IdentityVerificationService {
    private final ExperianGateway experianGateway;
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityVerificationService.class);

    IdentityVerificationService(ExperianGateway experianGateway) {
        this.experianGateway = experianGateway;
    }

    public String verifyIdentity(PersonIdentity personIdentity) {
        try {
            return experianGateway.performIdentityCheck(personIdentity);
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
