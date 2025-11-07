공유 변수(shared variables)는 편리하지만, 동시에 **동기화 오류(synchronization errors)** 의 위험을 초래한다.
이 문제를 보여주는 예제로 **`badcnt.c`** 프로그램(그림 12.16)을 살펴보자.
이 프로그램은 두 개의 스레드를 생성하며, 각 스레드는 전역 공유 변수 `cnt`를 `niters`번씩 증가시킨다.

각 스레드가 `niters`번씩 `cnt`를 증가시키므로, 프로그램 종료 시 `cnt`의 예상 값은 `2 x niters`
가 되어야 한다.

겉보기에는 단순하고 명확한 프로그램처럼 보인다.
그러나 실제로 리눅스에서 `badcnt.c`를 실행하면, **결과가 매번 다르고, 심지어 틀린 값이 나온다!**


### Figure 12.16 — `badcnt.c`: 잘못 동기화된 카운터 프로그램

```c
/* WARNING: This code is buggy! */
#include "csapp.h"

void *thread(void *vargp);  /* 스레드 루틴 프로토타입 */

/* 전역 공유 변수 */
volatile long cnt = 0;      /* 카운터 */

int main(int argc, char **argv)
{
  long niters;
  pthread_t tid1, tid2;

  if (argc != 2) {
    printf("usage: %s <niters>\n", argv[0]);
    exit(0);
  }

  niters = atoi(argv[1]);

  /* 스레드 생성 및 종료 대기 */
  Pthread_create(&tid1, NULL, thread, &niters);
  Pthread_create(&tid2, NULL, thread, &niters);
  Pthread_join(tid1, NULL);
  Pthread_join(tid2, NULL);

  /* 결과 확인 */
  if (cnt != (2 * niters))
    printf("BOOM! cnt=%ld\n", cnt);
  else
    printf("OK cnt=%ld\n", cnt);
  exit(0);
}

/* 스레드 루틴 */
void *thread(void *vargp)
{
  long i, niters = *(long *)vargp;

  for (i = 0; i < niters; i++)
    cnt++;

  return NULL;
}
```

==`volatile` 키워드의 역할은??==

실행 결과 예시:

```
linux> ./badcnt 1000000
BOOM! cnt=1445085
linux> ./badcnt 1000000
BOOM! cnt=1915220
linux> ./badcnt 1000000
BOOM! cnt=1404746
```

매번 결과가 다르게 출력된다. 왜 그럴까?


### 원인 분석

문제를 이해하기 위해 루프 부분(`lines 40–41`)의 어셈블리 코드를 살펴보자.
이 코드를 다섯 부분으로 나눌 수 있다:

| 기호     | 의미                                        |
| ------ | ----------------------------------------- |
| **Hi** | 루프 시작 부분의 명령어 블록                          |
| **Li** | 공유 변수 `cnt`를 레지스터 `%rdx_i`로 로드(load)하는 명령 |
| **Ui** | `%rdx_i`를 1 증가시키는 명령                      |
| **Si** | `%rdx_i`의 값을 다시 `cnt`에 저장(store)하는 명령     |
| **Ti** | 루프 끝 부분의 명령어 블록                           |

`Hi`와 `Ti`는 스레드의 지역 변수만 다루고,
`Li`, `Ui`, `Si`는 공유 변수 `cnt`를 다룬다.


### Figure 12.17 — 루프 부분의 어셈블리 코드

```
C code:
for (i = 0; i < niters; i++)
  cnt++;

Assembly (for thread i):
movq  (%rdi), %rcx
testq %rcx, %rcx
jle   .L2
movl  $0, %eax
.L3:
  movq  cnt(%rip), %rdx    ; L_i
  addq  $1, %rdx           ; U_i
  movq  %rdx, cnt(%rip)    ; S_i
  addq  $1, %rax
  cmpq  %rcx, %rax
  jne   .L3
.L2:
```


### 명령어 순서의 차이

두 스레드가 단일 CPU에서 번갈아 실행될 때,
각 스레드의 명령들이 **어떤 순서(interleaving)** 로 실행되는지는 운영체제가 결정한다.
이 순서가 올바르면 결과가 맞지만, 그렇지 않으면 틀린 결과가 나온다.


