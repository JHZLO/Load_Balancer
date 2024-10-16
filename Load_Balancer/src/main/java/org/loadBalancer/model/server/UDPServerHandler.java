package org.loadBalancer.model.server;

import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.loadBalancer.model.server.ServerHandler;

public class UDPServerHandler extends ServerHandler {
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public UDPServerHandler(String ip, int port) {
        super(ip, port);
    }

    @Override
    public void handleRequest() {
        try (DatagramSocket socket = new DatagramSocket(port, InetAddress.getByName(ip))) {
            System.out.println("UDP ServerHandler running on " + ip + ":" + port);

            byte[] buffer = new byte[1024];

            // 무한 루프를 돌며 UDP 패킷을 수신 대기
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);  // 클라이언트의 UDP 요청 수신

                // 각 패킷을 별도의 스레드에서 처리
                threadPool.execute(() -> handlePacket(socket, packet));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 수신한 패킷을 처리하는 메서드
    private void handlePacket(DatagramSocket socket, DatagramPacket packet) {
        try {
            // 수신된 데이터 읽기
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received: " + received);

            // 응답 데이터 생성
            String response = "Echo: " + received;
            byte[] responseData = response.getBytes();

            // 응답 패킷 생성 및 전송
            DatagramPacket responsePacket = new DatagramPacket(
                responseData, responseData.length, packet.getAddress(), packet.getPort());
            socket.send(responsePacket);

            System.out.println("Response sent to " + packet.getAddress() + ":" + packet.getPort());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
