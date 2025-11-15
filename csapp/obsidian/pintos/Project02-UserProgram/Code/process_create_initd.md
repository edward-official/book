```
/* process_create_initd()
 *
 * user program(사용자 프로그램)을 실행하는 **최초의 커널 스레드(initd)**를 만드는 함수.
 * file_name은 "prog arg1 arg2 ..." 형태의 전체 커맨드 라인이다.
 *
 * 주요 흐름:
 *   1) file_name을 복사해 load()에서 race 없이 파싱할 수 있도록 준비
 *   2) thread_name을 file_name의 첫 단어로 추출해 스레드 이름으로 사용
 *   3) thread_create()로 initd 스레드 생성
 *   4) wait 시스템 초기화 + 부모가 자식 상태를 받을 수 있도록 변수 설정
 */
tid_t
process_create_initd (const char *file_name) {
	char *fn_copy;
	tid_t tid;

	/* file_name 전체를 복사한다.
	 * load()에서 인자를 파싱할 때 원본 file_name이 바뀌면 race가 발생하므로
	 * 독립적인 페이지 버퍼에 복사해 안전하게 사용한다. */
	fn_copy = palloc_get_page (0);
	if (fn_copy == NULL)
		return TID_ERROR;
	strlcpy (fn_copy, file_name, PGSIZE);

	/* ---- 스레드 이름(thread_name) 추출 ----
	 * Pintos의 스레드는 이름이 짧아야 하므로 file_name의 첫 번째 토큰만 사용한다.
	 * 예) "echo x y" → thread_name = "echo" */
	char thread_name[16];
	strlcpy (thread_name, file_name, sizeof thread_name);

	/* 첫 공백을 찾아 '\0'로 바꿔 첫 단어만 스레드 이름으로 사용 */
	char *space = strchr (thread_name, ' ');
	if (space != NULL)
		*space = '\0';

	/* 스레드 생성 및 wait 관련 초기화는 atomic해야 하므로 인터럽트 비활성화 */
	enum intr_level old_level = intr_disable ();

	/* ---- 사용자 프로그램을 실행할 initd 스레드 생성 ----
	 * - thread_name  : 스레드 이름
	 * - PRI_DEFAULT  : 디폴트 우선순위
	 * - initd        : 스레드 시작 함수 (내부에서 process_exec() 호출)
	 * - fn_copy      : 전체 커맨드 라인 문자열 (load에서 사용됨)
	 */
	tid = thread_create (thread_name, PRI_DEFAULT, initd, fn_copy);

	if (tid == TID_ERROR) {
		/* 스레드 생성 실패 → file_name 복사본 메모리 해제 */
		palloc_free_page (fn_copy);
	}
	else {
		/* ---- 스레드 생성 성공 시 자식 프로세스 wait 초기화 ---- */

		/* wait 시스템이 한 번만 초기화되도록 보장 */
		wait_system_init ();

		/* 현재 부모가 기다릴 자식의 tid 기록 */
		current_child_tid = tid;

		/* 자식의 exit status를 부모가 나중에 process_wait()에서 받을 것 */
		child_wait_status = -1;

		/* 자식이 이미 종료했는지 여부: 아직은 false */
		child_wait_in_use = false;
	}

	/* 인터럽트 상태 복구 */
	intr_set_level (old_level);

	return tid;
}

```