### Figure 12.18 — 루프 1회 반복의 명령 순서 예시

| (a) 올바른 순서 | (b) 잘못된 순서 |
| ---------- | ---------- |
| 1: H₁      | 1: H₁      |
| 2: L₁      | 2: L₁      |
| 3: U₁      | 3: U₁      |
| 4: S₁      | 4: H₂      |
| 5: H₂      | 5: L₂      |
| 6: L₂      | 6: S₁      |
| 7: U₂      | 7: T₁      |
| 8: S₂      | 8: U₂      |
| 9: T₂      | 9: S₂      |
| 10: T₁     | 10: T₂     |

왼쪽 (a)에서는 최종 `cnt = 2` (정상).
오른쪽 (b)에서는 최종 `cnt = 1` (잘못된 결과).


### 핵심 요점

운영체제가 어떤 순서를 선택할지는 **예측할 수 없다**.
즉, 스레드가 공유 변수를 동시에 접근하면 ==**결과가 비결정적(nondeterministic)**== 이 된다.
이것이 ==**경쟁 조건(race condition)**== 이다.


## 12.5.1 진행 그래프 (Progress Graphs)

**Progress Graph**는 여러 스레드의 실행 과정을 좌표 공간 위의 궤적(trajectory)으로 모델링한 것이다.

* 각 축은 한 스레드의 진행 정도를 나타냄
* 각 점은 “어떤 스레드가 몇 번째 명령까지 실행했는가”를 나타냄
* 오른쪽(→) 이동은 스레드 1의 명령 실행
* 위쪽(↑) 이동은 스레드 2의 명령 실행
* 대각선 이동은 불가능 (두 스레드가 동시에 명령 실행 불가)


### Figure 12.19 — `badcnt.c` 루프 1회 반복의 진행 그래프

![[Screenshot 2025-11-07 at 11.37.39.png]]

* 가로축: Thread 1
* 세로축: Thread 2
* 각 축의 순서: `H → L → U → S → T`
* 예: 점 `(L₁, S₂)` → Thread1이 L₁까지, Thread2가 S₂까지 실행한 상태


### Figure 12.20 — 예시 궤적 (Trajectory)

![[Screenshot 2025-11-07 at 11.39.38.png]]

아래 실행 순서에 해당:

```
H₁, L₁, U₁, H₂, L₂, S₁, T₁, U₂, S₂, T₂
```

궤적은 실행의 실제 경로를 의미하며, 각 점은 프로그램의 상태를 나타낸다.


## 임계 구역 (Critical Section)

스레드 i에서 `Lᵢ`, `Uᵢ`, `Sᵢ`는 공유 변수 `cnt`를 다루므로 ==**임계 구역(critical section)**== 이라 한다.
임계 구역은 다른 스레드의 임계 구역과 **동시에 실행되어서는 안 된다.**

이를 보장하는 것이 ==**상호 배제(mutual exclusion)**== 개념이다.


### Figure 12.21 — ==안전 궤적과 비안전 궤적==

![[Screenshot 2025-11-07 at 11.45.25.png]]

* 두 스레드의 임계 구역이 겹치는 부분 = **Unsafe region (위험 구역)**
* 이 영역에 진입하지 않는 경로 = **Safe trajectory (안전 궤적)**
* 이 영역을 통과하는 경로 = **Unsafe trajectory (경쟁 상태)**


✅ 안전 궤적을 유지하면 `cnt`가 올바르게 갱신된다.
⚠️ Unsafe trajectory를 밟으면 잘못된 결과(`cnt` 손실)가 발생한다.

따라서 올바른 동작을 위해 스레드들이 항상 **Safe trajectory**를 유지하도록 ==**동기화(synchronize)**== 해야 한다.
이를 구현하는 고전적인 방법이 바로 ==**세마포어(semaphore)**== 이다.


## 12.5.2 세마포어 (Semaphores)

컴퓨터 과학자 ==**Edsger Dijkstra**==는 병행 프로그래밍의 선구자로,
스레드 간 동기화 문제를 해결하기 위해 ==**세마포어(semaphore)**== 라는 개념을 제안했다.

