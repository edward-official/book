공유 데이터를 동기화해야 할 때 프로그램이 훨씬 복잡해진다는 것을 눈치챘을 것이다.
지금까지는 **상호 배제(mutual exclusion)** 와 **생산자-소비자(producer-consumer)** 문제 해결 기법을 살펴봤지만,
이것은 빙산의 일각에 불과하다.

**동기화(synchronization)** 는 본질적으로 어렵다.
이는 순차적 프로그램에서는 전혀 발생하지 않는 새로운 종류의 문제를 야기한다.

이 절에서는 스레드를 사용할 때 흔히 발생하는 주요 동시성 문제들을 다룬다.
특히 ==**스레드가 공유 자원을 다룰 때 생길 수 있는 전형적인 오류들**==을 다룬다.


### 12.7.1 스레드 안전성 (Thread Safety)

스레드를 사용할 때는, 반드시 **스레드 안전(thread safety)** 을 보장하도록 함수를 작성해야 한다.

> 어떤 함수가 여러 개의 스레드에 의해 동시에 호출되어도
> 항상 올바른 결과를 내면 그 함수는 **thread-safe** 하다고 한다.

그렇지 않다면 **thread-unsafe** 하다고 부른다.

스레드 안전하지 않은 함수는 일반적으로 다음 ==네 가지 (서로 겹치지 않는) 유형==으로 분류할 수 있다.


#### ✅ Class 1: 공유 변수를 보호하지 않는 함수

예시로, 다음은 스레드 안전하지 않은 난수 생성기이다:

```c
unsigned next_seed = 1;

unsigned rand(void) {
  next_seed = next_seed * 1103515245 + 12543;
  return (unsigned)(next_seed >> 16) % 32768;
}

void srand(unsigned new_seed) {
  next_seed = new_seed;
}
```

이 함수는 전역 변수 `next_seed`를 보호하지 않는다.
여러 스레드가 동시에 호출하면 **경쟁 조건(race condition)** 이 발생해 엉뚱한 결과를 만든다.

➡️ 해결법: `next_seed` 접근을 `P()` / `V()` 같은 세마포어로 보호하면 된다.
단점은 — 동기화로 인해 속도가 느려진다는 것.


#### ✅ Class 2: 여러 호출 사이의 상태를 유지하는 함수

위의 `rand()`가 여기에 해당한다.
이 함수는 이전 호출의 결과(`next_seed`)에 의존하므로 다른 스레드가 동시에 호출하면 서로의 상태를 덮어쓴다.
![[Screenshot 2025-11-07 at 17.11.53.png]]
➡️ 해결법:
`static` 변수를 없애고, 상태를 호출자에게 인자로 넘기도록 고쳐야 한다.
예를 들어 `rand_r()`처럼 바꾸면 된다. ==(그래서 뭐 인자는 어떻게 다르게 줄건데..??)==


#### ✅ Class 3: static 변수를 가리키는 포인터를 반환하는 함수

예를 들어 C 표준 라이브러리의 `ctime()` 은 static 메모리를 사용한다:

```c
char *ctime(const time_t *timep);
```

이 함수는 내부에서 static 버퍼에 문자열을 저장하고, 그 버퍼의 포인터를 반환한다.
따라서 여러 스레드가 동시에 `ctime()`을 호출하면
서로의 결과를 덮어쓰게 된다.

➡️ 해결법 1: 함수를 재작성하여 결과를 저장할 버퍼를 호출자가 직접 전달하게 한다. ==(이게 뭔 말인지를 모르겠는데..?? > 아 이해 완료)==
➡️ 해결법 2: **lock-and-copy 기법** 사용.
뮤텍스로 보호하고, 결과를 private 버퍼에 복사한다.

예:

```c
char *ctime_ts(const time_t *timep, char *private) {
  char *sharedp;
  P(&mutex);
  sharedp = ctime(timep);
  strcpy(private, sharedp);
  V(&mutex);
  return private;
}
```


#### ✅ Class 4: thread-unsafe 함수를 호출하는 함수

함수 `f()`가 thread-unsafe 함수 `g()`를 호출하면, `f()`도 unsafe일 가능성이 있다.

* 만약 `g()`가 Class 2 유형이라면, `f()`도 unsafe하다. ==(일단 말만으로는 이해가 안돼....)==
* 하지만 `g()`가 Class 3이고, `f()`가 그 결과를 mutex로 보호한다면 `f()`는 safe할 수도 있다.


### Figure 12.39 — 함수의 관계

![[Screenshot 2025-11-07 at 17.04.00.png]]

### 12.7.2 재진입성 (Reentrancy)

