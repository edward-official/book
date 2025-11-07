지금까지 우리는 동시적인 논리적 흐름(logical flow)을 만드는 두 가지 방법을 살펴보았다.
첫 번째 접근법은 각 흐름마다 별도의 프로세스(process) 를 사용하는 것이다. 커널이 각 프로세스를 자동으로 스케줄링하며, 각 프로세스는 고유한 주소 공간(address space) 을 가지므로 흐름 간 데이터 공유가 어렵다.

두 번째 접근법은 하나의 프로세스 내에서 여러 논리적 흐름을 직접 생성하고, I/O Multiplexing 으로 이들을 명시적으로 스케줄링하는 것이다. 이 경우 ==프로세스가 하나==뿐이므로 모든 흐름이 동일한 주소 공간을 공유한다.


### 스레드 기반 접근법

이번 절에서는 세 번째 접근법인 스레드(thread) 기반 동시 프로그래밍을 소개한다.
이 방식은 앞의 ==두 가지(프로세스 기반 + I/O Multiplexing 기반)의 혼합형(hybrid) ==이다.

==스레드==는 프로세스의 문맥(context) 안에서 실행되는 하나의 논리적 흐름(logical flow) 이다.
지금까지의 예제들은 대부분 프로세스당 하나의 단일 스레드만을 사용했지만, 현대 시스템은 하나의 프로세스 내에서 여러 스레드가 동시에 실행 되도록 지원한다.

모든 스레드는 ==커널에 의해 자동으로 스케줄링==되며, 각 스레드는 다음과 같은 자신만의 ==스레드 문맥(thread context)== 을 가진다:

* 고유한 스레드 ID (TID)
* 스택(stack)
* 스택 포인터(stack pointer)
* 프로그램 카운터(program counter)
* 범용 레지스터(general-purpose registers)
* 조건 코드(condition codes)

모든 스레드는 동일한 프로세스의 ==가상 주소 공간 (코드, 데이터, 힙, 열린 파일, 공유 라이브러리 포함)을 공유==한다.


### 스레드 기반 동시 흐름의 특징

스레드 기반 논리 흐름은 프로세스 기반과 I/O Multiplexing 기반의 특성을 모두 가진다.

* 프로세스 기반처럼: ==커널이 스레드를 자동으로 스케줄링==하며, 각 스레드는 커널에 의해 정수형 ID로 식별된다.
* I/O Multiplexing 기반처럼: 하나의 프로세스 안에서 ==여러 스레드가 동시에 실행 (엄밀히 말하면 아닌거 같은데... 그냥 하나의 프로세스가 여러 I/O를 동시에 다루는 개념에 가깝지 않나?)==되므로 전체 주소 공간을 공유할 수 있다.


## 12.3.1 스레드 실행 모델 (Thread Execution Model)

다중 스레드의 실행 모델은 다중 프로세스 모델과 유사하다.
하나의 프로세스는 처음에 ==메인 스레드(main thread) 하나로 시작==한다.
그 후 메인 스레드는 피어 스레드(peer thread) 를 생성하며, 두 스레드는 동시에 실행된다.

시스템 타이머나 `read`, `sleep` 같은 시스템 호출에 의해 문맥 교환(context switch) 이 발생할 때마다 제어가 한 스레드에서 다른 스레드로 전환된다.

스레드 간 전환은 프로세스 간 전환보다 훨씬 빠르다.
또한 스레드는 프로세스처럼 ==부모-자식 관계가 아닌 피어 풀(pool of peers) 형태==로 구성된다.

즉,

* 어떤 스레드든 다른 스레드를 종료시킬 수 있고,
* 다른 스레드의 종료를 기다릴 수도 있으며,
* 동일한 공유 데이터를 읽고 쓸 수 있다.


### Figure 12.12

![[Screenshot 2025-11-06 at 21.10.52.png]]

Concurrent thread execution (스레드의 동시 실행)

시간 축을 따라 main thread와 peer thread가 번갈아가며 실행되고, 시스템에 의해 문맥 교환이 발생한다.


## 12.3.2 POSIX Threads (Pthreads)

POSIX Threads (Pthreads) 는 ==C 프로그램에서 스레드를 다루기 위한 표준 인터페이스==이다.
1995년에 표준으로 제정되었으며 모든 리눅스 시스템에서 사용할 수 있다.

Pthreads는 약 60개의 함수를 정의하며, 이들은 스레드 생성, 종료, 회수(reap), 통신 및 동기화에 사용된다.


### Figure 12.13 — “Hello, world!” Pthreads 프로그램

```c
#include "csapp.h"
void *thread(void *vargp);

int main()
{
  pthread_t tid;
  Pthread_create(&tid, NULL, thread, NULL);
  Pthread_join(tid, NULL);
  exit(0);
}

void *thread(void *vargp)
{
  printf("Hello, world!\n");
  return NULL;
}
```

설명:

