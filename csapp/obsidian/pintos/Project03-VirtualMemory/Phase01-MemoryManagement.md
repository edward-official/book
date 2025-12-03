![[StructThread.png]]
#### spt를 어떻게 설계하지??
일단 스레드 구조체안에 spt가 있다.
각 프로세스(스레드)마다 spt가 있는 형태이기 때문에 spt의 위치는 적합하다고 생각한다.
그럼 그 내부의 spt는 va가 주어졌을 때 그에 맞는 struct page 주소값이 반환되는 해시 테이블의 형태가 적합한 것 같다.
그러면 우선 만들어야하는게:
1. hash table 형태의 spt를 만든다면 초기화 함수(supplemental_page_table_init)를 먼저 만들어야한다.
2. 초기화 함수에는 hash_init 함수를 써야하기 때문에 hash 함수와 less 함수를 만들어 줘야한다.

#### supplemental_page_table_init
이 함수가 하는 일:
- 특정 프로세스(스레드 구조체)가 가진 페이지 테이블(해시 테이블)을 초기화한다.
- 해시 테이블은 내부적으로 hash 함수와 less 함수가 필요하다.
- 이 두 함수는 우리가 만들고 포인터를 전달해주면 된다.
hash:
- 일반적으로 특정 요소를 해시값으로 바꿔주는 역할을 한다.
- 여기서는 페이지의 사용자 가상 주소를 해시값으로 바꿔주는 역할.
less:
- 일반적으로 해시 테이블의 키를 비교하는 용도로 사용한다.
- 여기서는 페이지의 사용자 가상 주소를 비교하는 역할.

#### spt_find_page
![[HashFind.png]]

일단 당연히 hash_find 함수를 사용해야한다고 생각을 했는데, 함수 인자를 보니 의문이 생겼다.
지금 페이지를 찾으려고 이 함수를 쓰는건데 그 페이지의 멤버(hash_elem)를 어떻게 넘겨주지??
그래서 좀 고민을 하다가 이렇게 함수를 작성했다.

![[OutdatedFind.png]]

간단히 말하면 그냥 spt의 모든 페이지를 순회하면서 동일한 va를 가지는 페이지를 찾으면 리턴하는 방식이었다.
그런데 정훈이에게 물어보니 가짜 페이지를 만들어서 그 페이지에 va만 설정해주고 그 페이지의 리스트 원소를 넘겨주면 된다는 충격적인 조언을 해줬다.

![[FindElement.png]]

그래서 자리로 돌아와서 이게 진짜 가능한 지 코드를 뜯어보니까 어차피 내부적으로 우리가 정의한 less 함수를 통해 가상 주소만 비교하는 식으로 페이지를 찾기 때문에 문제될 것이 없는 상황이었다.
진짜 혼자서만 고민했으면 이런 방법은 생각을 못했을 텐데 역시 좋은 동료를 주위에 둬야하는것같다.

#### spt_insert_page
![[InsertPage.png]]

#### 이제는 프레임을 구현할 시간
일단 페이지 폴트가 발생해야 프레임을 할당받을 상황이 생긴다.
그럼 당연히 spt를 순회하면서 우리가 원하는 페이지를 찾아야할 것이고, 페이지를 올리고 내리기 위해서 아래의 함수를 구현해야할 것이다.

```
struct page_operations {
	bool (*swap_in) (struct page *, void *);
	bool (*swap_out) (struct page *);
	void (*destroy) (struct page *);
	enum vm_type type;
};
```


#### vm_get_frame
커널의 유저 풀에 새로운 물리 프레임을 할당 받아야한다.
만약 자리가 없다면 당연히 기존의 프레임 중 하나를 쫓아내는 과정도 필요할 것이다.
그럼 비워진 프레임이 생길 것이고 그 프레임의 커널 가상 주소와 페이지 포인터를 NULL로 설정하는 과정도 필요할 것이다.

```
static struct frame *
vm_get_frame (void) {
	struct frame *frame = NULL;

	ASSERT (frame != NULL);
	ASSERT (frame->page == NULL);
	return frame;
}
```

프레임을 받을 때 아마 palloc_get_page 함수를 쓸 텐데, 커널의 사용자 풀에 공간이 있는지를 어떻게 확인할까?
아마 반환값이 NULL이면 공간이 없다고 판단하고 eviction을 하는거겠지??

그럼 또 eviction은 어떻게 하지??
일단 프레임 테이블을 구현을 해야겠네..
아마 그러면 프레임 구조체 내부에 리스트 원소를 추가하고 프레임 테이블을 이중 연결 리스트로 구현한 뒤, 리스트의 맨 뒤에 가장 오래전에 사용된 프레임을 유지할 수 있다면 LRU eviction 알고리즘을 사용하기에 적합할 것 같다.
여기서 중요한 건 어떻게 리스트의 맨 뒤에 evict할 프레임을 유지할 것인가이다.
내가 단순하게 생각했을 때는 pml4_get_page 함수를 통해 pml4에서 특정 프레임에 접근하는 시점에 그 프레임의 위치를 프레임 테이블에서 업데이트하면 된다고 느꼈는데 이 로직도 문제가 있었다.
pml4_get_page는 페이지에 실제 접근하지 않고 단순히 특정 가상 주소가 매핑이 되어있는지만 확인하는 함수이기 때문에 하드웨어가 프레임에 접근하는 정확한 순간을 알아낼 수 없다는 이유 때문이었다.
다만 하드웨어가 프레임에 접근할 때 PTE의 A비트를 1로 설정해주기 때문에 타이머 인터럽트가 발생하는 시점이나 evict 직전에 이 비트 값을 바탕으로 프레임 테이블을 업데이트하면 될 것 같다.
하지만 만약에 여러 프레임의 A비트 값이 1이라면 이들이 어떤 순서로 접근되었는 지는 알 수 없다. (즉, 완벽한 LRU는 구현이 안되고 근사 LRU로 구현하는 방식)

```
struct page_operations {
	bool (*swap_in) (struct page *, void *);
	bool (*swap_out) (struct page *);
	void (*destroy) (struct page *);
	enum vm_type type;
};
```

그리고 또 위에 코드를 보면 swap_in, swap_out 동작과 destroy하는 동작도 페이지의 타입에 따라 다르게 설정해야하는 것 같은데 그 이유는 정확이 모르겠다.
그 이유를 알기 위해서는 일단 각 페이지 타입마다 가지는 특성을 이해해야할 것 같다.
분명 페이지 타입마다 가지는 데이터도 다를 것이고, 그 부분에서 아마 차이가 발생하는 것 같다.

#### vm_do_claim_page

```
static bool
vm_do_claim_page (struct page *page) {
	struct frame *frame = vm_get_frame ();
	frame->page = page;
	page->frame = frame;
	if (!pml4_set_page(thread_current()->pml4, page->va, frame->kva, page->is_writable)) {
		page->frame = NULL;
		frame->page = NULL;
		return false;
	}
	return swap_in (page, frame->kva); /* swap the data into the frame that we got previously */
}
```

#### vm_claim_page

```
bool
vm_claim_page (void *va) {
	struct page *page = spt_find_page(&thread_current()->spt, va);
	if (!page) return false;
	return vm_do_claim_page (page);
}
```