세마포어 `s`는 음수가 아닌 정수 값을 가지는 **전역 변수**이며,
다음 두 가지 연산만을 통해 조작된다:


### P(s) 연산

* `s`가 **0이 아니면**, `s`를 1 감소시키고 즉시 반환
* `s`가 **0이면**, 스레드를 **차단(block)** 시키고
  다른 스레드가 `V(s)`로 `s`를 증가시킬 때까지 대기

재시작되면 `s`를 감소시키고 제어권을 돌려줌


### V(s) 연산

* `s`를 1 증가시킴
* 만약 `P(s)`에서 대기 중인 스레드가 있다면, 그 중 **하나만** 재시작시킴


📝 **이름의 유래**

* P: 네덜란드어 *proberen* (시험하다, test)
* V: *verhogen* (증가시키다, increment)


P와 V 연산은 ==**원자적(atomic)**== 으로 수행되어, 중간에 끼어들 수 없다.
`V` 연산은 여러 스레드가 대기 중일 때, ==**어느 스레드가 깨어날지는 예측할 수 없다.**==

이러한 규칙 덕분에 세마포어는 항상 음수가 될 수 없으며,
이 속성을 ==**세마포어 불변식(semaphore invariant)**== 이라고 부른다.


## POSIX 세마포어 함수

```c
#include <semaphore.h>

int sem_init(sem_t *sem, 0, unsigned int value);
int sem_wait(sem_t *s);  /* P(s) */
int sem_post(sem_t *s);  /* V(s) */
```

* `sem_init`: 세마포어 초기화 (`value`는 초기값)
* `sem_wait`: P 연산 (감소 또는 대기)
* `sem_post`: V 연산 (증가 또는 깨우기)


편의상 다음과 같은 **래퍼 함수(wrapper)** 로도 사용된다:

```c
#include "csapp.h"

void P(sem_t *s);  /* sem_wait의 래퍼 */
void V(sem_t *s);  /* sem_post의 래퍼 */
```

## 12.5.3 세마포어를 이용한 상호 배제 (Using Semaphores for Mutual Exclusion)

세마포어는 **공유 변수에 대한 상호 배제(mutual exclusion) 접근**을 보장하는 간편한 방법을 제공한다.
기본 아이디어는 각 공유 변수(또는 관련된 변수 집합)에 대해 ==**초기값이 1인 세마포어 `s`**== 를 연결한 뒤,
그 변수에 접근하는 ==임계 구역(critical section)을 **`P(s)`와 `V(s)` 연산으로 감싸는 것**==이다.

공유 변수를 보호하는 목적으로 사용되는 세마포어는 **이진 세마포어(binary semaphore)** 라고 하며,
그 값은 항상 0 또는 1이다.
이진 세마포어는 보통 **뮤텍스(mutex)** 라고 부른다.

* `P` 연산을 수행하는 것을 **뮤텍스 잠금(locking)** 이라고 한다.
* `V` 연산을 수행하는 것을 **뮤텍스 해제(unlocking)** 이라고 한다.
* 잠근 상태에서 아직 해제하지 않은 스레드는 **뮤텍스를 보유(holding)** 중이라고 한다.

한편, 세마포어를 여러 개의 자원을 관리하기 위한 카운터로 사용하는 경우에는
이를 ==**계수 세마포어(counting semaphore)**== 라고 부른다.


### Figure 12.22 — 세마포어를 이용한 상호 배제

![[Screenshot 2025-11-07 at 13.23.36.png]]

그림 12.22는 **이진 세마포어를 사용해 `badcnt.c` 프로그램을 동기화하는 방법**을 보여준다.
각 상태에는 세마포어 `s`의 현재 값이 표시되어 있다.

핵심 아이디어는 `P`와 `V` 연산의 조합으로 인해
`s < 0`인 상태들이 만들어지는데, 이 상태들의 집합을 ==**금지 영역(forbidden region)**== 이라고 한다.

