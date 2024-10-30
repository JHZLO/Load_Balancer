package org.client;

public class MultiProtocolClient {

    public static void main(String[] args) {
        int udpPort = 8999; // 로드밸런서의 UDP 포트
        int tcpPort = 8888; // 로드밸런서의 TCP 포트
        int httpPort = 9000; // 로드밸런서의 HTTP 포트
        String serverAddress = "10.20.0.154";

        // UDP 테스트 스레드
        Thread udpThread = new Thread(new UDPClientTask(serverAddress, udpPort));
        // TCP 테스트 스레드
        Thread tcpThread = new Thread(new TCPClientTask(serverAddress, tcpPort));
        // HTTP 테스트 스레드
        Thread httpThread = new Thread(new HTTPClientTask(serverAddress, httpPort));

        // 스레드 시작
        udpThread.start();
        tcpThread.start();
        httpThread.start();

        // 스레드 종료 대기
        try {
            udpThread.join();
            tcpThread.join();
            httpThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
