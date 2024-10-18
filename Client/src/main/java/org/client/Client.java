package org.client;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    public static void main(String[] args) {
        // 스레드를 관리하는 스레드 풀 생성 (스레드 갯수는 프로세서 수만큼)
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // 여러 서버에 동시에 접속
        executor.execute(() -> connectToUdpServer("127.0.0.1", 8091, "Hello, UDP Server!"));
        executor.execute(() -> connectToTcpServer("127.0.0.1", 8090, "Hello, TCP Server!"));
        executor.execute(() -> connectToHttpServer("127.0.0.1", 8092));

        // 스레드 풀 종료
        executor.shutdown();
    }

    // UDP 서버에 접속하는 메서드
    public static void connectToUdpServer(String serverIp, int port, String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(serverIp);

            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, port);
            socket.send(sendPacket); // UDP 서버에 데이터 전송

            byte[] buffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(receivePacket); // UDP 서버로부터 응답 받기

            String response = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("UDP Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TCP 서버에 접속하는 메서드
    public static void connectToTcpServer(String serverIp, int port, String message) {
        try (Socket socket = new Socket(serverIp, port);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // TCP 서버로 데이터 전송
            out.println(message);
            String response = in.readLine(); // TCP 서버로부터 응답 받기
            System.out.println("TCP Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // HTTP 서버에 접속하는 메서드
    public static void connectToHttpServer(String serverIp, int port) {
        try {
            URL url = new URL("http://" + serverIp + ":" + port + "/");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode(); // HTTP 서버로부터 응답 코드 받기
            System.out.println("HTTP Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // HTTP 서버로부터 응답 출력
            System.out.println("HTTP Response: " + response.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