세마포어 불변식(semaphore invariant)에 따르면,
실행 가능한 어떤 궤적도 `s < 0`인 금지 영역에 포함될 수 없다.
게다가 이 금지 영역은 ==**불안전 영역(unsafe region)**== 을 완전히 감싸고 있기 때문에,
프로그램의 실행 궤적은 절대로 불안전 영역에 들어갈 수 없다.
즉, 실행 순서와 상관없이 항상 안전하게 공유 변수를 갱신할 수 있다.


💡 **운영 관점에서 보면**,
`P`와 `V` 연산으로 만들어진 금지 영역 덕분에
여러 스레드가 동시에 임계 구역 안의 명령을 수행하는 것이 불가능해진다.
다시 말해, 세마포어는 **임계 구역에 대한 상호 배제 접근을 보장**한다.


### 예시: `goodcnt.c` 프로그램

다음은 `badcnt.c`를 세마포어로 수정한 버전이다.

```c
volatile long cnt = 0;   /* Counter */
sem_t mutex;              /* Protects counter */

int main(int argc, char **argv)
{
  long niters;
  pthread_t tid1, tid2;

  niters = atoi(argv[1]);
  Sem_init(&mutex, 0, 1);   /* mutex = 1 */

  Pthread_create(&tid1, NULL, thread, &niters);
  Pthread_create(&tid2, NULL, thread, &niters);
  Pthread_join(tid1, NULL);
  Pthread_join(tid2, NULL);

  printf("cnt=%ld\n", cnt);
  exit(0);
}

void *thread(void *vargp)
{
  long i, niters = *(long *)vargp;
  for (i = 0; i < niters; i++) {
    P(&mutex);     /* Lock */
    cnt++;
    V(&mutex);     /* Unlock */
  }
  return NULL;
}
```


### 실행 결과

```
linux> ./goodcnt 1000000
OK cnt=2000000

linux> ./goodcnt 1000000
OK cnt=2000000
```

이제 매번 정확한 결과가 출력된다.


📘 **참고: Progress Graph의 한계**

Progress Graph는 단일 CPU에서의 스레드 동시 실행을 시각적으로 이해하기에는 유용하지만, **멀티프로세서 환경**에서는 CPU 캐시 일관성이나 메모리 접근 방식 차이 ==(아마 write-back 같은 정책에서 L1 캐시가 CPU 내부에 위치하는 사실 때문에 차이가 발생하는 듯)==로 인해 그래프에서 설명되지 않는 상태들이 발생할 수 있다.
그럼에도 핵심 메시지는 동일하다.

> "단일 프로세서든 멀티프로세서든, 공유 변수 접근은 반드시 동기화해야 한다."


## 12.5.4 세마포어를 이용한 공유 자원 스케줄링

(Using Semaphores to Schedule Shared Resources)

세마포어의 또 다른 중요한 용도는 ==**공유 자원에 대한 접근 순서를 제어(scheduling)**== 하는 것이다.
이 경우, 한 스레드가 다른 스레드에게
“지금 어떤 조건이 만족되었다”고 알리기 위해 세마포어를 사용한다.

==대표적인 두 가지 예시==는 다음과 같다:

1. **생산자–소비자 문제 (Producer–Consumer Problem)**
2. **Reader–Writer 문제 (Readers–Writers Problem)**


### ==생산자–소비자 문제 (Producer–Consumer Problem)==

![[Screenshot 2025-11-07 at 14.00.52.png]]

그림 12.23은 이 문제를 보여준다.
생산자(Producer) 스레드는 아이템을 만들어 버퍼에 넣고,
소비자(Consumer) 스레드는 버퍼에서 아이템을 꺼내 사용한다.
이 버퍼는 **n개의 슬롯을 가진 유한 버퍼(bounded buffer)** 이다.

생산자 스레드는 반복적으로 새 항목을 생성하고 버퍼에 삽입하며,
소비자 스레드는 반복적으로 버퍼에서 항목을 꺼내 사용한다.
여러 생산자와 소비자가 동시에 존재할 수도 있다.

버퍼를 수정할 때는 **상호 배제(mutex)** 가 필요하지만,
그것만으로는 충분하지 않다.
다음과 같은 **스케줄링 문제**도 존재한다:

