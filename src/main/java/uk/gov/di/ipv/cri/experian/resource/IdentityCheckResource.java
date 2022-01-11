package uk.gov.di.ipv.cri.experian.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;
import spark.Response;
import spark.Route;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.domain.ValidationResult;
import uk.gov.di.ipv.cri.experian.service.IdentityVerificationService;
import uk.gov.di.ipv.cri.experian.validation.InputValidationExecutor;

import javax.servlet.http.HttpServletResponse;

public class IdentityCheckResource {
    private ObjectMapper objectMapper;
    private IdentityVerificationService identityVerificationService;
    private InputValidationExecutor inputValidationExecutor;

    public IdentityCheckResource(
            IdentityVerificationService identityVerificationService,
            ObjectMapper objectMapper,
            InputValidationExecutor inputValidationExecutor) {
        this.identityVerificationService = identityVerificationService;
        this.objectMapper = objectMapper;
        this.inputValidationExecutor = inputValidationExecutor;
    }

    public final Route performIdentityCheckRoute =
            (Request request, Response response) -> {
                PersonIdentity personIdentity =
                        objectMapper.readValue(request.body(), PersonIdentity.class);

                ValidationResult validationResult =
                        this.inputValidationExecutor.performInputValidation(personIdentity);

                String responseBody;
                int responseStatusCode;

                if (validationResult.isValid()) {
                    responseStatusCode = HttpServletResponse.SC_CREATED;
                    responseBody = this.identityVerificationService.verifyIdentity(personIdentity);
                } else {
                    responseStatusCode = HttpServletResponse.SC_BAD_REQUEST;
                    responseBody = objectMapper.writeValueAsString(validationResult);
                }

                response.header("Content-Type", "application/json");
                response.status(responseStatusCode);
                response.body(responseBody);
                return response.body();
            };
}
