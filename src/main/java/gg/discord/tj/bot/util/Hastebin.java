package gg.discord.tj.bot.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class Hastebin
{
    public static CompletableFuture<String> paste(String server, String text, boolean raw) {
        CompletableFuture<String> result = new CompletableFuture<>();

        new Thread(() -> {
            try {
                byte[] postData = text.getBytes(StandardCharsets.UTF_8);
                int postDataLength = postData.length;
                String requestURL = server + "/documents";
                URL url = new URL(requestURL);
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

                con.setDoOutput(true);
                con.setInstanceFollowRedirects(false);
                con.setRequestMethod("POST");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                con.setRequestProperty("Content-Length", String.valueOf(postDataLength));
                con.setUseCaches(false);

                try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                    wr.write(postData);
                }

                String response;

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    response = reader.readLine();
                }

                if (response != null && response.contains("\"key\"")) {
                    response = response.substring(response.indexOf(":") + 2, response.length() - 2);

                    String postURL = raw ? server + "/raw/" : server + "/";

                    response = postURL + response;
                }

                result.complete(response);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }).start();

        return result;
    }
}