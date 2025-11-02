응용 프로그램은 `readdir` 계열의 함수를 사용하여 디렉터리 안의 파일 목록을 읽을 수 있다.

```c
#include <sys/types.h>
#include <dirent.h>

DIR *opendir(const char *name);
```

> 성공 시 `DIR*` 포인터 반환, 실패 시 NULL

`opendir`는 경로 이름을 받아 해당 디렉터리에 대한 디렉터리 스트림(directory stream)을 연다.
스트림은 디렉터리 항목들의 정렬된 리스트라고 생각하면 된다.


##### 다음 항목 읽기

```c
#include <dirent.h>

struct dirent *readdir(DIR *dirp);
```

> 성공 시 다음 디렉터리 항목 포인터 반환, 없으면 NULL

각 호출은 `struct dirent` 구조체를 반환한다:

```c
struct dirent {
  ino_t d_ino;          // inode 번호
  char d_name[256];     // 파일 이름
};
```

> `d_name`은 파일 이름, `d_ino`는 파일 위치를 나타낸다.

`readdir`가 오류 시 `errno`를 설정하므로,
끝(EOF)인지 오류인지를 구분하려면 `errno`를 확인해야 한다.


##### 스트림 닫기

```c
#include <dirent.h>
int closedir(DIR *dirp);
```

> 성공 시 0, 실패 시 -1


##### Figure 10.11

```c
#include "csapp.h"

int main(int argc, char argv) {
  DIR *streamp;
  struct dirent *dep;

  streamp = Opendir(argv[1]);
  errno = 0;

  while ((dep = readdir(streamp)) != NULL)
    printf("Found file: %s\n", dep->d_name);

  if (errno != 0)
    unix_error("readdir error");

  Closedir(streamp);
  exit(0);
}
```

> 출력 예:
>
> ```
> Found file: .
> Found file: ..
> Found file: hello.c
> ```
