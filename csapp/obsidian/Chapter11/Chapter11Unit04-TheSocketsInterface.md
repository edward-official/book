==소켓 인터페이스(sockets interface)== 는 UNIX I/O 함수들과 함께 사용되어 네트워크 응용 프로그램을 작성할 때 사용하는 함수들의 집합입니다.
이 인터페이스는 대부분의 현대 시스템(모든 Unix 계열, Windows, Macintosh 등)에 구현되어 있습니다.

##### Figure 11.12
![[Figure11-12.png]]
위 그림은 전형적인 ==클라이언트-서버 트랜잭션== 에서 ==소켓 인터페이스==의 전반적인 흐름을 보여줍니다. ==(이해가 안되는데??)==
이 그림은 이후 각 함수를 설명할 때 전체적인 지도로 참고하면 좋습니다.

#### 11.4.1 Socket Address Structures

==리눅스 커널의 관점==에서 ==소켓(socket) 은 통신의 끝점(endpoint)==입니다.
==리눅스 프로그램의 관점==에서는, 소켓은 단순히 ==파일 디스크립터처럼 열린 파일==이며, 커널에 의해 관리됩니다.


인터넷 소켓 주소 구조체 예시 (16바이트 크기)

```c
/* IP socket address structure */
struct sockaddr_in {
  uint16_t sin_family;  /* 프로토콜 패밀리 (항상 AF_INET) */
  uint16_t sin_port;    /* 포트 번호 (네트워크 바이트 순서) */
  struct in_addr sin_addr; /* IP 주소 (네트워크 바이트 순서) */
  unsigned char sin_zero[8]; /* 크기 맞춤용 패딩 */
};

/* 범용 소켓 주소 구조체 */
struct sockaddr {
  uint16_t sa_family;  /* 프로토콜 패밀리 */
  char sa_data[14];    /* 주소 데이터 */
};
```

이 구조체는 인터넷 애플리케이션에서 사용되며,

* `sin_family` = `AF_INET`
* `sin_port` = 16비트 포트 번호
* `sin_addr` = 32비트 IP 주소 (항상 빅엔디안(big-endian) 형식으로 저장)

`connect`, `bind`, `accept` 함수는 특정 프로토콜의 소켓 주소 구조체를 가리키는 포인터를 필요로 합니다.
하지만 C 언어 초기에는 `void*` 포인터가 없었기 때문에, 소켓 API 설계자들은 모든 구조체를 `sockaddr` 형식으로 변환(cast) 하도록 설계했습니다.

편의상, 다음과 같이 `typedef` 를 정의하여 사용합니다:

```c
typedef struct sockaddr SA;
```

#### 11.4.2 The `socket` Function

클라이언트와 서버는 `socket()` 함수를 사용해 ==소켓 디스크립터(socket descriptor)== 를 생성합니다.

```c
#include <sys/types.h>
#include <sys/socket.h>

int socket(int domain, int type, int protocol);
/* 성공 시 음수가 아닌 디스크립터, 실패 시 -1 반환 */
```

예를 들어, 32비트 IP 주소와 TCP 스트림을 사용하는 소켓을 생성하려면:

```c
clientfd = Socket(AF_INET, SOCK_STREAM, 0);
```

* `AF_INET` → IPv4 사용
* `SOCK_STREAM` → TCP 스트림 기반 소켓

이렇게 생성된 소켓은 아직 완전히 열린 상태가 아니며, 데이터를 주고받기 위해서는 클라이언트 혹은 서버로서 연결을 설정(connect) 해야 합니다.

#### 11.4.3 The `connect` Function

클라이언트는 `connect()` 함수를 사용해 서버에 연결합니다.

```c
#include <sys/socket.h>

int connect(int clientfd, const struct sockaddr *addr, socklen_t addrlen);
/* 성공 시 0, 실패 시 -1 반환 */
```

* `clientfd`: `socket()` 으로 생성한 디스크립터
* `addr`: 서버의 주소 (예: `struct sockaddr_in`)
* `addrlen`: 주소 구조체의 크기 (`sizeof(struct sockaddr_in)`)

