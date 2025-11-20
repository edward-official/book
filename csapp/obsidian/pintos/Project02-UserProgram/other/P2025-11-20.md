## 사건의 발단
포크 구현을 마치고 exec-missing의 테스트를 해결하고 싶어서 코드를 둘러보니....
어??? 문제가 없어 보이는데??
이런 생각이 들었다.
하지만 로그를 보니 페이지 폴트가 발생하는 상황이었고, 결과적으로 문제 발생 지점은 process_exec.c 파일 내부의 process_cleanup 함수 근방이었다.

```
int
process_exec (void *f_name) {
	char *file_name = f_name;
	bool success;

	/* We cannot use the intr_frame in the thread structure.
	 * This is because when current thread rescheduled,
	 * it stores the execution information to the member. */
	struct intr_frame _if;
	_if.ds = _if.es = _if.ss = SEL_UDSEG;
	_if.cs = SEL_UCSEG;
	_if.eflags = FLAG_IF | FLAG_MBS;

	process_cleanup (); /* We first kill the current context */
	success = load (file_name, &_if); /* And then load the binary */
	/* test 46 fails.. */
	palloc_free_page (file_name);
	if (!success) return -1;
	do_iret (&_if); /* Start switched process. */
	NOT_REACHED ();
}
```

자 이 문제의 process_cleanup이라는 함수는 사용자 프로세스가 가진 페이지 테이블을 날려버리는 역할이었다.


## 내가 이해하지 못했던 지점
#### 테스트 코드도 사용자 프로그램이다.
분명히 내가 이번 프로젝트 시작 시점에 알게되었던 사실이다.
이제는 테스트 코드도 사용자 프로그램이고 이를 우리의 개인 컴퓨터에서 컴파일하고 그 결과물을 이뮬레이터로 삽입한다는 걸 분명 알고있었다.
그걸 몰랐기 때문에 어떤 이유로 페이지 폴트가 나는 지 이해를 할 수가 없었다.

#### 시스템 콜 처리의 맥락
사용자 프로그램(테스트 코드)에서 특정 파일을 실행하고 싶으면 시스템 콜을 통해서 커널 컨텍스트로 전환하고 process_exec를 실행하게 된다.
이 상황에서 다시 process_cleanup이라는 함수를 보자.
이 함수는 사용자 프로그램(테스트 코드)의 맥락에서 특정 프로그램으로 전환하기 위해서 사용자 프로그램(테스트 코드)의 페이지 테이블을 모두 날린다.
새로운 프로그램으로 전환하기 위해서 기존의 페이지 테이블을 모두 삭제하는 것이 합리적인 동작이라는 것은 누구나 동의할 수 있는 부분이다.

#### 만약 원하는 프로그램을 실행할 수 없다면?
예를 들어서 process_exec가 인자로 넘겨받은 파일 이름이 잘못된 경우에, load 함수에서 문제가 생긴다.
그럼 당연히 커널 공간에 생성한 지역 변수(file_name)를 반환하고 그 후에 리턴하는 코드로 넘어가게 된다.
하지만 이런 상황에서 시스템 콜이 리턴하면 돌아가는 곳은 바로 사용자 프로그램(테스트 코드)이다.
그런데 문제는 우리가 이전에 process_cleanup이라는 함수를 통해서 사용자 프로그램(테스트 코드)의 페이지 테이블을 모두 날려버렸다는 것이다.
따라서 리턴하는 동작이 정상적으로 이루어질 수 없고 이미 삭제된 공간으로 돌아가는 과정에서 페이지 폴트가 일어나는 것이었다.

## 문제를 해결하는 방법은?
나는 사용자 프로그램(테스트 코드)으로 돌아가지 않고 실행 흐름을 완전히 끊는 방식을 선택했다.
따라서 다음의 코드가 완성이 되었다.

```
int
process_exec (void *f_name) {
	char *file_name = f_name;
	bool success;

	/* We cannot use the intr_frame in the thread structure.
	 * This is because when current thread rescheduled,
	 * it stores the execution information to the member. */
	struct intr_frame _if;
	_if.ds = _if.es = _if.ss = SEL_UDSEG;
	_if.cs = SEL_UCSEG;
	_if.eflags = FLAG_IF | FLAG_MBS;

	process_cleanup (); /* We first kill the current context */
	success = load (file_name, &_if); /* And then load the binary */
	/* test 46 fails.. */
	palloc_free_page (file_name);
	if (!success) {
		thread_current ()->exit_status = -1;
		thread_exit (); /* If load failed, terminate the process. */
	}
	do_iret (&_if); /* Start switched process. */
	NOT_REACHED ();
}
```

코드 수정 분량으로만 봤을 때는 미미하지만, 이 모든 과정이 나에게는 이 프로젝트의 구조에 대해서 더 깊이 이해할 수 있는 시간이었기 때문에 의미가 있었다고 개인적으로 생각한다.

이렇게 문제를 마주하고 극복하는 과정들이 내가 개발자로서 가장 큰 성취감을 느끼는 순간이라고 생각한다.
앞으로도 이런 문제 상황들을 많이 만나고 더 많은 성장을 할 수 있었으면 좋겠다.
파이팅!!