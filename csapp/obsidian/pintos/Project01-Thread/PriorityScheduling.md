### 1️⃣ Ready List — 우선순위 기반 정렬

```
bool thread_cmp_priority_desc (const struct list_elem *a, const struct list_elem *b, void *aux UNUSED) {
  const struct thread *ta = list_entry (a, struct thread, elem);
  const struct thread *tb = list_entry (b, struct thread, elem);
  return ta->priority > tb->priority;
}

/* 호출 위치
- thread_yield
- thread_unblock
*/
```
✅ **목표:** 항상 `ready_list.front`가 최고 우선순위 스레드 유지


### 2️⃣ Preemption

```c
void check_preemption (void) {
  if (!list_empty(&ready_list)) {
    struct thread *curr = thread_current();
    struct thread *front = list_entry(list_front(&ready_list), struct thread, elem);
    if (front->priority > curr->priority)
      intr_context() ? intr_yield_on_return() : thread_yield();
  }
}

/* 호출 위치
- thread_unblock
- thread_create
- sema_up
- lock_release
- thread_set_priority
*/
```


✅ **목표:** 새 스레드나 우선순위 변화가 생기면 즉시 선점 검사


### 3️⃣ Priority Donation

```c
struct thread {
  int priority;              // 현재 우선순위 (가변 우선순위)
  int original_priority;     // 원래의 우선순위
  struct lock *waiting_for;  // 현재 기다리는 lock
  struct list donators;      // 나에게 기부한 스레드들 (서로 다른 락을 기다릴 수 있음)
  struct list_elem elem_for_donators; // donators 리스트용 elem
};
```


#### ✅ **Donation 발생**: `lock_acquire()`

```c
if (lock->holder != NULL) {
  curr->waiting_for = lock;
  list_push_back(&lock->holder->donators, &curr->elem_for_donators);
  thread_refresh_priority(lock->holder);
  thread_propagate_donation(lock->holder);
}
sema_down(&lock->semaphore);
curr->waiting_for = NULL;
lock->holder = curr;
```

#### ✅ **Donation 회수**: `lock_release()`

```c
thread_remove_lock_donations(lock);
thread_refresh_priority(thread_current());
lock->holder = NULL;
sema_up(&lock->semaphore);
check_preemption();
```

