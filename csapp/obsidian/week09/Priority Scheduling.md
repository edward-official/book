### 1️⃣ **Ready List 정렬 (우선순위 기반 스케줄링 핵심)**

* **ready_list**는 항상 **우선순위(priority)** 가 높은 스레드가 먼저 실행되도록 유지해야 함.
* `thread_unblock()`에서 스레드를 ready list에 추가할 때, **list_insert_ordered()** 를 사용하여 **priority 순으로 삽입**해야 함.
* `next_thread_to_run()`에서는 **리스트의 맨 앞 스레드**를 꺼내 실행시켜야 함.

> 💡 Hint: `list_less_func` 비교 함수로 `priority`를 기준으로 정렬.


### 2️⃣ **현재 스레드보다 높은 우선순위 스레드가 생기면 yield**

* `thread_unblock()`에서 새 스레드를 ready list에 추가한 후,
  그 스레드의 priority가 **현재 실행 중인 스레드보다 높으면 즉시 yield()** 해야 함.
* `thread_set_priority()`로 스스로 우선순위를 낮추는 경우에도,
  **더 높은 우선순위의 스레드가 ready 상태라면 yield()** 해야 함.

```c
if (new_thread->priority > thread_current()->priority)
    thread_yield();
```


### 3️⃣ **우선순위 기부 (Priority Donation)**

#### (1) 기본 아이디어

* 높은 우선순위 스레드가 낮은 우선순위 스레드가 가진 lock을 기다릴 때, **낮은 스레드에게 우선순위를 임시로 기부(donate)** 해야 함.
* lock을 해제하면 **기부가 철회(restore)** 되어야 함.

#### (2) 구현 구조

* 각 스레드 구조체에 다음 필드를 추가:

  ```c
  struct thread {
      int priority;              // 현재 우선순위
      int original_priority;     // 기부 전 원래 우선순위
      struct list donators;      // 자신에게 기부한 스레드 목록
      struct lock *waiting_lock; // 자신이 기다리고 있는 lock
  };
  ```

#### (3) 핵심 함수

* **`donate_priority(struct thread *t)`**

  * `t`가 기다리고 있는 lock의 holder에게 우선순위를 기부.
  * nested donation의 경우 재귀적으로 적용.
  * 깊이 제한(예: 8단계) 설정 가능.

* **`remove_donations_for_lock(struct lock *lock)`**

  * lock 해제 시, 해당 lock을 기다리던 스레드들의 기부를 제거.

* **`refresh_priority()`**

  * 기부가 철회되면 원래 우선순위(`original_priority`)로 복원.
  * 남아 있는 기부 중 최고 우선순위를 반영.


### 4️⃣ **Lock, Semaphore, Condition Variable 연동**

* **Lock을 기다릴 때만 donation 발생**하도록 수정.
* `lock_acquire()`:

  * lock이 이미 누군가에게 점유되어 있다면,
    holder에게 `donate_priority()` 호출.
* `lock_release()`:

  * `remove_donations_for_lock()` 호출 → 기부 철회.
  * `refresh_priority()` 호출 → 원래 우선순위 복원.

> ⚠️ Semaphore나 Condition Variable 자체에는 donation을 구현하지 않아도 됨.
> 하지만 내부적으로 lock을 사용하므로 간접적으로 영향이 있음.


### 5️⃣ **기부 체인 (Nested Donation)**

* 예:
  H(63) → M(31) → L(10)
  H가 M이 가진 lock을 기다리고,
  M이 L이 가진 lock을 기다린다면,
  → L이 H의 우선순위를 간접적으로 받음.

* 구현 시 재귀 호출로 처리:

  ```c
  void donate_priority(struct thread *t) {
      if (depth >= 8) return;
      if (t->waiting_lock && t->waiting_lock->holder) {
          t->waiting_lock->holder->priority = max(t->priority, holder->priority);
          donate_priority(t->waiting_lock->holder);
      }
  }
  ```


### 6️⃣ **thread_set_priority() 주의점**

* 스스로 priority를 변경할 때,

  * donation 중이라면 `original_priority`만 변경.
  * donation이 없다면 실제 `priority` 변경.
* 변경 후, ready list에서 더 높은 priority의 스레드가 있으면 **yield()**.


### 7️⃣ **테스트 통과를 위한 주요 시나리오**

다음 테스트들을 통과해야 정상 구현:

* `priority-change`
* `priority-donate-one`, `priority-donate-multiple`, `priority-donate-nest`
* `priority-donate-sema`, `priority-donate-lower`
* `priority-preempt`, `priority-yield`, `priority-sema`, `priority-condvar`


## ✅ **정리**

| 구현 대상               | 핵심 포인트                    |
| ------------------- | ------------------------- |
| ready_list          | priority 기준 정렬 유지         |
| thread_unblock      | 높은 priority 등장 시 즉시 yield |
| thread_set_priority | priority 변경 후 yield 확인    |
| priority donation   | lock holder에게 기부 + 철회     |
| nested donation     | 재귀적 기부 구현                 |
| donation 관리         | donations 리스트로 관리         |
| lock_release        | donation 철회 후 priority 복원 |