* 버퍼가 가득 찼을 때 → 생산자는 빈 슬롯이 생길 때까지 기다려야 함
* 버퍼가 비었을 때 → 소비자는 새 항목이 생길 때까지 기다려야 함


### 실제 응용 예시 ==(이게 왜 생산자 소비자인지 이해가 안돼??)==

* **멀티미디어 스트리밍**: 생산자(인코더)가 프레임을 생성하고, 소비자(디코더)가 화면에 표시
* **GUI 시스템**: 입력 이벤트(생산자)를 버퍼에 넣고, 렌더링 스레드(소비자)가 화면에 그림


### `SBUF` 패키지 (Bounded Buffer 구현)

아래 구조체는 `SBUF` 패키지에서 사용되는 버퍼 정의이다.

```c
typedef struct {
  int *buf;      /* 버퍼 배열 */
  int n;         /* 최대 슬롯 개수 */
  int front;     /* buf[(front+1)%n]은 첫 번째 아이템 */
  int rear;      /* buf[rear%n]은 마지막 아이템 */
  sem_t mutex;   /* 버퍼 접근 보호 */
  sem_t slots;   /* 남은 빈 슬롯 수 */
  sem_t items;   /* 사용 가능한 아이템 수 */
} sbuf_t;
```


### Figure 12.25 — SBUF 함수 구현

```c
#include "csapp.h"
#include "sbuf.h"

/* 버퍼 초기화 */
void sbuf_init(sbuf_t *sp, int n)
{
  sp->buf = Calloc(n, sizeof(int));
  sp->n = n;
  sp->front = sp->rear = 0;
  Sem_init(&sp->mutex, 0, 1);
  Sem_init(&sp->slots, 0, n);
  Sem_init(&sp->items, 0, 0);
}

/* 버퍼 해제 */
void sbuf_deinit(sbuf_t *sp)
{
  Free(sp->buf);
}

/* 항목 삽입 (생산자) */
void sbuf_insert(sbuf_t *sp, int item)
{
  P(&sp->slots);                      /* 빈 슬롯 대기 */
  P(&sp->mutex);                      /* 버퍼 잠금 */
  sp->buf[(++sp->rear) % (sp->n)] = item;
  V(&sp->mutex);                      /* 버퍼 해제 */
  V(&sp->items);                      /* 아이템 추가 알림 */
}

/* 항목 제거 (소비자) */
int sbuf_remove(sbuf_t *sp)
{
  int item;
  P(&sp->items);                      /* 아이템 대기 */
  P(&sp->mutex);                      /* 버퍼 잠금 */
  item = sp->buf[(++sp->front) % (sp->n)];
  V(&sp->mutex);                      /* 버퍼 해제 */
  V(&sp->slots);                      /* 슬롯 비었음 알림 */
  return item;
}
```

이 구현은 생산자와 소비자가 동시에 접근해도 안정적으로 동작한다.


## ==Readers–Writers 문제==

**Readers–Writers 문제**는 ==상호 배제 문제의 확장판==이다.
여러 스레드가 공유 자원(예: 메모리의 자료구조, 데이터베이스 레코드 등)에 접근할 때:

* **Writer**: 데이터를 수정하는 스레드
* **Reader**: 데이터를 읽기만 하는 스레드

Writer는 반드시 **독점적 접근(exclusive access)** 을 가져야 하지만,
Reader들은 동시에 접근해도 된다.


이 문제는 ==두 가지 버전==이 있다:

1. **첫 번째 Readers–Writers 문제 (Reader 우선)**

   * Writer가 대기 중이더라도, 이미 읽고 있는 Reader들이 모두 끝날 때까지 기다린다.
   * 즉, Reader는 대기하지 않지만, Writer는 대기할 수 있다.

2. **두 번째 Readers–Writers 문제 (Writer 우선)**

   * Writer가 쓰기를 기다리는 중이라면, 새로운 Reader는 기다려야 한다.
   * 즉, Writer의 대기 시간을 최소화한다.


### Figure 12.26 — 첫 번째 Readers–Writers 문제 해결 코드

