어떤 에코 서버를 작성해야 한다고 가정해 보자. 이 서버는 네트워크 클라이언트의 요청뿐 아니라 사용자가 표준 입력으로 입력하는 명령(==예를 들어서 종료==)에도 동시에 응답해야 한다.
즉, 두 가지 독립적인 I/O 이벤트에 모두 반응해야 한다:

1. 네트워크 클라이언트가 연결 요청을 보냄
2. 사용자가 키보드로 명령을 입력함

문제는 “무엇을 먼저 기다릴 것인가?”이다.
만약 `accept` 호출에서 클라이언트 연결을 기다리고 있다면, 표준 입력 명령에 반응할 수 없다.
반대로 `read` 호출에서 입력 명령을 기다리고 있다면, 새로운 연결 요청에 반응하지 못한다.

이 문제를 해결하기 위한 방법이 바로 ==I/O Multiplexing== 이다.
핵심 아이디어는 `select` 함수를 이용해 커널에 “다음 I/O 이벤트가 발생할 때까지 프로세스를 일시 중단(suspend)하라”고 요청하는 것이다.
즉, 하나 이상의 파일 디스크립터에서 이벤트가 발생할 때만 제어가 애플리케이션으로 돌아온다.

예를 들어 다음과 같은 경우를 처리할 수 있다:

* 집합 {0, 4}에 속한 디스크립터 중 하나라도 읽기 가능(read-ready) 상태일 때 반환
* 집합 {1, 2, 7} 중 하나라도 쓰기 가능(write-ready) 상태일 때 반환
* 152.13초 동안 아무 I/O 이벤트가 없으면 타임아웃

`select` 함수는 다양한 시나리오에서 사용할 수 있지만, 여기서는 ==“읽기 가능한 디스크립터 집합을 기다리는”== 한 가지 경우에 집중한다.


#### `select` 함수의 기본 구조

```c
#include <sys/select.h>

int select(int n, fd_set *fdset, NULL, NULL, NULL);
```

> 반환값: 준비된(ready) 디스크립터의 개수, 에러 시 –1

#### 주요 매크로들

```c
FD_ZERO(fd_set *fdset);    /* fdset의 모든 비트를 0으로 초기화 */
FD_CLR(int fd, fd_set *fdset);  /* fdset에서 fd 비트를 클리어 */
FD_SET(int fd, fd_set *fdset);  /* fdset에 fd 비트를 추가 */
FD_ISSET(int fd, fd_set *fdset);/* fdset에 fd 비트가 켜져 있는가 확인 */
```


#### 동작 원리

`select`는 `fd_set`이라는 구조체 집합을 다룬다.
이를 디스크립터 집합(descriptor set) 이라고 부르며, 내부적으로는 비트 벡터처럼 동작한다.

예를 들어, 크기가 n인 집합은 다음과 같이 표현된다:

```
b_(n-1), ..., b_1, b_0
```

각 비트 `b_k`는 디스크립터 k를 의미하며, `b_k = 1`이면 그 디스크립터가 집합에 속한다는 뜻이다.

우리가 할 수 있는 일은 세 가지뿐이다:

1. 집합을 할당한다.
2. 집합을 복사한다.
3. FD 매크로를 이용해 비트를 조작하거나 검사한다.


#### 사용 예시

`select`는 두 개의 인자를 받는다:

* 읽기를 기다릴 디스크립터 집합(`read_set`)
* 그 집합의 최대 크기(`n`)

`select`는 `read_set`에 속한 디스크립터 중 ==하나라도 읽기 가능한 상태가 될 때까지 블록(block)== 된다.
예를 들어 디스크립터 k가 읽기 가능하다는 것은 “이 디스크립터에서 1바이트 읽기를 시도해도 블록되지 않는다”는 뜻이다.

이때 `select`는 부수효과로 `fd_set`을 수정한다.
즉, 원래의 `read_set`을 “읽기 가능한 디스크립터만 포함하는 집합”으로 바꿔놓는다.


#### 예시 코드: `select`를 이용한 에코 서버

다음 예제는 표준 입력(stdin)과 네트워크 연결을 동시에 처리하는 반복형(iterative) 에코 서버이다.

##### 초기 상태:

* `listenfd` = 3 (리스닝 소켓)
* `stdin` = 0 (표준 입력)

`read_set(∅)`

```
3  2  1  0  
0  0  0  0
```

##### `read_set({0,3})`

```
3  2  1  0  
1  0  0  1
```

이 상태에서 서버는 `select`를 호출한다.

* 사용자가 엔터를 누르면 `stdin`이 읽기 가능 상태가 되어 반환된다.
* 새로운 클라이언트가 연결을 요청하면 `listenfd`가 읽기 가능 상태가 되어 반환된다.


#### 코드 예시 — `select.c`

