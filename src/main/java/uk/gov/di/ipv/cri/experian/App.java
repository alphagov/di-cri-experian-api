package uk.gov.di.ipv.cri.experian;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class App {
    public static void main(String[] args) throws IOException {
        String keystoreBase64 = System.getenv("KEYSTORE");
        Path tempFile = Files.createTempFile(null, null);
        Files.write(tempFile, Base64.getDecoder().decode(keystoreBase64));
        System.setProperty("javax.net.ssl.keyStore", tempFile.toString());

        System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
        System.setProperty("javax.net.ssl.keyStorePassword", System.getenv("KEYSTORE_PASSWORD"));

        new FraudApi();
    }
}
