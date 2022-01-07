package uk.gov.di.ipv.cri.experian;

public class App {
    public static void main(String[] args) {
        System.setProperty(
                "javax.net.ssl.keyStore", System.getenv("EXPERIAN_CROSS_CORE_API_KEYSTORE_PATH"));
        System.setProperty("javax.net.ssl.keyStoreType", "pkcs12");
        System.setProperty(
                "javax.net.ssl.keyStorePassword",
                System.getenv("EXPERIAN_CROSS_CORE_API_KEYSTORE_PASSWORD"));

        new ExperianApi();
    }
}