* 메인 스레드는 새로운 피어 스레드를 생성 (`pthread_create`)
* 피어 스레드는 `"Hello, world!\n"` 을 출력하고 종료
* 메인 스레드는 `pthread_join` 으로 피어 스레드의 종료를 기다림
* 이후 메인 스레드가 종료되면서 전체 프로세스 종료


## 12.3.3 스레드 생성 (Creating Threads)

스레드는 `pthread_create` 함수를 호출해 생성된다.

```c
#include <pthread.h>
typedef void *(func)(void *);

int pthread_create(pthread_t *tid, pthread_attr_t *attr, func *f, void *arg);
```

* `tid`: 생성된 스레드의 ID 저장
* `attr`: 스레드 속성 (NULL 시 기본값)
* `f`: 실행할 함수 (스레드 루틴)
* `arg`: 함수에 전달할 인자 포인터

생성된 스레드는 함수 `f`를 실행하며, 종료 시 `pthread_self()`로 자신의 ID를 확인할 수 있다.


## 12.3.4 스레드 종료 (Terminating Threads)

스레드는 다음 방식 중 하나로 종료된다:

1. ==암묵적 종료 (implicit)== — 스레드 루틴이 `return`할 때
2. ==명시적 종료 (explicit)== — `pthread_exit(void *retval)` 호출 시
3. 프로세스 전체 종료 — 한 스레드가 `exit()` 호출 시
4. ==다른 스레드에 의해 취소됨== — `pthread_cancel(tid)` 호출 시


## 12.3.5 스레드 회수 (Reaping Terminated Threads)

스레드가 종료되면, 다른 스레드는 `pthread_join`으로 그 종료를 기다릴 수 있다.

```c
int pthread_join(pthread_t tid, void **thread_return);
```

이 함수는 지정한 스레드가 끝날 때까지 블록되며, 그 스레드의 반환값을 `thread_return` 위치에 저장한다.
`pthread_join`은 지정된 특정 스레드만 기다릴 수 있고, 임의의 스레드를 기다릴 방법은 없다.


## 12.3.6 스레드 분리 (Detaching Threads)

스레드는 ==조인 가능(joinable) 상태 또는 분리(detached) 상태== 중 하나이다.

* 조인 가능 스레드는 다른 스레드가 `pthread_join`으로 회수해야 한다.
* 분리된 스레드는 종료 시 자동으로 메모리 자원이 해제된다.

```c
int pthread_detach(pthread_t tid);
```

==일반적으로 서버==에서는 각 요청마다 새 스레드를 생성하므로, 일일이 join할 필요가 없도록
스레드 자신이 `pthread_detach(pthread_self())`를 호출해 자동 해제되게 만든다.


## 12.3.7 스레드 초기화 (Initializing Threads)

`pthread_once`는 스레드 관련 초기화를 한 번만 수행하기 위해 사용된다.

```c
pthread_once_t once_control = PTHREAD_ONCE_INIT;

int pthread_once(pthread_once_t *once_control, void (*init_routine)(void));
```

* 여러 스레드가 동시에 호출해도 `init_routine`은 단 한 번만 실행된다.
* 주로 공유 자원(global resource) 초기화에 사용된다.


## 12.3.8 스레드 기반 동시 서버 (Concurrent Server Based on Threads)

다음 코드는 스레드를 이용한 동시 에코 서버(concurrent echo server) 이다.
전체 구조는 프로세스 기반 서버와 유사하지만, 스레드를 사용하여 클라이언트를 동시에 처리한다.

```c
#include "csapp.h"

void echo(int connfd);
void *thread(void *vargp);

int main(int argc, char argv)
{
  int listenfd, *connfdp;
  socklen_t clientlen;
  struct sockaddr_storage clientaddr;
  pthread_t tid;

  if (argc != 2) {
    fprintf(stderr, "usage: %s <port>\n", argv[0]);
    exit(0);
  }

  listenfd = Open_listenfd(argv[1]);
  while (1) {
    clientlen = sizeof(struct sockaddr_storage);
    connfdp = Malloc(sizeof(int));
    *connfdp = Accept(listenfd, (SA *) &clientaddr, &clientlen);
    Pthread_create(&tid, NULL, thread, connfdp);
  }
}

void *thread(void *vargp)
{
  int connfd = *((int *)vargp);
  Pthread_detach(pthread_self());
  Free(vargp);
  echo(connfd);
  Close(connfd);
  return NULL;
}
```


### 주요 포인트

1. 경쟁 조건(race condition) 방지
   `connfd` 변수를 스택이 아닌 ==동적 메모리(Malloc) 로 할당==해야 한다.
   그렇지 않으면 `accept` 호출과 스레드 실행 사이의 타이밍 문제로 잘못된 소켓 번호를 참조할 수 있다.

2. 메모리 누수 방지
   ==각 스레드는 자신을 `detach` 해야 하며==(`pthread_detach(pthread_self())`),
   메인 스레드가 할당한 메모리를 스스로 `Free` 해야 한다.

