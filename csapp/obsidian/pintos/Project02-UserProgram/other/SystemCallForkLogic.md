#### process_create_initd
동작 순서:
- 현재 스레드의 스레드 구조체 정보를 사용 가능한 상태로 세팅한다.
- 유저 프로세스를 만들기 위해서 ==초기화 구조체==를 만든다. (파일 이름 + ==대기 구조체==)
- ==초기화 구조체==를 넘겨주면서 유저 프로세스를 생성한다.
- ==현재 스레드의 자녀 리스트에 만들어진 대기 구조체를 추가한다.==

#### 부모가 자식 리스트에서 특정 자식을 삭제하는 시점
- 커널 부모가 process_wait 함수 호출 (remove_child_wait_status)
- 포크 부모가 포크 성공시 process_exit에서 (release_child_waits)
- 포크 부모가 포크 실패시 (remove_child_wait_status)

#### 커널 부모가 유저 프로그램 실행 시 struct wait_status 해제 과정
- 부모가 process_wait 함수 호출 (wait_status_release)
- 자식이 process_exit 함수 호출 (wait_status_release)

#### 포크가 성공하는 경우에 struct wait_status 해제 과정
- 자식이 process_exit 함수 호출 (wait_status_release)
- 부모가 process_exit 함수 호출 (wait_status_release)

#### 포크가 실패하는 경우에 struct wait_status 해제 과정
- 부모가 process_fork에서 wait_status_release 함수 호출
- 부모가 process_exit에서 wait_status_release 함수 호출


카운트 값이 0이되면 구조체는 즉시 해제된다.


#### 부모 입장에서 대기 구조체의 역할
- 부모는 자식의 아이디를 알 수 있다.
- 종료 코드와 종료 여부를 알 수 있다.
- 참조 수를 이용해 메모리 해제 시점도 조절한다. (부모/자식 공통).
- 동기화 (부모/자식 공통).

#### 자식 입장에서 대기 구조체의 역할
- 부모를 깨울 수 있다.
- 참조 수를 이용해 메모리 해제 시점도 조절한다. (부모/자식 공통).
- 동기화 (부모/자식 공통).

#### 대기 구조체의 멤버
- 부모의 자녀 리스트에 들어가기 위해서 리스트 원소
- 부모를 깨울 수 있도록 하는 세마포어
- 이 구조체 멤버에 동시 접근할 수 없도록 하기 위한 락
- 이 구조체를 받은 자식 프로세스의 아이디
- 이 구조체를 참조하는 수
- 자식이 이미 종료했는지에 관한 불리언 멤버
