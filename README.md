# 🎯Load_Balancer🎯
> 자바로 로드밸런서 구현하기

[[Java로 로드밸런서 구현하기] 로드밸런서의 개념 (1)](https://jhzlo.tistory.com/45)

<br>


## ✅ Registration
![image](https://github.com/user-attachments/assets/9ae92cb6-ea15-4a94-88e8-a7f242eaa5c1)

- 각각의 서버는 로드밸런서에 연결되어야 한다 -> 서버에 대한 정보를 로드밸런서가 알고 있어야 함

  설정파일을 사용할 것이다. - `json`의 형태로 관리
- dynamic하게 구현할 것이다 -> 로드밸런서는 프로토콜과 포트를 바인딩하고 listen해야 한다

 `load balancer`는 register 받으면 하나씩 balance할 수 있는 기능을 구현해야한다 (`round-robin`으로 구현)
=> 그러고 나서 ack를 반환한다.

<br>

## ✅ Unregistration
![image](https://github.com/user-attachments/assets/4c59c59b-d84d-42af-9cf9-babe7ee1d3c0)

- 서버가 종료될 때, 로드 밸런서는 포트를 `close`한다.
- 다른 서버는 여전히 `open port`를 유지한다
=> member를 관리한다

<br>


## ✅ Health Check
![image](https://github.com/user-attachments/assets/ed11cb25-f7ee-41be-a9f6-0391844c435f)

- 로드 밸런서는 각각의 서버에 hello message를 보낸다
	 hello message를 받으면 server에 `traffic`을 보낸다
- server와 disconnection을 하면 traffic을 더 이상 보내지 않고 port를 `close`한다.


<br>


## ✅ Load Balancing
![image](https://github.com/user-attachments/assets/09d90f45-9904-444c-92f4-81f06084bae7)

- 비록 새로운 서버가 추가되더라도, 클라이언트의 트래픽은 같은 서버에 keeps going
- 기본적으로 로드밸런서 `RountRobin`의 형태로 구현
  - Need to maintain a serverHandler list mapped to the open port
  - Need to maintain a session table
	
- TCP UDP같은 경우는 network를 잘써서 줘야한다.

---

### 📌 추가 고려사항
- 서론


- 구현
    - architecture적으로 어떻게 짰는지 (code level말고)
        - 컴포넌트를 어떻게 나눴는지
        - 멀티쓰레드? 싱글 쓰레드?
    - 왜 그렇게 선택했는지
 

- 환경설정
    - 어떻게 돌리는지
        - 컴파일하고 run어떻게 하는지
    - 스크린샷
    - 성능에 대한 그래프와 테이블


## 테스트
![image](https://github.com/user-attachments/assets/db82b4e9-ac78-47f8-a58f-c673ebed920e)
![image](https://github.com/user-attachments/assets/6186026c-5dd0-4f6d-930b-9e84bead6f14)
![image](https://github.com/user-attachments/assets/838f1fcd-0dfc-4e31-956e-59be75a38a8b)
![image](https://github.com/user-attachments/assets/c24d200b-887a-4f1a-9af8-a2251ec7e742)

### 트러블 슈팅
![image](https://github.com/user-attachments/assets/6ac4bc92-5c95-43f8-a7cb-9a14822e372c)

![image](https://github.com/user-attachments/assets/c24d47bd-a17d-4839-b5eb-13c03585fa2e)

![image](https://github.com/user-attachments/assets/6732372f-1fc8-4b46-ba47-1f1b11451a21)


## 부하테스트
![image](https://github.com/user-attachments/assets/b6343ddf-b041-412d-96d4-248ffbfe3836)




