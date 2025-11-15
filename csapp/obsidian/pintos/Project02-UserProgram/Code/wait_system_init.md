```
/* wait_system_init()
 *
 * 프로세스 간의 wait 메커니즘(부모가 자식을 기다리는 기능)에 필요한
 * 전역 동기화 객체들을 한 번만 초기화하는 함수.
 *
 * ✔ Pintos는 여러 스레드(프로세스)가 동시에 wait 관련 기능을 사용할 수 있으므로
 *   초기화 과정 자체가 race condition 없이 ‘딱 한 번만’ 실행되어야 한다.
 *
 * ✔ 이 함수는 다음 두 가지를 수행한다:
 *     1) child_wait_sema   : 부모가 자식 종료를 기다릴 때 사용되는 세마포어 초기화
 *     2) child_wait_lock   : wait 관련 공유 데이터 보호용 락 초기화
 *
 * ✔ intr_disable() / intr_set_level() 을 사용하는 이유:
 *     - 인터럽트를 비활성화하여 초기화 과정이 중단되거나
 *       다른 CPU 흐름이 끼어드는 것을 방지함.
 *     - 즉, wait_initialized 플래그 검사 + 초기화 과정 전체를
 *       원자적으로(atomic) 보장하기 위해서다.
 *
 * 요약: wait 시스템이 필요로 하는 세마포어/락을 ‘한 번만’ 안전하게 세팅한다.
 */
static void
wait_system_init (void) {
    /* 인터럽트를 끄고 현재 인터럽트 상태를 old_level에 저장 */
	enum intr_level old_level = intr_disable ();

    /* 아직 초기화되지 않았다면 한 번만 초기화한다 */
	if (!wait_initialized) {
		sema_init (&child_wait_sema, 0);   /* 부모가 자식 종료를 기다릴 세마포어 */
		lock_init (&child_wait_lock);       /* wait 관련 공유 자원 보호용 락 */
		wait_initialized = true;            /* 이후에는 다시 초기화하지 않도록 플래그 설정 */
	}

    /* 원래 인터럽트 상태로 복구 */
	intr_set_level (old_level);
}

```