`connect()` 함수는 연결이 성공하거나 오류가 발생할 때까지 ==블록(block)==됩니다.
성공하면 소켓은 읽기/쓰기 가능한 상태가 되며, 연결은 다음 쌍으로 식별됩니다:

```
(x:y, addr.sin_addr:addr.sin_port)
```

즉, `x:y`는 클라이언트의 IP와 포트, `addr.sin_addr:addr.sin_port`는 서버의 IP와 포트를 의미합니다.

#### 11.4.4 `bind` 함수

서버는 `bind()` 함수를 사용하여 소켓 디스크립터를 특정 주소(포트 포함)에 연결합니다.

```c
#include <sys/socket.h>

int bind(int sockfd, const struct sockaddr *addr, socklen_t addrlen);
/* 성공 시 0, 실패 시 -1 반환 */
```

* `sockfd`: 서버 소켓 디스크립터
* `addr`: 서버의 IP와 포트를 담은 주소 구조체

`bind()`는 커널에 “이 소켓은 이 주소(포트)에 바인딩된다”는 사실을 알려줍니다.
서버가 여러 클라이언트의 요청을 받기 위해 필수적인 단계입니다.

#### 11.4.5 `listen` 함수

서버는 클라이언트 연결 요청을 수신하기 위해 대기(listen) 상태로 들어갑니다.

```c
#include <sys/socket.h>

int listen(int sockfd, int backlog);
/* 성공 시 0, 실패 시 -1 반환 */
```

* `sockfd`: `bind()`된 소켓
* `backlog`: 커널이 대기열에 저장할 수 있는 최대 연결 요청 수 (일반적으로 1024로 설정)

`listen()`은 소켓을 active socket (연결을 여는 측)에서 listening socket (연결을 기다리는 측)으로 전환시킵니다.


#### 11.4.6 `accept` 함수

서버는 `accept()` 함수를 호출하여 클라이언트의 연결 요청을 수락합니다.

```c
#include <sys/socket.h>

int accept(int listenfd, struct sockaddr *addr, int *addrlen);
/* 성공 시 새 연결 디스크립터 반환, 실패 시 -1 */
```

* `listenfd`: 클라이언트 요청을 기다리는 리스닝 소켓
* `addr`: 연결한 클라이언트의 주소를 저장할 버퍼

`accept()` 함수는 새로운 연결 소켓(connected descriptor) 을 반환합니다.
이 디스크립터는 클라이언트와의 실제 통신에 사용됩니다.

##### 리스닝 디스크립터 vs 연결 디스크립터

- 리스닝 디스크립터(listenfd) : 서버가 클라이언트 요청을 기다리는 소켓
- 연결 디스크립터(connfd) : 실제 클라이언트와 통신하는 소켓

서버는 평생 동안 하나의 `listenfd`만 유지하지만, 새로운 클라이언트가 연결될 때마다 새로운 `connfd`를 생성합니다.
이 구분 덕분에 서버는 여러 클라이언트를 동시에 처리할 수 있습니다.

##### 왜 두 가지 디스크립터를 구분할까?

처음에는 불필요해 보이지만, `listenfd` 와 `connfd` 를 구분함으로써 동시 서버(concurrent server) 를 구현할 수 있습니다.
즉, `listenfd`로 여러 클라이언트의 연결 요청을 감지하고, 각 요청이 들어올 때마다 새로운 프로세스나 스레드를 생성해 `connfd`로 통신을 처리할 수 있습니다.

#### 11.4.7 Host and Service Conversion
리눅스는 `getaddrinfo` 와 `getnameinfo` 라는 강력한 함수를 제공합니다.
이 함수들은 이진 소켓 주소 구조체(binary socket address structures) 와
호스트 이름, 호스트 주소, 서비스 이름, 포트 번호 등의 문자열 표현 간의 변환을 수행합니다.

이 함수들은 소켓 인터페이스와 함께 사용될 때,
특정 IP 프로토콜 버전에 종속되지 않은 네트워크 프로그램을 작성할 수 있게 해줍니다.


