```
/* Switch the current execution context to the f_name.
 * Returns -1 on fail. */
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

	/* We first kill the current context */
	process_cleanup ();

	/* And then load the binary */
	success = load (file_name, &_if);

	/* If load failed, quit. */
	palloc_free_page (file_name);
	if (!success)
		return -1;

	/* Start switched process. */
	do_iret (&_if);
	NOT_REACHED ();
}
```
#### 배경
이 함수에서 load 함수를 부르기 때문에 이 함수에 대해서 이해를 해야 argument passing 과제의 핵심이 되는 load 함수의 동작을 살펴볼 수 있을 것이라고 판단.

#### 내부 동작
- process_cleanup 메서드를 통해서 **현재의 스레드 컨텍스트를 버린다**.
- load 함수를 호출해 **현재 스레드의 컨텍스트를 특정 ELF 파일 프로그램으로** 바꿔준다.
- do_iret 함수를 호출해 **사용자 모드를 실행**한다.

#### 결론
이 함수는 결국 형성된지 얼마 되지않아 내부 동작이 정해지지 않은 스레드에 우리가 실행하고자 하는 ELF 파일의 내용을 올려서 그 ELF 유저 프로그램이 동작할 수 있도록 컨텍스트를 전환해주는 것

#### 왜 함수 내부에서 인터럽트 구조체 변수를 선언해야하는가?
결론적으로는 내부에서 스레드의 컨텍스트가 바뀌기 때문에 인터럽트 구조체가 필요하고, **스레드가 원래 구조체 내부에 갖고 있는 인터럽트 구조체는 스케줄러가 쓰는 것**이기 때문에, 이 구조체 대신에 process_exec 함수 내부에서 **지역 변수로 별도의 구조체**를 만들어서 쓰는 것.
또한 이 지역 변수를 쓰는 이유는 **일반적인 인터럽트 상황과는 다르다**. (일반적으로는 인터럽트 후 복귀할 때 컨텍스트를 복구해주기 위한 용도.)
이 지역 변수의 목적은 **새로운 ELF 파일이 가질 실행 흐름의 컨텍스트 초기값을 설정**해주기 위한 것.