```c
/* 전역 변수 */
int readcnt;        /* Initially 0 */
sem_t mutex, w;     /* Both initially 1 */

void reader(void)
{
  while (1) {
    P(&mutex);
    readcnt++;
    if (readcnt == 1)
      P(&w);              /* 첫 번째 Reader는 Writer 차단 */
    V(&mutex);

    /* 임계 구역: 읽기 수행 */

    P(&mutex);
    readcnt--;
    if (readcnt == 0)
      V(&w);              /* 마지막 Reader가 Writer 허용 */
    V(&mutex);
  }
}

void writer(void)
{
  while (1) {
    P(&w);
    /* 임계 구역: 쓰기 수행 */
    V(&w);
  }
}
```

이 코드는 Reader들에게 우선권을 준다. ==(reader가 하나라도 실행되면 writer를 막아버림)==
단, 많은 Reader가 연속해서 들어오면 Writer가 **영원히 대기(starvation)** 할 수도 있다.


## 12.5.5 프리스레딩 기반 병행 서버 (Putting It Together: A Concurrent Server Based on Prethreading)

이제 우리는 세마포어를 사용해 **공유 변수에 접근**하고, **공유 자원에 대한 접근을 스케줄링**하는 방법을 배웠다.
이번에는 이러한 개념들을 좀 더 구체적으로 이해하기 위해,
==**프리스레딩(prethreading)**== 이라는 기법을 이용한 ==**병행 서버(concurrent server)**== 에 적용해보자.


### 기존 병행 서버의 한계

이전(그림 12.14)의 병행 서버에서는 **새로운 클라이언트가 연결될 때마다 새로운 스레드를 생성**했다.
하지만 이 방식에는 단점이 있다.
→ 클라이언트마다 새 스레드를 만드는 데 드는 **비용이 크다.**


### 프리스레딩(prethreading) 아이디어

이 문제를 해결하기 위해, 프리스레딩 서버는 **프로그램 시작 시 여러 개의 ==워커 스레드(worker threads)**== 를 **미리 생성**해둔다.
그 후 메인 스레드가 클라이언트 연결을 받아 ==**공유 버퍼에 작업을 넣고**==,
워커 스레드들은 **버퍼에서 작업을 꺼내 처리**한다.

즉, **생산자–소비자 모델(producer-consumer model)** 을 적용한 것이다.


### Figure 12.27 — 프리스레딩 병행 서버의 구조

```
Client  --->  Master thread (accepts connections)
              ↓
           [Buffer of connected descriptors]
              ↓
Worker threads (service clients)
```

* **메인 스레드**: 클라이언트 연결 요청을 수락(accept)하고, 해당 연결의 파일 디스크립터(connfd)를 버퍼에 삽입한다.
* **워커 스레드**: 버퍼에서 connfd를 꺼내, 클라이언트 요청을 처리한다(예: echo 서비스).

이 방식에서는 새로운 클라이언트가 와도 스레드를 새로 만들 필요가 없고,
이미 만들어진 워커 스레드가 바로 일을 처리한다.


### 다른 동기화 메커니즘 (Aside)

세마포어는 단순하고 고전적인 동기화 방식이지만,
현대 언어와 라이브러리에서는 다른 방법들도 사용된다.

* **Java Monitor**: 자바에서 스레드 동기화를 위한 고수준 추상화. 세마포어 기반으로 구현할 수 있다.
* **Pthreads 조건 변수 (condition variables)**:

  * `mutex`: 상호 배제에 사용
  * `condition`: 특정 조건을 기다리거나 알릴 때 사용
    이들은 프로듀서–컨슈머 문제처럼 공유 자원의 접근 순서를 제어하는 데 쓰인다.


### Figure 12.28 — 프리스레딩 병행 에코 서버 코드

