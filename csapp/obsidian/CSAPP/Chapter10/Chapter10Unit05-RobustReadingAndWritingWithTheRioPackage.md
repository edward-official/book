이 절에서는 RIO(Robust I/O) 패키지를 소개한다.
RIO는 ==short count 문제를 자동으로 처리==하며,
네트워크 프로그램 등에서 안전하고 효율적인 I/O를 제공한다.

RIO는 두 가지 종류의 함수로 구성된다:

1. 비버퍼드(Unbuffered) I/O 함수

   * 애플리케이션 레벨 버퍼링 없이 메모리와 파일 간 직접 전송
   * 주로 ==이진(binary) 데이터==에 적합

2. 버퍼드(Buffered) I/O 함수

   * 파일 내용을 내부 버퍼에 캐싱하여 효율적으로 텍스트 라인을 읽음
   * `printf` 등의 표준 I/O 함수와 유사하지만 ==스레드 안전(thread-safe)==


#### 10.5.1 RIO 비버퍼드 입출력 함수

```c
#include "csapp.h"

ssize_t rio_readn(int fd, void *usrbuf, size_t n);
ssize_t rio_writen(int fd, void *usrbuf, size_t n);
```

* `rio_readn`: 파일 디스크립터 `fd`에서 최대 `n`바이트를 읽어 `usrbuf`에 저장
* `rio_writen`: `usrbuf`의 내용을 `fd`로 최대 `n`바이트 씀

`rio_readn`은 EOF에 도달하면 short count를 반환할 수 있으며, `rio_writen`은 short count를 반환하지 않는다.
이 두 함수는 동일한 디스크립터에서 번갈아 호출될 수 있다.


##### Figure 10.4

`rio_readn`과 `rio_writen`은 신호 처리 중단(`EINTR`)을 자동으로 복구한다.

```c
ssize_t rio_readn(int fd, void *usrbuf, size_t n) {
  size_t nleft = n;
  ssize_t nread;
  char *bufp = usrbuf;

  while (nleft > 0) {
    if ((nread = read(fd, bufp, nleft)) < 0) { // 아 이걸 보고 재진입 가능하다고 하는건가??
      if (errno == EINTR) nread = 0;  // 신호에 의해 중단 시 재시도
      else return -1;
    } else if (nread == 0) break;     // EOF
    nleft -= nread;
    bufp += nread;
  }
  return (n - nleft);
}
```

`rio_writen`도 동일한 방식으로 작동하지만 쓰기(write)를 수행한다. ==(아 이걸 보고 재진입 가능하다고 하는건가??)==


#### 10.5.2 RIO 버퍼드 입력 함수

텍스트 파일에서 줄 단위(line-by-line)로 데이터를 읽을 때는 매번 `read`를 호출하는 것은 비효율적이다.
그래서 내부 읽기 버퍼(read buffer)를 두고 필요할 때마다 커널에서 데이터를 채워오는 방식으로 개선한다.

```c
#include "csapp.h"

void rio_readinitb(rio_t *rp, int fd);
ssize_t rio_readlineb(rio_t *rp, void *usrbuf, size_t maxlen);
ssize_t rio_readnb(rio_t *rp, void *usrbuf, size_t n);
```

* `rio_readinitb`: ==버퍼 구조체== `rio_t`를 파일 디스크립터 `fd`에 연결
* `rio_readlineb`: 텍스트 한 줄(`\n` 포함)을 읽음
* `rio_readnb`: 이진 데이터를 `n`바이트까지 읽음


##### Figure 10.6

```c
#define RIO_BUFSIZE 8192

typedef struct {
  int rio_fd;                // 파일 디스크립터
  int rio_cnt;               // 내부 버퍼에 남은 바이트 수
  char *rio_bufptr;          // 다음에 읽을 바이트의 위치
  char rio_buf[RIO_BUFSIZE]; // 내부 버퍼
} rio_t;
```

`rio_readinitb`는 버퍼를 초기화한다.

```c
void rio_readinitb(rio_t *rp, int fd) {
  rp->rio_fd = fd;
  rp->rio_cnt = 0;
  rp->rio_bufptr = rp->rio_buf;
}
```


##### Figure 10.7

`rio_read` 함수는 내부 버퍼가 비어 있으면 `read()`를 호출하여 채운 뒤, 사용자 버퍼로 데이터를 복사한다.

```c
static ssize_t rio_read(rio_t *rp, char *usrbuf, size_t n) {
  while (rp->rio_cnt <= 0) {
    rp->rio_cnt = read(rp->rio_fd, rp->rio_buf, sizeof(rp->rio_buf));
    if (rp->rio_cnt < 0 && errno != EINTR) return -1;
    else if (rp->rio_cnt == 0) return 0;  // EOF
    rp->rio_bufptr = rp->rio_buf;
  }
  int cnt = n < rp->rio_cnt ? n : rp->rio_cnt;
  memcpy(usrbuf, rp->rio_bufptr, cnt);
  rp->rio_bufptr += cnt;
  rp->rio_cnt -= cnt;
  return cnt;
}
```


##### Figure 10.8

`rio_readlineb`는 `rio_read`를 반복 호출하여 줄 끝(`\n`)이 나올 때까지 읽는다.
`rio_readnb`는 지정된 크기만큼 이진 데이터를 읽는다.


##### Figure 10.5

```c
#include "csapp.h"

int main(int argc, char argv) {
  int n;
  rio_t rio;
  char buf[MAXLINE];

  Rio_readinitb(&rio, STDIN_FILENO);
  while ((n = Rio_readlineb(&rio, buf, MAXLINE)) != 0)
    Rio_writen(STDOUT_FILENO, buf, n);
}
```

이 프로그램은 표준 입력에서 한 줄씩 읽어 표준 출력으로 복사한다.


##### (참고) RIO 패키지의 기원

RIO 함수들은 W. Richard Stevens의 고전적인 네트워크 프로그래밍 교재에 나오는
`readline`, `readn`, `writen` 함수에서 영감을 받아 만들어졌다.
`rio_readn`과 `rio_writen`은 Stevens의 것과 동일하지만,
`readline` 함수는 두 가지 한계가 있었다:

1. `readline`은 버퍼링되고, `readn`은 비버퍼링되어 둘을 같은 디스크립터에 사용할 수 없었다.
2. `readline`은 `static` 버퍼를 사용하기 때문에 스레드 안전하지 않았다.

RIO 패키지는 이 두 가지 문제를 해결한 `rio_readlineb`와 `rio_readnb`를 제공하며,
이 둘은 서로 호환되고 스레드 안전하다.
