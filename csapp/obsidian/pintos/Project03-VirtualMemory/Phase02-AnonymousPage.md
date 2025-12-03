#### PAGE INITIALIZATION FLOW
kernel receives a new page request:
- vm_alloc_page_with_initializer
- return to the user program
user program tries to access a page which it believes to possess a frame:
- the page fault handling procedure
- uninit_initialize == page_initializer(anon_initializer, file_backed_initializer) + vm_initializer

#### PAGE CYCLE
- initialize
- page_fault
- lazy load
- swap in
- swap out
- destroy

#### LAZY LOADING FOR EXECUTABLE
지연 적재에서는 프로세스가 실행되면 일단 즉시 필요한 부분만 메인 메모리에 적재합니다.
또한 모든 페이지는 일단 `VM_UNINIT` 타입으로 생성됩니다.
이 방식은 오버헤드를 줄일 수 있습니다.

우리는 이 지연 적재 방식을 지원하기 위해서 `VM_UNINIT`이라는 페이지의 타입을 도입합니다.
또한 우리는 `VM_UNINIT` 타입을 지원하기 위해서 `struct uninit_page`라는 구조체도 지원하며, 이 타입의 페이지를 생성(creating) / 초기화(initializing) / 파괴(destroying)하는 함수도 지원하고 있습니다.
우리는 이 함수를 완성해야할 것입니다.

![[Bogus.png]]

페이지 폴트가 발생하면 페이지 폴트 핸들러는 `vm_try_handle_fault` 함수로 제어권을 전달합니다.
`vm_try_handle_fault`는 우선 해당 페이지 폴트가 유효한지 먼저 확인합니다.
만약 그 페이지 폴트가 유효한(bogus) 폴트라면 해당하는 페이지에 내용을 로드하고 다시 사용자 프로그램으로 제어를 반납합니다.

유효한 페이지 폴트에는 세가지 종류가 있습니다.
1. lazy-loaded page
2. swapped-out page
3. write-protected page

일단 우리는 lazy-loaded page의 경우만 고려합니다.
만약 지연 적재를 위한 페이지 폴트가 발생하면 커널은 세그먼트를 지연 적재하기 위해 `vm_alloc_page_with_initializer`에 의해 설정된 ==initializers 중 하나==를 호출합니다.
우리는 `lazy_load_segment`, `vm_alloc_page_with_initializer`를 구현해야합니다.
또한 우리는 전달된 `VM_TYPE`에 맞는 적절한 initializer를 찾아서 가져와야하고 그 ==initializer를 통해서 `uninit_new`를 호출==해야합니다.

#### 프레임/페이지 지연 적재를 구현해봅시다
지연 적재(Lazy Loading)란 내가 이해한 바로는 `spt`에만 페이지를 먼저 할당하고 실제 물리 프레임은 실제 필요한 순간(page fault)에만 할당하는 방법인 것 같다.
또한 프레임을 할당하면 당연히 그 프레임에 데이터를 적재하는 과정도 자연스럽게 따라올 것이다.

![[Page.png]]

#### `vm_alloc_page_with_initializer`

![[LoadSegment.png]]

일단 간단히 말하면 `vm_alloc_page_with_initializer`라는 함수는 새로운 페이지(`VM_UNINIT`)를 생성하고 `spt`에 올리는 함수이다.
페이지 폴트 핸들러는 호출의 체인을 따라서 `swap_in`을 호출하고 마침내 `uninit_initialize`에 도달하게 된다.
우선 이 `uninit_initialize` 함수는 완성되어 있지만 우리의 코드 설계에 따라 수정해야할 수도 있다.

그래서 이 함수가 해야하는 일은:
- 새 페이지 할당 (`UNINIT`)
- 할당된 페이지의 기본 설정 (`type`, `va`, `is_writable`, ..)
- `spt`에 등록

그런데 여기서 궁금증:
- Q: `page`는 어떻게 할당하지? `palloc_get_page`는 실제 프레임을 받아오는 함수니까 여기서 쓰는 건 아닐거고..
  A: 어쨋든 `spt`도 RAM에 보관될 거니까 `malloc`이나 `calloc`을 쓰면 될 듯하다. (아마??)

- Q: `vm_alloc_page_with_initializer` 함수가 받은 `type` 인자는 어디로 가는거지??
  A: `union`의 `struct uninit_page`에 있는 `type`에 저장된다.

- Q: `vm_alloc_page_with_initializer` 함수에서 `struct page_operations`까지 정리해줘야 하나??
  A: `uninit_new` 함수가 알아서 해준다.

- Q: `spt`에는 어떻게 등록하지?
  A: `spt_insert_page` 함수를 써라.
![[UninitializedPage.png]] 

- Q: uninit_page 구조체에서 vm_initializer과 page_initializer의 차이점이 뭐지??
  A: 음 내가 대략 생각하기에 page_initializer는 타입별로 달라지는 operations과 추가적인 메타 데이터를 세팅하는 반면, vm_initializer는 데이터 적재를 담당하는 것 같다.

- Q: uninit_page 구조체에서 vm_initializer과 page_initializer의 차이점이 뭐지??
  A: page_initializer는 타입별로 달라지는 operations과 추가적인 메타 데이터를 세팅하는 반면, vm_initializer는 데이터 적재를 담당하는 것 같다.

- Q: 그러면 uninitialized page는 swap_in와 같은 struct page_operations의 함수가 필요없는건가?
  A: 음 uninit_new 함수에서 설정한 uninit_ops를 보면 여기서 swap_in이 page_initializer와 vm_initializer를 모두 호출하는 형태이다.

