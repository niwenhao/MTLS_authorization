package jp.co.nri.openapi.sample;


import org.apache.commons.cli.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.logging.Logger;

/**
 * Created by nwh on 17/07/18.
 */
public class MtlsClient {
    private String keyfile;
    private String password;
    private String url;
    private String store;
    private String storePassword;

    public String getKeyfile() {
        return keyfile;
    }

    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getStorePassword() {
        return storePassword;
    }

    public void setStorePassword(String storePassword) {
        this.storePassword = storePassword;
    }

    public static void main(String[] argv) {
        Options options = new Options();
        options.addOption("k", "clientkey", true, "Client key (.pfx) file path");
        options.addOption("p", "password", true, "Client key (.pfx) password");
        options.addOption("c", "certstore", true, "keystore file(.jsk) path");
        options.addOption("a", "storepasswork", true, "keystore file(.jks) password");
        options.addOption("u", "url", true, "Get URL");
        CommandLineParser parser = new DefaultParser();
        CommandLine cl;
        HelpFormatter help = new HelpFormatter();
        try {
            cl = parser.parse(options, argv);
        } catch(ParseException e) {
            Logger.getGlobal().warning(e.toString());
            help.printHelp("java -jar xxx.jar", options);
            System.exit(1);
            return;
        }

        MtlsClient main = new MtlsClient();

        if (cl.hasOption("k")) {
            main.setKeyfile(cl.getOptionValue("k"));
        } else {
            help.printHelp("java -jar xxx.jar", options);
            System.exit(2);
            return;
        }

        if (cl.hasOption("p")) {
            main.setPassword(cl.getOptionValue("p"));
        } else {
            help.printHelp("java -jar xxx.jar", options);
            System.exit(3);
            return;
        }

        if (cl.hasOption("u")) {
            main.setUrl(cl.getOptionValue("u"));
        } else {
            help.printHelp("java -jar xxx.jar", options);
            System.exit(4);
            return;
        }

        if (cl.hasOption("c")) {
            main.setStore(cl.getOptionValue("c"));
        } else {
            help.printHelp("java -jar xxx.jar", options);
            System.exit(5);
            return;
        }

        if (cl.hasOption("a")) {
            main.setStorePassword(cl.getOptionValue("a"));
        } else {
            help.printHelp("java -jar xxx.jar", options);
            System.exit(6);
            return;
        }

        Logger.getGlobal().info(main.doGet().toString());
    }

    private StringBuilder doGet() {
        KeyStore keyStore = null;
        FileInputStream keyStoreIS = null;


        try {
            keyStoreIS = new FileInputStream(this.getKeyfile());
            keyStore = KeyStore.getInstance("pkcs12");
            keyStore.load(keyStoreIS, this.getPassword().toCharArray());

            SSLContextBuilder sslCtxBld = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, this.getPassword().toCharArray());
            keyStoreIS.close();
            keyStoreIS = new FileInputStream(this.getStore());
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(keyStoreIS, this.getStorePassword().toCharArray());
            sslCtxBld.loadTrustMaterial(keyStore);
            HttpClient httpClient = HttpClients.custom()
                    .setSslcontext(sslCtxBld.build())
                    .build();

            HttpGet httpGet = new HttpGet(this.getUrl());

            HttpResponse response = httpClient.execute(httpGet);

            InputStream responseIS = response.getEntity().getContent();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = responseIS.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }

            responseIS.close();
            outputStream.flush();
            return new StringBuilder(outputStream.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
