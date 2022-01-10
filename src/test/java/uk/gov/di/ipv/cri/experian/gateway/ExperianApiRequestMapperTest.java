package uk.gov.di.ipv.cri.experian.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.ipv.cri.experian.domain.AddressType;
import uk.gov.di.ipv.cri.experian.domain.PersonIdentity;
import uk.gov.di.ipv.cri.experian.gateway.dto.CrossCoreApiRequest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.di.ipv.cri.experian.util.TestDataCreator.createTestPersonIdentity;

class ExperianApiRequestMapperTest {

    private static final String TENANT_ID = "tenant-id";
    private ExperianApiRequestMapper experianApiRequestMapper;
    private PersonIdentity personIdentity;

    @BeforeEach
    void setup() {
        experianApiRequestMapper = new ExperianApiRequestMapper(TENANT_ID);
    }

    @Test
    void shouldConvertPersonIdentityToCrossCoreApiRequestForCurrentAddress() {
        personIdentity = createTestPersonIdentity(AddressType.CURRENT);

        CrossCoreApiRequest result = experianApiRequestMapper.mapPersonIdentity(personIdentity);

        assertNotNull(result);
        assertEquals(
                LocalDate.of(1976, 12, 26).toString(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getPersonDetails()
                        .getDateOfBirth());
        assertEquals(
                personIdentity.getFirstName(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getNames()
                        .get(0)
                        .getFirstName());
        assertEquals(
                personIdentity.getSurname(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getNames()
                        .get(0)
                        .getSurName());
        assertEquals(
                AddressType.CURRENT.toString(),
                result.getPayload().getContacts().get(0).getAddresses().get(0).getAddressType());
        assertEquals(
                "PostTown",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getPostTown());
        assertEquals(
                "Street Name",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getStreet());
        assertEquals(
                "Postcode",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getPostal());
    }

    @Test
    void shouldConvertPersonIdentityToCrossCoreApiRequestForPreviousAddress() {
        personIdentity = createTestPersonIdentity(AddressType.PREVIOUS);

        CrossCoreApiRequest result = experianApiRequestMapper.mapPersonIdentity(personIdentity);

        assertNotNull(result);
        assertEquals(
                LocalDate.of(1976, 12, 26).toString(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getPersonDetails()
                        .getDateOfBirth());
        assertEquals(
                personIdentity.getFirstName(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getNames()
                        .get(0)
                        .getFirstName());
        assertEquals(
                personIdentity.getSurname(),
                result.getPayload()
                        .getContacts()
                        .get(0)
                        .getPerson()
                        .getNames()
                        .get(0)
                        .getSurName());
        assertEquals(
                AddressType.PREVIOUS.toString(),
                result.getPayload().getContacts().get(0).getAddresses().get(0).getAddressType());
        assertEquals(
                "PostTown",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getPostTown());
        assertEquals(
                "Street Name",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getStreet());
        assertEquals(
                "Postcode",
                result.getPayload().getContacts().get(0).getAddresses().get(0).getPostal());
    }

    @Test
    void shouldThrowExceptionWhenPersonIdentityIsNull() {
        NullPointerException exception =
                assertThrows(
                        NullPointerException.class,
                        () -> experianApiRequestMapper.mapPersonIdentity(personIdentity));
        assertEquals("The personIdentity must not be null", exception.getMessage());
    }
}