- Q: uninit_initialize가 이해가 안돼... 왜 page_initializer를 먼저 호출하고 vm_initializer를 호출하는 거지??
  A: page_initializer가 먼저 타입을 확정해주고 그에 맞는 operations 등을 세팅해줘야 vm_initializer 호출 자체가 가능해지는 구조인거같다. 간단히 말하면 page_initializer는 타입 전환을 하는 역할이고, vm_initializer는 데이터를 적재하는 역할인 거라고 볼 수 있다.

- Q: uninitialized page는 타입이 확정되지 않은 페이지인거고 initialized page는 타입이 확정된 페이지인거겠지???
  A: 정답!

- Q: 어떤 페이지(프레임)가 스왑아웃되면 페이지 구조체에서 어떤 부분이 변경될까?? (일단 당연한건 프레임 포인터가 NULL이 되겠지.)
  A: 타입별 메타 필드에 있는 스왑 슬롯 인덱스, 파일 오프셋, 더티 플래그가 갱신된다고 한다. (일단은 이해가 완벽히는 안되니까 넘어가. 🚨🚨🚨🚨)

- Q: 그리고 왜 uninit_initialize에서 init ? init (page, aux) : true을 하는 거지?? vm_initializer은 무조건 정의되어있는거 아닌가?
  A: 타입만 확정하면 되고, 추가 데이터 적재가 필요 없는 경우에는 vm_initializer가 필요없다고 하는데 이해가 안돼... 🚨🚨🚨🚨 (뭐 bss 정도는 이해가 되는데, 흠... 찝찝해 anonymous인 경우에도 vm_initializer가 필요한 경우도 있다고 하고... 에라 모르겠다~)

#### `uninit_initialize`
최초로 발생한 페이지 폴트에 의해서 페이지를 초기화합니다.
일단 `page_initializer`가 페이지 타입 관련 설정을 수행하고 그 후에 `vm_initializer`가 지연 적재를 수행하는 것 같다.
앞에서 설명했듯이 이 함수는 완성되어 있지만 우리의 설계에 따라서 이 코드를 수정해야할 수도 있다.
또한 `vm_anon_init`과 `anon_initializer`도 필요에 따라 수정해야할 수 있다.

#### `vm_anon_init`
![[vm_anon_init.png]]
진짜 뭐가 뭔지를 모르겠다:
- Q: 익명 시스템의 서브시스템을 초기화하는 함수라고 하는데 서브시스템이 뭐지??
  A: 아마 익명 페이지를 관리하기 위한 전역 상태(스왑 디스크와 비트맵, 락, ..)라고 생각하면 될 것 같다.

#### `anon_initializer`
이 함수는 우선 익명 페이지를 위한 핸들러(`page->operations`)를 세팅한다.
우리는 `anon_page`의 내용을 업데이트해야한다. (현재는 비어있다.)

우리는 `load_segment`와 `lazy_load_segment`를 구현해야한다.
디스크에 있는 ELF 실행파일(executables)는 커널이 페이지 폴트를 잡아낼 때만 RAM에 적재되어야한다.

우리는 프로그램 로더의 핵심 부분(`load_segment`의 반복문)을 수정해야한다.
매 반복에서 vm_alloc_page_with_initializer를 호출해서 pending page object(`VM_UNINIT`)을 생성한다. (실제 적재가 수행되는 시점은 페이지 폴트가 발생할 때.)

#### `load_segment`
이 함수는 우선 파일에서 읽어야할 바이트와 0으로 채워야할 바이트의 수를 계산하고 반복문을 돌면서 `vm_alloc_page_with_initializer` 함수를 통해 pending page를 생성한다.
우리는 바이너리 파일 적재를 위해 필요한 정보를 담을 구조체를 만들어서 `aux` 인자로 넘겨줘야한다.

여기서 궁금증:
- Q: 일단 `struct lazy_aux` 구조체에 어떤 데이터를 담아야하는거지?
  A: 파일 구조체, 파일 오프셋, 읽을 바이트 수, 0으로 채울 바이트 수

- Q: 타입은 어떻게 알지??
  A: `page_read_bytes`가 0이면 `VM_ANON`, 그렇지 않다면 `VM_FILE`이다.

- Q: `struct lazy_aux`에 파일 오프셋이 왜 필요하지??
  A: `lazy_load_segment` 함수에서 `file_read_at`을 통해서 정확한 위치를 읽기 위해서 필요하다!!

- Q: 파일 오프셋은 그냥 `page_read_bytes`를 더해주는 방식으로 업데이트 해도 되는건가??
  A: 세그먼트 내에서는 파일 상에서 논리적으로 연속된 바이트를 읽기 때문에 내부적으로 파일 시스템이 조각난 블록을 어떻게 찾아서 따라가는지는 지금 몰라도 된다. (아마 프로젝트4에서 구현하겠지..)

- Q: `vm_alloc_page_with_initializer`에 전달된 `lazy_load_segment` 함수는 `struct page` 어디에 위치하는거지??
  A: `vm_initializer` (아마 실제로 로드하는 역할인 것 같다.)

#### `lazy_load_segment`
이 함수는 `load_segment` 함수에서 `vm_alloc_page_with_initializer`를 호출할 때 넘겨주었던 함수로, 페이지 폴트가 발생했을 때 호출된다.
`struct page *page`, `void *aux`를 인자로 받아서 페이지에 프레임을 할당하고 그 프레임에 데이터를 읽어오는 역할을 하는 것 같다.

