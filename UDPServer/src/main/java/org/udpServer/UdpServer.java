package org.udpServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServer {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java UdpServer <Port>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("UDP Server is running on port " + port);

            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                // 클라이언트로부터 데이터 수신
                socket.receive(packet);

                String receivedData = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received: " + receivedData);

                // 클라이언트로 데이터 응답
                String response = "Echo: " + receivedData;
                DatagramPacket responsePacket = new DatagramPacket(
                    response.getBytes(),
                    response.getBytes().length,
                    packet.getAddress(),
                    packet.getPort()
                );
                socket.send(responsePacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