##### `getaddrinfo` 함수

`getaddrinfo` 함수는 문자열로 표현된 호스트 이름, 호스트 주소, 서비스 이름, 포트 번호 등을
소켓 주소 구조체(socket address structures) 로 변환합니다.
이 함수는 오래된 `gethostbyname` 및 `getservbyname` 함수의 현대적인 대체 함수입니다.
게다가 이 함수는 재진입 가능(reentrant) 하며, 모든 프로토콜과 함께 작동합니다.

```c
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>

int getaddrinfo(const char *host, const char *service,
                const struct addrinfo *hints,
                struct addrinfo result);
/* 성공 시 0, 실패 시 비 0 값 반환 */

void freeaddrinfo(struct addrinfo *result);
/* 메모리 해제 */

const char *gai_strerror(int errcode);
/* 오류 메시지 문자열 반환 */
```


##### `getaddrinfo`가 반환하는 데이터 구조

`getaddrinfo`는 `host` 와 `service` 인자를 기반으로
하나 이상의 `addrinfo` 구조체를 연결 리스트 형태로 반환합니다.
각 구조체는 `host` 와 `service` 에 해당하는 하나의 소켓 주소를 가리킵니다.
(그림 11.15 참고)


호출 과정 설명:

1. 클라이언트는 `getaddrinfo`를 호출하면, 여러 `addrinfo` 구조체를 포함하는 리스트를 받습니다.
2. 이 리스트를 순회하며 각 주소를 `socket()` → `connect()` 순서로 시도합니다.
   성공하면 연결이 설정됩니다.
3. 서버의 경우에도 리스트를 순회하며 `socket()` → `bind()`를 호출합니다.
4. 모든 작업이 끝나면 `freeaddrinfo()`로 메모리를 해제해야 합니다.

`getaddrinfo`가 비 0 값을 반환하면,
`gai_strerror()`를 사용해 오류 코드를 문자열로 변환할 수 있습니다.


##### 인자 설명

* `host`
  도메인 이름(예: `www.example.com`) 또는 숫자 IP 주소(예: `192.168.0.1`)
  주소 변환을 원하지 않으면 `NULL` 로 설정 가능.

* `service`
  서비스 이름(예: `"http"`) 또는 포트 번호(예: `"80"`)
  `host` 또는 `service` 중 하나는 반드시 지정해야 함.

* `hints`
  `addrinfo` 구조체 포인터이며,
  `getaddrinfo`가 반환할 주소 목록의 필터링 기준을 지정할 수 있습니다.
  사용하지 않을 필드는 0 또는 NULL로 초기화해야 합니다.


##### `addrinfo` 구조체

```c
struct addrinfo {
  int ai_flags;         /* 힌트 플래그 */
  int ai_family;        /* 주소 체계(AF_INET 또는 AF_INET6) */
  int ai_socktype;      /* 소켓 타입(SOCK_STREAM 등) */
  int ai_protocol;      /* 프로토콜 번호 */
  char *ai_canonname;   /* 정식 호스트 이름 */
  size_t ai_addrlen;    /* ai_addr 구조체의 크기 */
  struct sockaddr *ai_addr;  /* 소켓 주소 구조체 포인터 */
  struct addrinfo *ai_next;  /* 다음 구조체로의 포인터 (링크드 리스트) */
};
```


##### `hints` 구조체의 주요 필드

* `ai_family`

  * `AF_INET`: IPv4 주소만 반환
  * `AF_INET6`: IPv6 주소만 반환

* `ai_socktype`

  * `SOCK_STREAM`: TCP 연결용
  * `SOCK_DGRAM`: UDP용 (다루지 않음)
    → 설정하면 각 주소당 하나의 구조체만 반환.

* `ai_flags`
  여러 옵션을 비트 OR 연산으로 결합 가능.

###### 유용한 플래그들