==**Reentrant function (재진입 가능한 함수)** 는 **어떤 공유 데이터도 참조하거나 수정하지 않는** thread-safe 함수이다.==
즉, 여러 스레드가 동시에 호출하더라도 각자의 스택/인자만을 사용한다.

모든 reentrant 함수는 thread-safe이지만, 모든 thread-safe 함수가 reentrant인 것은 아니다.
(뮤텍스로 보호되는 함수는 thread-safe이지만 reentrant는 아니다.)


예를 들어 `rand()`를 reentrant하게 바꾸면 다음과 같다:

```c
int rand_r(unsigned int *nextp) {
  *nextp = *nextp * 1103515245 + 12345;
  return (unsigned int)(*nextp / 65536) % 32768;
}
```

이 함수는 상태(`nextp`)를 전역 static 변수 대신 **호출자 인자**로 전달받기 때문에 다른 스레드와 공유하지 않는다 → **reentrant**하다.


#### Reentrancy의 종류

* ==**Explicitly reentrant**==
  → 모든 인자와 지역 변수가 자동 변수(스택에 존재)일 때 항상 안전.
* ==**Implicitly reentrant**==
  → 호출자가 올바르게 non-shared 데이터를 넘겨줄 경우에만 안전.

`rand_r()`는 implicitly reentrant 함수의 예이다.


### 12.7.3 기존 라이브러리 함수의 사용

대부분의 C 표준 라이브러리 함수(`malloc`, `printf`, `scanf` 등)는 thread-safe이지만,
몇몇 오래된 함수들은 그렇지 않다.

| 함수            | 스레드 안전성 클래스 | 안전한 버전          |
| ------------- | ----------- | --------------- |
| rand          | 2           | rand_r          |
| strtok        | 2           | strtok_r        |
| asctime       | 3           | asctime_r       |
| ctime         | 3           | ctime_r         |
| gethostbyaddr | 3           | gethostbyaddr_r |
| gethostbyname | 3           | gethostbyname_r |
| inet_ntoa     | 3           | 없음              |
| localtime     | 3           | localtime_r     |

> 대부분의 `_r` 접미사 함수들은 reentrant 버전이다.


### 12.7.4 경쟁 상태 (Races)

**경쟁 상태(race)** 는 프로그램의 실행 결과가 스레드의 실행 순서에 따라 달라지는 현상이다.

예시 프로그램:

```c
#define N 4
void *thread(void *vargp);

int main() {
  pthread_t tid[N];
  int i;
  for (i = 0; i < N; i++)
    Pthread_create(&tid[i], NULL, thread, &i);
  for (i = 0; i < N; i++)
    Pthread_join(tid[i], NULL);
}

void *thread(void *vargp) {
  int myid = *((int *)vargp);
  printf("Hello from thread %d\n", myid);
  return NULL;
}
```

출력 결과:

```
Hello from thread 1
Hello from thread 3
Hello from thread 2
Hello from thread 3
```

문제는 main 스레드가 `&i` ==(같은 주소)==를 모든 스레드에 전달한다는 것.
`i`가 증가하는 도중에 스레드가 실행되면 엉뚱한 값을 읽는다.

➡️ 해결법:

![[Screenshot 2025-11-07 at 17.51.22.png]]

스레드 내에서 `Free(vargp);`로 메모리를 해제한다.
이제 각 스레드는 자기 고유한 `int` 복사본을 가진다.


### 12.7.5 교착 상태 (Deadlocks)

세마포어는 또 다른 문제를 야기할 수 있다 — **교착 상태(deadlock)**.

> 교착 상태란, 여러 스레드가 서로가 보유한 자원을 기다리면서
> 아무도 앞으로 진행하지 못하는 상태를 말한다.

예를 들어 두 스레드가 두 개의 세마포어 `s`와 `t`를 사용하고, 각각 다른 순서로 `P(s)`, `P(t)`를 호출하면, 서로가 상대의 락을 기다리며 영원히 멈출 수 있다.


#### Deadlock의 원인 요약

* 금지 구역(forbidden region)이 겹치면 스레드들이 서로를 막는다.
* 실행 궤적이 `deadlock region`에 들어가면 영원히 빠져나오지 못한다.
* 재현이 어렵고, 기계마다 다르게 발생할 수 있다.


#### ✅ 해결 규칙: **뮤텍스 잠금 순서 규칙 (Mutex lock ordering rule)**

> 모든 뮤텍스에 대해 전역적인 순서를 정의하고,
> 각 스레드가 **항상 그 순서대로 락을 획득하고,
> 역순으로 해제하면(deadlock-free)** 교착 상태는 발생하지 않는다.

즉, `s` → `t` 순으로 항상 lock한다면, 다른 스레드도 같은 순서로 lock하면 안전하다.