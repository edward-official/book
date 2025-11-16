## 스레드가 할당받는 페이지는 대체 어떤 놈인가??
```
tid_t
thread_create (const char *name, int priority,
		thread_func *function, void *aux) {
	struct thread *t;
	tid_t tid;

	ASSERT (function != NULL);

	/* Allocate thread. */
	t = palloc_get_page (PAL_ZERO); /* 🔥 edward: Thread gets page */
	if (t == NULL)
		return TID_ERROR;

	/* Initialize thread. */
	init_thread (t, name, priority);
	tid = t->tid = allocate_tid ();
	if (thread_mlfqs) {
		t->nice = thread_current ()->nice;
		t->recent_cpu = 0;
		mlfqs_update_priority (t);
	}

	/* Call the kernel_thread if it scheduled.
	 * Note) rdi is 1st argument, and rsi is 2nd argument. */
	t->tf.rip = (uintptr_t) kernel_thread;
	t->tf.R.rdi = (uint64_t) function;
	t->tf.R.rsi = (uint64_t) aux;
	t->tf.ds = SEL_KDSEG;
	t->tf.es = SEL_KDSEG;
	t->tf.ss = SEL_KDSEG;
	t->tf.cs = SEL_KCSEG;
	t->tf.eflags = FLAG_IF;

	/* Add to run queue. */
	thread_unblock (t);
	check_preemption();
	return tid;
}
```

#### 자 일단 이 함수의 동작을 이해해보자.
1. 페이지 하나를 할당받는다.
2. 페이지에 스레드 구조체를 설정한다.
3. 나머지 공간을 스레드 스택으로 활용하기 위해서 스택 포인터를 설정해준다.

#### 페이지 할당 함수의 내부 동작도 알아보자.
```
void *
palloc_get_page (enum palloc_flags flags) {
	return palloc_get_multiple (flags, 1);
}
```
커널의 페이지 할당기에서 한장의 물리 페이지를 받아오는 헬퍼 함수
내부적으로 palloc_get_multiple에 page_cnt를 1로 넘겨주는 형태
옵션:
- PAL_USER: 사용자 풀에서 페이지를 가져옵니다. 이 비트를 빼면 기본적으로 커널 풀에서 할당합니다.
- PAL_ZERO: 새 페이지를 0으로 채워서 돌려줍니다. 초기화를 원하지 않으면 빼두면 됩니다.
- PAL_ASSERT: 할당할 페이지가 없을 때 NULL을 돌려주는 대신 커널 패닉을 일으켜 즉시 중단합니다.
- 0: 위 비트를 아무 것도 주지 않은 기본값입니다. 즉 커널 풀에서 페이지 하나를 가져오되(zero-fill 없음) 실패 시 NULL을 반환합니다.

