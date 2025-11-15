```
/* process_wait(child_tid)
 *
 * 부모 프로세스가 자식 프로세스(child_tid)의 종료를 기다리고,
 * 해당 자식의 exit status를 반환하는 함수.
 *
 * Pintos userprog의 단순화된 wait 모델에서는
 *   - 부모는 동시에 오직 1개의 자식만 기다릴 수 있고,
 *   - 이미 wait한 자식을 다시 wait하면 -1을 반환한다.
 *
 * 이 함수는 다음 순서로 동작한다:
 *   1) wait 시스템이 초기화되어 있는지 보장
 *   2) child_tid가 현재 부모가 기다릴 수 있는 자식인지 검사
 *   3) 자식을 기다리는(wait) 중이라는 표시(child_wait_in_use)를 설정
 *   4) child_wait_sema에서 block → 자식이 종료될 때까지 기다림
 *   5) 자식이 process_exit()에서 sema_up 해주면 깨어남
 *   6) exit status 읽고 정리한 뒤 반환
 */
int
process_wait (tid_t child_tid) {
    /* wait 시스템 전역 리소스(sema/lock)가 한 번만 초기화되도록 보장 */
	wait_system_init ();

    /* ---- (1) 자식 검증 단계 ---- */
	lock_acquire (&child_wait_lock);

	/* 현재 기다릴 수 있는 자식인지 확인:
	 *   - child_tid가 current_child_tid(현재 추적 중인 자식)와 다르면 잘못된 자식
	 *   - 이미 wait 중(child_wait_in_use==true)이면 두 번 wait하므로 잘못된 호출
	 */
	if (child_tid != current_child_tid || child_wait_in_use) {
		lock_release (&child_wait_lock);
		return -1;   /* 잘못된 wait → -1 반환 */
	}

	/* 이제 이 자식을 기다리는 중이라는 표시를 설정 */
	child_wait_in_use = true;
	lock_release (&child_wait_lock);

    /* ---- (2) 자식이 종료될 때까지 기다림 ---- */
	/* 자식이 process_exit()에서 sema_up(&child_wait_sema) 할 때까지 block됨 */
	sema_down (&child_wait_sema);

    /* ---- (3) 자식의 종료 정보를 회수하고 정리 ---- */
	lock_acquire (&child_wait_lock);

	/* process_exit()에서 기록된 exit status를 읽어온다. */
	int status = child_wait_status;

	/* 이 자식은 더 이상 기다릴 수 없으므로 상태 초기화 */
	current_child_tid = TID_ERROR;
	child_wait_in_use = false;

	lock_release (&child_wait_lock);

	/* ---- (4) 자식의 exit status 반환 ---- */
	return status;
}

```