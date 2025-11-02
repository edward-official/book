파일을 열거나 새로 생성하려면 `open` 함수를 사용한다.

```c
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>

int open(char *filename, int flags, mode_t mode);
```

반환값: 성공 시 새 파일 디스크립터, 실패 시 -1
가장 작은 사용 가능한 디스크립터 번호를 반환한다.

##### flags 인자

* `O_RDONLY` : 읽기 전용
* `O_WRONLY` : 쓰기 전용
* `O_RDWR` : 읽기/쓰기

예시:

```c
fd = Open("foo.txt", O_RDONLY, 0);
```

추가 플래그:

* `O_CREAT` : 파일이 없으면 새로 생성 ==(플래그를 여러 개 줄 수도 있나??)==
* `O_TRUNC` : 이미 있으면 내용을 지움
* `O_APPEND` : 항상 파일 끝에 추가

##### 접근 권한 (Access permissions)

파일 생성 시 `mode` 인자는 접근 권한을 지정한다. ==(이게 어디에 쓰는지 정확히 모르겠는데??)==
다음은 `<sys/stat.h>`에 정의된 비트 마스크들이다:

| Mask    | Description |
| ------- | ----------- |
| S_IRUSR | 사용자 읽기      |
| S_IWUSR | 사용자 쓰기      |
| S_IXUSR | 사용자 실행      |
| S_IRGRP | 그룹 읽기       |
| S_IWGRP | 그룹 쓰기       |
| S_IXGRP | 그룹 실행       |
| S_IROTH | 기타 사용자 읽기   |
| S_IWOTH | 기타 사용자 쓰기   |
| S_IXOTH | 기타 사용자 실행   |

##### umask

프로세스는 `umask`를 통해 ==새 파일의 기본 권한을 제한==할 수 있다.
파일 생성 시 실제 권한은 `mode & ~umask`로 결정된다.

예시:

```c
#define DEF_MODE  S_IRUSR|S_IWUSR|S_IRGRP|S_IROTH
#define DEF_UMASK S_IWGRP|S_IWOTH

umask(DEF_UMASK);
fd = Open("foo.txt", O_CREAT|O_TRUNC|O_WRONLY, DEF_MODE);
```

이 코드는 소유자는 읽기/쓰기 가능, 다른 사용자는 읽기만 가능한 파일을 만든다.

##### 파일 닫기

```c
#include <unistd.h>

int close(int fd);
```

반환값: 0 (성공), -1 (오류)
이미 닫힌 파일 디스크립터를 다시 닫으면 오류가 발생한다.