| 플래그              | 설명                                     |
| ---------------- | -------------------------------------- |
| `AI_ADDRCONFIG`  | 로컬 호스트가 IPv4/IPv6로 설정된 경우에만 해당 주소를 반환. |
| `AI_CANONNAME`   | 정식 호스트 이름(`ai_canonname`)을 반환 리스트에 포함. |
| `AI_NUMERICSERV` | `service`를 이름 대신 숫자 포트로 처리.        |
| `AI_PASSIVE`     | 서버용 소켓 주소를 반환. (와일드카드 주소 사용: 0.0.0.0)  |


##### `getnameinfo` 함수

`getnameinfo`는 `getaddrinfo`의 역방향 함수(reverse function) 입니다.
즉, 소켓 주소 구조체를 문자열 형태의 호스트명과 서비스명으로 변환합니다.

이 함수는 오래된 `gethostbyaddr` 및 `getservbyport`의 대체 함수이며,
재진입 가능(reentrant)하고 프로토콜 독립적입니다.

```c
#include <sys/socket.h>
#include <netdb.h>

int getnameinfo(const struct sockaddr *sa, socklen_t salen,
                char *host, size_t hostlen,
                char *service, size_t servlen, int flags);
/* 성공 시 0, 실패 시 비 0 값 반환 */
```


인자 설명

* `sa`: 소켓 주소 구조체 포인터
* `salen`: 해당 구조체의 크기
* `host`: 변환된 호스트 이름을 저장할 버퍼
* `service`: 변환된 서비스 이름(또는 포트 번호)을 저장할 버퍼
* `flags`: 동작을 제어하는 플래그

주요 플래그

| 플래그              | 설명                         |
| ---------------- | -------------------------- |
| `NI_NUMERICHOST` | DNS 조회 없이 숫자 IP 주소 문자열을 반환 |
| `NI_NUMERICSERV` | 서비스 이름 대신 숫자 포트 번호를 반환     |


##### 예제: `HOSTINFO` 프로그램

아래는 `getaddrinfo` 와 `getnameinfo` 를 이용해
도메인 이름을 해당 IP 주소 목록으로 변환해 출력하는 프로그램입니다.

```c
#include "csapp.h"

int main(int argc, char argv) {
  struct addrinfo *p, *listp, hints;
  char buf[MAXLINE];
  int rc, flags;

  if (argc != 2) {
    fprintf(stderr, "usage: %s <domain name>\n", argv[0]);
    exit(0);
  }

  memset(&hints, 0, sizeof(struct addrinfo));
  hints.ai_family = AF_INET;       /* IPv4 전용 */
  hints.ai_socktype = SOCK_STREAM; /* TCP 연결만 */
  rc = getaddrinfo(argv[1], NULL, &hints, &listp);
  if (rc != 0) {
    fprintf(stderr, "getaddrinfo error: %s\n", gai_strerror(rc));
    exit(1);
  }

  flags = NI_NUMERICHOST; /* 도메인 대신 IP 문자열 표시 */
  for (p = listp; p; p = p->ai_next) {
    getnameinfo(p->ai_addr, p->ai_addrlen, buf, MAXLINE, NULL, 0, flags);
    printf("%s\n", buf);
  }

  freeaddrinfo(listp);
  exit(0);
}
```


이 프로그램은 도메인 이름을 입력받아
해당 이름에 대응하는 모든 IP 주소를 표시합니다.
`nslookup` 명령어와 유사한 기능을 수행합니다.


실행 예시:

```
linux> ./hostinfo twitter.com
199.16.156.102
199.16.156.230
199.16.156.6
199.16.156.70
```

이 출력은 앞서 `nslookup twitter.com` 명령에서 본 결과와 동일합니다.




#### 11.4.8 Helper Functions for the Sockets Interface
`getaddrinfo` 함수와 소켓 인터페이스는 처음 배우는 사람들에게 다소 복잡하게 느껴질 수 있습니다.
그래서 여기서는 두 가지 고수준 보조 함수(helper functions) 인
`open_clientfd` 와 `open_listenfd` 를 사용해
클라이언트와 서버가 서로 통신할 때 코드를 단순화합니다.


##### `open_clientfd` 함수

