모든 리눅스 파일에는 ==타입(type)==이 있다.

* 일반 파일 (regular file):
  임의의 데이터를 포함한다.
  텍스트 파일(ASCII 또는 유니코드 문자)과 바이너리 파일로 구분되지만, 커널은 구분하지 않는다.
  텍스트 파일은 여러 텍스트 라인(text lines)으로 이루어지며, 각 라인은 `\n` (LF, 0x0a) 문자로 끝난다.

* 디렉터리 (directory):
  링크들의 배열이다.
  각 링크는 파일 이름을 다른 파일(또는 디렉터리)에 연결한다.
  디렉터리는 항상 두 개의 항목을 포함한다:

  1. `.` : 자기 자신
  2. `..` : 부모 디렉터리

  디렉터리를 생성할 때는 `mkdir`, 삭제할 때는 `rmdir`을 사용한다.

* 소켓 (socket):
  네트워크 통신에 사용되는 파일.

그 밖에도 named pipes, symbolic links, character/block devices 등이 있다.

##### 디렉터리 계층 구조

리눅스는 모든 파일을 하나의 디렉터리 계층(directory hierarchy) 안에 둔다.
최상위 디렉터리는 `/` (root directory)이며, 모든 파일은 그 하위에 존재한다.

```
/
├── bin/
│   └── bash
├── dev/
│   └── tty1
├── etc/
│   ├── group
│   └── passwd
├── home/
│   ├── droh/
│   │   └── hello.c
│   └── bryant/
│       ├── stdio.h
│       └── unistd.h
└── usr/
    ├── include/
    ├── sys/
    └── vim
```