```c
#include "csapp.h"
#include "sbuf.h"

#define NTHREADS 4
#define SBUFSIZE 16

void echo_cnt(int connfd);
void *thread(void *vargp);

sbuf_t sbuf;  /* 연결된 파일 디스크립터를 저장하는 공유 버퍼 */

int main(int argc, char **argv)
{
  int i, listenfd, connfd;
  socklen_t clientlen;
  struct sockaddr_storage clientaddr;
  pthread_t tid;

  if (argc != 2) {
    fprintf(stderr, "usage: %s <port>\n", argv[0]);
    exit(0);
  }

  listenfd = Open_listenfd(argv[1]);
  sbuf_init(&sbuf, SBUFSIZE);  /* 버퍼 초기화 */

  /* 워커 스레드 미리 생성 */
  for (i = 0; i < NTHREADS; i++)
    Pthread_create(&tid, NULL, thread, NULL);

  while (1) {
    clientlen = sizeof(struct sockaddr_storage);
    connfd = Accept(listenfd, (SA *)&clientaddr, &clientlen);
    sbuf_insert(&sbuf, connfd);  /* connfd를 버퍼에 삽입 */
  }
}

void *thread(void *vargp)
{
  Pthread_detach(pthread_self());
  while (1) {
    int connfd = sbuf_remove(&sbuf);  /* 버퍼에서 connfd 꺼내기 */
    echo_cnt(connfd);                 /* 클라이언트 서비스 */
    Close(connfd);
  }
}
```

✅ **핵심 구조**

* 메인 스레드 → **생산자 역할** (연결 수락 후 버퍼에 저장)
* 워커 스레드 → **소비자 역할** (버퍼에서 꺼내 서비스 수행)


### Figure 12.29 — `echo_cnt`: 클라이언트로부터 받은 총 바이트 수를 세는 echo 함수

```c
#include "csapp.h"

static int byte_cnt;   /* 누적 바이트 수 */
static sem_t mutex;    /* 보호용 세마포어 */

static void init_echo_cnt(void)
{
  Sem_init(&mutex, 0, 1);
  byte_cnt = 0;
}

void echo_cnt(int connfd)
{
  int n;
  char buf[MAXLINE];
  rio_t rio;
  static pthread_once_t once = PTHREAD_ONCE_INIT;

  Pthread_once(&once, init_echo_cnt);  /* 최초 1회만 초기화 */

  Rio_readinitb(&rio, connfd);
  while ((n = Rio_readlineb(&rio, buf, MAXLINE)) != 0) {
    P(&mutex);
    byte_cnt += n;
    printf("server received %d (%d total) bytes on fd %d\n",
           n, byte_cnt, connfd);
    V(&mutex);
    Rio_writen(connfd, buf, n);
  }
}
```

💡 `pthread_once`는 여러 스레드가 있어도 **한 번만 실행되는 초기화 함수**를 등록할 때 사용한다.
→ `byte_cnt` 초기화 코드가 여러 번 실행되지 않도록 보장한다.


### 실행 흐름 요약

1. **서버 시작 시**

   * `sbuf_init()`으로 버퍼 생성
   * `Pthread_create()`로 워커 스레드 여러 개 미리 생성

2. **메인 스레드 (Producer)**

   * `Accept()`로 클라이언트 연결 수락
   * 연결된 `connfd`를 `sbuf_insert()`로 버퍼에 추가

3. **워커 스레드 (Consumers)**

   * `sbuf_remove()`로 `connfd`를 하나 꺼냄
   * `echo_cnt()`를 호출해 요청 처리
   * 처리 후 `Close(connfd)`


### 성능상의 이점

* 새로운 클라이언트가 와도 스레드 생성/소멸 오버헤드 없음
* 워커 스레드가 이미 대기 중이므로 응답 지연이 짧음
* 세마포어(`mutex`, `slots`, `items`)로 공유 버퍼 접근이 안전하게 보호됨


### 이벤트 기반 서버 (Aside)

프리스레딩 서버는 사실상 ==**이벤트 기반(event-driven)**== 구조로도 볼 수 있다.

* **메인 스레드의 상태(state)**

  * 연결 대기 중 (waiting for connection)
  * 버퍼 슬롯 대기 중 (waiting for available slot)

* **워커 스레드의 상태(state)**

  * 버퍼 아이템 대기 중 (waiting for available buffer item)

즉, 각 스레드가 I/O 이벤트(연결 도착, 버퍼 비움 등)에 따라 상태를 전이(transition)하는
**단순 상태 기계(state machine)** 로 표현될 수 있다.

