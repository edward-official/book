응용 프로그램은 `stat`과 `fstat` 함수를 사용해 파일의 메타데이터(metadata)를 조회할 수 있다.

```c
#include <unistd.h>
#include <sys/stat.h>

int stat(const char *filename, struct stat *buf);
int fstat(int fd, struct stat *buf);
```

> 반환값: 성공 시 0, 실패 시 -1

* `stat`은 파일 이름을 입력으로 받아 그 파일의 정보를 `struct stat` 구조체에 채운다.
* `fstat`은 파일 디스크립터를 인자로 받아 동일한 정보를 가져온다.

이 중에서 웹 서버 등을 구현할 때 자주 사용하는 필드는
`st_mode` (파일 타입 및 접근 권한)과 `st_size` (파일 크기)이다.


##### 주요 필드

`st_size`는 파일 크기를 바이트 단위로 저장한다.
`st_mode`는 파일 타입과 접근 권한 비트를 모두 포함하며,
매크로를 통해 쉽게 확인할 수 있다.

```c
S_ISREG(m)   // 일반 파일인가?
S_ISDIR(m)   // 디렉터리인가?
S_ISSOCK(m)  // 소켓인가?
```


##### Figure 10.9

```c
struct stat {
  dev_t     st_dev;     // 디바이스 ID
  ino_t     st_ino;     // inode 번호
  mode_t    st_mode;    // 보호 모드 및 파일 타입
  nlink_t   st_nlink;   // 하드 링크 개수
  uid_t     st_uid;     // 소유자 UID
  gid_t     st_gid;     // 소유 그룹 GID
  dev_t     st_rdev;    // 디바이스 타입 (특수 파일일 경우)
  off_t     st_size;    // 총 크기 (바이트 단위)
  unsigned long st_blksize;  // I/O 블록 크기
  unsigned long st_blocks;   // 할당된 블록 수
  time_t    st_atime;   // 마지막 접근 시각
  time_t    st_mtime;   // 마지막 수정 시각
  time_t    st_ctime;   // 마지막 상태 변경 시각
};
```


##### Figure 10.10

```c
#include "csapp.h"

int main (int argc, char argv) {
  struct stat stat;
  char *type, *readok;

  Stat(argv[1], &stat);

  if (S_ISREG(stat.st_mode))
    type = "regular";
  else if (S_ISDIR(stat.st_mode))
    type = "directory";
  else
    type = "other";

  if (stat.st_mode & S_IRUSR)
    readok = "yes";
  else
    readok = "no";

  printf("type: %s, read: %s\n", type, readok);
  exit(0);
}
```

> 출력 예:
> `type: regular, read: yes`
