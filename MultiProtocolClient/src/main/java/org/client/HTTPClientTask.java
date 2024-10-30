package org.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPClientTask implements Runnable {
    private final String serverAddress;
    private final int serverPort;

    public HTTPClientTask(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 30; i++) { // 10번 요청 전송
                // HTTP 요청 전송
                URL url = new URL("http://" + serverAddress + ":" + serverPort + "/balance");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                System.out.println("HTTP 요청 전송, 응답 코드: " + responseCode);

                if (responseCode == 200) { // 성공적으로 응답을 받은 경우
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    System.out.println("서버로부터 받은 HTTP 응답: " + response);
                } else {
                    System.out.println("서버로부터 받은 HTTP 응답: 실패");
                }
                connection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
