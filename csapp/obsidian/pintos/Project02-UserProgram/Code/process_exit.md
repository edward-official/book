```
/* process_exit()
 *
 * 현재 스레드(=프로세스)를 종료할 때 호출되는 함수.
 * userprog에서는 "프로세스 = 스레드 1개" 구조이므로
 * 이 함수는 사용자 프로세스가 종료할 때의 모든 정리(cleanup)를 담당한다.
 *
 * 수행 작업 요약:
 *   1) "프로그램이 어떻게 종료되었는지" 출력   (예: echo: exit(0))
 *   2) 부모가 process_wait()으로 대기 중이면 exit status 전달 + 깨움(sema_up)
 *   3) 주소 공간(pml4)과 기타 자원을 정리 (process_cleanup)
 */
void
process_exit (void) {
	struct thread *curr = thread_current ();   /* 현재 종료 중인 스레드 */

	/* (1) 종료 메시지 출력
	 *
	 * curr->name : 실행 파일(스레드) 이름
	 * curr->exit_status : exit(n)의 n 값
	 *
	 * 테스트 출력 형식에 반드시 맞아야 함:
	 *     program_name: exit(status)
	 */
	if (curr->pml4 != NULL)
		printf ("%s: exit(%d)\n", curr->name, curr->exit_status);

	/* (2) 부모가 자식을 기다리는(wait) 중인 경우 처리
	 *
	 *    부모는 process_wait()에서 sema_down()으로 블록되어 있다.
	 *    따라서:
	 *      - 자식이 exit_status를 부모에게 전달해줘야 하고
	 *      - sema_up() 해서 부모를 깨워야 한다.
	 *
	 *    단, 현재 종료하는 프로세스가 부모가 기다리는 자식(child_tid)일 때만 수행.
	 *    (curr->tid != current_child_tid)라면 부모는 이 자식을 기다린 적이 없음.
	 */
	wait_system_init ();                 /* wait 시스템이 초기화되었는지 보장 */
	lock_acquire (&child_wait_lock);

	if (curr->tid == current_child_tid) {
		/* 부모가 기다리기로 한 자식이면 exit status 넘겨주기 */
		child_wait_status = curr->exit_status;

		/* process_wait()에서 block된 부모를 깨워서 실행 재개시킴 */
		sema_up (&child_wait_sema);
	}

	lock_release (&child_wait_lock);

	/* (3) 주소 공간 및 모든 리소스 정리
	 *
	 *    - pml4 해제
	 *    - open file 정리
	 *    - frame/VM 관련 구조 해제
	 *
	 *    모든 Pintos userprog clean-up은 이 함수가 담당함.
	 */
	process_cleanup ();
}

```