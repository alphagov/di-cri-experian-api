package uk.gov.di.ipv.cri.experian.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class SSLContextFactoryTest {

    private SSLContextFactory sslContextFactory;

    @BeforeEach
    void setUp() {
        this.sslContextFactory = new SSLContextFactory();
    }

    @Test
    void shouldThrowNullPointerExceptionWhenInvalidKeyStorePathAndPasswordProvided() {
        NullPointerException keyStorePathNullRefException =
                assertThrows(
                        NullPointerException.class,
                        () -> this.sslContextFactory.getSSLContext(null, null));
        assertEquals("keyStorePath must not be null", keyStorePathNullRefException.getMessage());

        NullPointerException keyStorePasswordNullRefException =
                assertThrows(
                        NullPointerException.class,
                        () -> this.sslContextFactory.getSSLContext("path-to-keystore", null));
        assertEquals(
                "keyStorePassword must not be null", keyStorePasswordNullRefException.getMessage());
    }

    @Test
    void shouldThrowArgumentExceptionWhenBlankOrEmptyPathAndPasswordProvided() {

        String[] testCases = new String[] {"  ", ""};

        Arrays.stream(testCases)
                .forEach(
                        (testCase) -> {
                            IllegalArgumentException keyStorePathException =
                                    assertThrows(
                                            IllegalArgumentException.class,
                                            () ->
                                                    this.sslContextFactory.getSSLContext(
                                                            testCase, "password"));
                            assertEquals(
                                    "keyStorePath must not be blank or empty",
                                    keyStorePathException.getMessage());

                            IllegalArgumentException keyStorePasswordException =
                                    assertThrows(
                                            IllegalArgumentException.class,
                                            () ->
                                                    this.sslContextFactory.getSSLContext(
                                                            "path-to-keystore", testCase));
                            assertEquals(
                                    "keyStorePassword must not be blank or empty",
                                    keyStorePasswordException.getMessage());
                        });
    }
}
