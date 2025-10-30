##### Figure 11.8
![[Figure11-08.png]]
전 세계 IP 인터넷(Global IP Internet)은 가장 유명하고 성공적인 인터넷 구현체입니다.
1969년부터 어떤 형태로든 존재해왔습니다.
인터넷의 내부 구조는 복잡하고 계속 변화하고 있지만, 클라이언트-서버 응용 프로그램의 구조는 1980년대 초 이후로 거의 변하지 않았습니다.

각 인터넷 호스트는 ==TCP/IP(Transmission Control Protocol / Internet Protocol)==을 구현한 소프트웨어를 실행합니다.
이 프로토콜은 거의 모든 현대 컴퓨터 시스템이 지원합니다.
인터넷 클라이언트와 서버는 ==소켓 인터페이스(socket interface)==와 ==UNIX I/O== 함수를 혼합하여 통신합니다.
소켓 함수들은 일반적으로 ==시스템 호출(system call)==로 구현되어 커널로 진입하여 ==커널 모드의 TCP/IP 함수==를 호출합니다.

##### TCP/IP 프로토콜 패밀리
TCP/IP는 ==여러 프로토콜들의 집합==으로, 각각 다른 기능을 제공합니다.
- IP (Internet Protocol): 기본적인 주소 지정(naming)과 전송(delivery)메커니즘을 제공합니다. IP는 데이터그램(datagram)이라 불리는 패킷 단위로 데이터를 전송합니다. 그러나 IP는 비신뢰적(unreliable)으로, 손실되거나 중복된 패킷을 복구하지 않습니다.
- UDP (User Datagram Protocol): IP를 약간 확장하여, 프로세스 간에 직접 데이터그램을 전송할 수 있도록 합니다. UDP 역시 비연결형이며, 신뢰성을 보장하지 않습니다.
- TCP (Transmission Control Protocol): IP 위에서 동작하며, 양방향(full-duplex)통신을 제공하고, 신뢰성 있는 데이터 스트림을 전송합니다.

##### 프로그래머의 관점
프로그래머의 관점에서 인터넷은 다음과 같은 성질을 가진 전 세계 호스트들의 집합으로 생각할 수 있습니다.
- 호스트들은 32비트 IP 주소의 집합으로 매핑됩니다.
- IP 주소들은 도메인 이름(Internet domain name)으로 매핑됩니다.
- 한 호스트의 프로세스는 다른 호스트의 프로세스와 연결(connection)을 통해 통신할 수 있습니다.

##### IPv4와 IPv6 (Aside)
- 기존 인터넷 프로토콜은 IPv4(Internet Protocol Version 4)로, 32비트 주소를 사용합니다.
- 1996년 IETF(Internet Engineering Task Force)가 IPv6을 제안했으며, 이는 128비트 주소를 사용하도록 설계되었습니다.
- 그러나 2015년 기준으로도 대부분의 인터넷 트래픽은 여전히 IPv4를 사용하며, ==Google 서비스 이용자의 약 4%==만 IPv6을 사용했습니다.
- 따라서 이 책에서는 IPv6를 다루지 않고, IPv4 중심으로 설명합니다. 다만, 이후 다룰 클라이언트-서버 프로그래밍 기법은 특정 프로토콜에 종속되지 않습니다.

#### 11.3.1 IP Addresses
![[Figure11-09.png]]
IP 주소는 ==부호 없는 32비트 정수(unsigned 32-bit integer)== 입니다.
네트워크 프로그램은 IP 주소를 구조체 형태로 저장합니다.
서버 간 호스트는 서로 다른 바이트 순서를 가질 수 있기 때문에, TCP/IP는 모든 정수 데이터를 ==네트워크 바이트 순서(network byte order)== — 즉 빅엔디안(big-endian) 으로 정의합니다.
따라서 IP 주소는 항상 빅엔디안 형식으로 저장되며, 호스트가 리틀엔디안이라도 동일합니다.


##### 엔디안 변환 함수

```c
#include <arpa/inet.h>

uint32_t htonl(uint32_t hostlong);
uint16_t htons(uint16_t hostshort);
uint32_t ntohl(uint32_t netlong);
uint16_t ntohs(uint16_t netshort);
```

* `htonl` : 호스트 바이트 순서 → 네트워크 바이트 순서 (32비트)
* `ntohl` : 네트워크 바이트 순서 → 호스트 바이트 순서 (32비트)
* `htons` / `ntohs` : 16비트 정수용


IP 주소는 사람이 읽기 쉽게 하기 위해 ==점으로 구분된 10진 표기(dotted-decimal notation)== 로 표현됩니다.
예:

```
128.2.194.242
```

이는 주소 `0x8002c2f2` 의 10진 표현입니다.

리눅스에서 자신의 호스트 주소를 확인하려면:

```
hostname -i
128.2.210.175
```


##### 문자열 ↔ IP 변환 함수

```c
#include <arpa/inet.h>

int inet_pton(AF_INET, const char *src, void *dst);
const char *inet_ntop(AF_INET, const void *src, char *dst, socklen_t size);
```

* `inet_pton`: 문자열(“128.2.194.242”) → 32비트 IP 주소로 변환
* `inet_ntop`: IP 주소 → 점으로 구분된 문자열로 변환