클라이언트는 서버와 연결을 설정하기 위해 `open_clientfd` 함수를 호출합니다.

```c
#include "csapp.h"

int open_clientfd(char *hostname, char *port);
/* 성공 시 디스크립터 반환, 실패 시 -1 */
```

이 함수는 지정된 `hostname` 과 `port` 번호로 동작 중인 서버에 연결을 시도합니다.
연결이 성공하면, 데이터를 읽고 쓸 수 있는 열린 소켓 디스크립터(open socket descriptor) 를 반환합니다.

내부적으로 `getaddrinfo` 를 호출하여 서버의 주소 목록을 얻은 뒤,
각 주소에 대해 순차적으로 `socket` 과 `connect` 를 시도합니다.
성공 시 즉시 연결을 반환하고, 실패하면 다음 주소로 넘어갑니다.
모든 시도가 실패하면 `-1` 을 반환합니다.

이 함수는 특정 IP 버전에 의존하지 않으며,
`getaddrinfo` 덕분에 IPv4, IPv6 모두 호환됩니다.


`open_clientfd` 코드 (Figure 11.18)

```c
int open_clientfd(char *hostname, char *port) {
  int clientfd;
  struct addrinfo hints, *listp, *p;

  memset(&hints, 0, sizeof(struct addrinfo));
  hints.ai_socktype = SOCK_STREAM;   /* 연결형 소켓 */
  hints.ai_flags = AI_NUMERICSERV;   /* 숫자형 포트 사용 */
  hints.ai_flags |= AI_ADDRCONFIG;   /* 주소 설정 권장 옵션 */
  Getaddrinfo(hostname, port, &hints, &listp);

  for (p = listp; p; p = p->ai_next) {
    if ((clientfd = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) < 0)
      continue;  /* 소켓 생성 실패 → 다음 주소로 */

    if (connect(clientfd, p->ai_addr, p->ai_addrlen) != -1)
      break;     /* 연결 성공 */

    Close(clientfd);  /* 연결 실패 시 소켓 닫기 */
  }

  Freeaddrinfo(listp);
  if (!p)  /* 연결 실패 */
    return -1;
  else
    return clientfd;  /* 성공적으로 열린 디스크립터 반환 */
}
```


##### `open_listenfd` 함수

서버는 클라이언트의 연결 요청을 수락하기 위해 리스닝 디스크립터(listening descriptor) 를 생성합니다.

```c
#include "csapp.h"

int open_listenfd(char *port);
/* 성공 시 디스크립터 반환, 실패 시 -1 */
```

`open_listenfd` 함수는 `port` 번호를 인자로 받아
그 포트에서 클라이언트 연결 요청을 기다릴 준비가 된 리스닝 소켓 을 반환합니다.

내부 동작은 `open_clientfd` 와 유사합니다.
`getaddrinfo` 로 가능한 주소 목록을 얻은 후,
각 주소에 대해 `socket` → `bind` → `listen` 순서로 실행합니다.


`open_listenfd` 코드 (Figure 11.19)

```c
int open_listenfd(char *port) {
  struct addrinfo hints, *listp, *p;
  int listenfd, optval = 1;

  memset(&hints, 0, sizeof(struct addrinfo));
  hints.ai_socktype = SOCK_STREAM;           /* TCP 연결용 */
  hints.ai_flags = AI_PASSIVE | AI_ADDRCONFIG; /* 모든 IP 주소 수락 */
  hints.ai_flags |= AI_NUMERICSERV;          /* 숫자형 포트 사용 */
  Getaddrinfo(NULL, port, &hints, &listp);

  for (p = listp; p; p = p->ai_next) {
    if ((listenfd = socket(p->ai_family, p->ai_socktype, p->ai_protocol)) < 0)
      continue;

    /* 포트 재사용 설정 (서버 재시작 시 에러 방지) */
    Setsockopt(listenfd, SOL_SOCKET, SO_REUSEADDR, (const void *)&optval, sizeof(int));

    if (bind(listenfd, p->ai_addr, p->ai_addrlen) == 0)
      break;  /* 바인딩 성공 */

    Close(listenfd);
  }

  Freeaddrinfo(listp);
  if (!p)
    return -1;

  if (listen(listenfd, LISTENQ) < 0) {
    Close(listenfd);
    return -1;
  }
  return listenfd;
}
```

