package uk.gov.di.ipv.cri.experian.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import java.io.File;
import java.security.KeyStore;
import java.util.Objects;

class SSLContextFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSLContextFactory.class);
    private static final String DEFAULT_SSL_CONTEXT_PROTOCOL = "TLSv1.2";
    private static final String DEFAULT_KEYSTORE_TYPE = "pkcs12";
    private final String keyStorePath;
    private final String keyStorePassword;

    SSLContextFactory(String keyStorePath, String keyStorePassword) {
        Objects.requireNonNull(keyStorePath, "keyStorePath must not be null");
        Objects.requireNonNull(keyStorePassword, "keyStorePassword must not be null");

        if (keyStorePath.isBlank() || keyStorePath.isEmpty()) {
            throw new IllegalArgumentException("keyStorePath must not be blank or empty");
        }
        if (keyStorePassword.isBlank() || keyStorePassword.isEmpty()) {
            throw new IllegalArgumentException("keyStorePassword must not be blank or empty");
        }

        this.keyStorePath = keyStorePath;
        this.keyStorePassword = keyStorePassword;
    }

    SSLContext getSSLContext() {
        try {
            KeyStore keyStore =
                    KeyStore.Builder.newInstance(
                                    DEFAULT_KEYSTORE_TYPE,
                                    null,
                                    new File(keyStorePath),
                                    new KeyStore.PasswordProtection(keyStorePassword.toCharArray()))
                            .getKeyStore();

            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            SSLContext sslContext = SSLContext.getInstance(DEFAULT_SSL_CONTEXT_PROTOCOL);
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            return sslContext;
        } catch (Exception e) {
            LOGGER.error(
                    "An error occurred when attempting to initialise an SSLContext with the given keystore details",
                    e);
            return null;
        }
    }
}