```c
#include "csapp.h"
void echo(int connfd);
void command(void);

int main(int argc, char argv)
{
  int listenfd, connfd;
  socklen_t clientlen;
  struct sockaddr_storage clientaddr;
  fd_set read_set, ready_set;

  if (argc != 2) {
    fprintf(stderr, "usage: %s <port>\n", argv[0]);
    exit(0);
  }

  listenfd = Open_listenfd(argv[1]);
  FD_ZERO(&read_set);
  FD_SET(STDIN_FILENO, &read_set);
  FD_SET(listenfd, &read_set);

  while (1) {
    ready_set = read_set;
    Select(listenfd + 1, &ready_set, NULL, NULL, NULL);

    if (FD_ISSET(STDIN_FILENO, &ready_set))
      command();

    if (FD_ISSET(listenfd, &ready_set)) {
      clientlen = sizeof(struct sockaddr_storage);
      connfd = Accept(listenfd, (SA *) &clientaddr, &clientlen);
      echo(connfd);
      Close(connfd);
    }
  }
}

void command(void)
{
  char buf[MAXLINE];
  if (!Fgets(buf, MAXLINE, stdin))
    exit(0); // EOF
  printf("%s", buf);
}
```

I/O Multiplexing을 사용하는 반복형 에코 서버.
서버는 `select`를 통해 표준 입력(stdin) 과 리스닝 소켓(listenfd) 모두의 이벤트를 기다린다. ==(캬 대박이다)==


## 12.2.1 I/O Multiplexing 기반의 이벤트 구동형 동시 서버

I/O Multiplexing은 이벤트 구동(event-driven) 프로그램의 기반이 될 수 있다.
이 프로그램에서는 논리적 흐름이 특정 이벤트 발생에 따라 진행된다.

이벤트 구동 설계는 일반적으로 ==상태 머신(state machine)== 으로 모델링된다.
각 상태 머신은 상태(states), 입력 이벤트(input events), 전이(transitions) 의 집합으로 구성된다.

예를 들어, 각 클라이언트 k마다 하나의 상태 머신 sₖ을 만들고, 이 머신은 연결된 디스크립터 dₖ를 담당한다.

* 상태: “디스크립터 dₖ가 읽기 가능해지기를 기다리는 중”
* 입력 이벤트: “디스크립터 dₖ가 읽기 가능해짐”
* 전이: “디스크립터 dₖ에서 한 줄 읽기(read line)”


### Figure 12.7

![[Figure12-07.png]]
이벤트 구동형 에코 서버의 논리 흐름을 위한 상태 머신

```
상태: dₖ가 읽기 가능해지기를 기다리는 중
입력 이벤트: dₖ가 읽기 가능함
전이: dₖ에서 한 줄 읽기
```


### Figure 12.8
![[Figure12-08.png]]
I/O Multiplexing 기반의 동시 에코 서버

이 서버는 각 반복(iteration)마다 다음 두 가지 이벤트를 감지한다:

1. 새로운 클라이언트의 연결 요청
2. 기존 클라이언트의 데이터 전송 준비 완료

`select` 함수로 이러한 이벤트를 감지하고, 각 준비된 디스크립터에 대해 `echo`를 수행한다.


### Figure 12.9

![[Screenshot 2025-11-06 at 17.51.03.png]]

`init_pool` 함수 — 클라이언트 풀 초기화

### Figure 12.10

![[Screenshot 2025-11-06 at 17.51.22.png]]

`add_client` 함수 — 새로운 클라이언트를 풀에 추가

### Figure 12.11

![[Screenshot 2025-11-06 at 17.52.43.png]]

`check_clients` 함수 — 준비된 클라이언트들의 입력을 처리하고 에코 수행


## 12.2.2 I/O Multiplexing의 장단점

I/O Multiplexing 기반의 이벤트 구동 서버는 다음과 같은 장점을 가진다:

* (1) 프로그래머가 각 논리 흐름의 제어를 세밀하게 조정할 수 있다.
  (예: 어떤 클라이언트에게 우선순위를 줄지 직접 결정 가능)
* (2) 모든 논리 흐름이 하나의 프로세스 내에서 실행되므로, 공유 데이터 관리가 용이하다.
* (3) 단일 프로세스이므로 디버깅이 쉽고, GDB 등 표준 도구를 사용할 수 있다.
* (4) 문맥 교환(context switch)이 없으므로 성능이 일반적인 프로세스 기반 설계보다 좋다.

그러나 단점도 있다:

* 복잡한 코드 구조:
  동일한 동작을 하는 프로세스 기반 서버보다 세 배 정도의 코드가 필요하다.
* 세분성(granularity) 문제:
  ==각 논리 흐름이 너무 큰 단위로만 실행되면(예: 한 번에 한 줄씩만 처리), 동시성이 떨어진다. (음 막 와닿지는 않네??)==
* 악의적 클라이언트 취약성:
  ==부분 문자열만 보내고 중단하는 클라이언트가 있으면 서버가 블록될 수 있다. (이건 또 뭔소리지??)==
* 멀티코어 활용 불가:
  ==단일 프로세스로 동작하므로 여러 CPU 코어를 병렬로 활용할 수 없다. (이것도 막 와닿지가 않네??)==


요약하자면, I/O Multiplexing 기반 서버는 프로세스나 스레드보다 효율적이지만, 구현 난이도와 유지보수 복잡도가 훨씬 높다.