이 함수는 재진입 가능(reentrant) 하고 프로토콜 독립적(protocol-independent) 입니다.




#### 11.4.9 Example Echo Client and Server

소켓 인터페이스를 가장 잘 배우는 방법은 예제 코드를 직접 보는 것입니다.


##### 에코 클라이언트 (Figure 11.20)

```c
#include "csapp.h"

int main(int argc, char argv) {
  int clientfd;
  char *host, *port, buf[MAXLINE];
  rio_t rio;

  if (argc != 3) {
    fprintf(stderr, "usage: %s <host> <port>\n", argv[0]);
    exit(0);
  }

  host = argv[1];
  port = argv[2];

  clientfd = Open_clientfd(host, port);
  Rio_readinitb(&rio, clientfd);

  while (Fgets(buf, MAXLINE, stdin) != NULL) {
    Rio_writen(clientfd, buf, strlen(buf));
    Rio_readlineb(&rio, buf, MAXLINE);
    Fputs(buf, stdout);
  }

  Close(clientfd);
  exit(0);
}
```

이 프로그램은 사용자의 입력을 서버로 보내고,
서버가 보낸 에코(반환된 문자열) 를 화면에 출력합니다.


##### 에코 서버 (Figure 11.21)

```c
#include "csapp.h"

void echo(int connfd);

int main(int argc, char argv) {
  int listenfd, connfd;
  socklen_t clientlen;
  struct sockaddr_storage clientaddr;
  char client_hostname[MAXLINE], client_port[MAXLINE];

  if (argc != 2) {
    fprintf(stderr, "usage: %s <port>\n", argv[0]);
    exit(0);
  }

  listenfd = Open_listenfd(argv[1]);
  while (1) {
    clientlen = sizeof(struct sockaddr_storage);
    connfd = Accept(listenfd, (SA *)&clientaddr, &clientlen);
    Getnameinfo((SA *)&clientaddr, clientlen,
                client_hostname, MAXLINE,
                client_port, MAXLINE, 0);
    printf("Connected to (%s, %s)\n", client_hostname, client_port);
    echo(connfd);
    Close(connfd);
  }
  exit(0);
}
```

이 서버는 하나의 클라이언트 요청을 처리한 후 종료하지 않고, 무한 루프를 돌며 다음 요청을 기다립니다.
이런 서버를 반복형(iterative) 서버 라고 합니다.
(12장에서는 동시형(concurrent) 서버 를 다룸.)


##### 에코 함수 (Figure 11.22)

```c
#include "csapp.h"

void echo(int connfd) {
  size_t n;
  char buf[MAXLINE];
  rio_t rio;

  Rio_readinitb(&rio, connfd);
  while ((n = Rio_readlineb(&rio, buf, MAXLINE)) != 0) {
    printf("server received %d bytes\n", (int)n);
    Rio_writen(connfd, buf, n);
  }
}
```

이 함수는 클라이언트가 보낸 문자열을 읽고,
동일한 내용을 다시 클라이언트에게 보내는 단순한 에코 서버의 핵심입니다.


##### Aside — 연결에서의 EOF란 무엇인가?

많은 학생들이 인터넷 연결에서의 EOF(End of File) 개념을 혼동합니다.
먼저 EOF 문자라는 것은 존재하지 않습니다.

EOF는 커널이 감지하는 상태(condition) 입니다.
즉, 응용 프로그램은 `read()` 함수가 0을 반환할 때 EOF를 알 수 있습니다.

* 디스크 파일의 경우:
  파일 포인터가 파일의 끝을 초과했을 때 EOF 발생
* 인터넷 연결의 경우:
  한쪽 프로세스가 연결을 닫을 때 EOF 발생
  반대편 프로세스는 마지막 바이트 이후를 읽으려 시도할 때 EOF를 감지합니다.
