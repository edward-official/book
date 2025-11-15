```
/* ELF 실행 파일을 현재 스레드 주소 공간에 적재하는 함수.
 * - file_name : "실행파일이름 인자1 인자2 ..." 형태의 전체 커맨드 라인
 * - if_       : 사용자 모드로 넘어갈 때 쓸 인터럽트 프레임 (RIP, RSP 등 세팅)
 * 성공하면 true, 실패하면 false를 반환한다. */
static bool
load (const char *file_name, struct intr_frame *if_) {
	struct thread *t = thread_current ();
	struct ELF ehdr;                 /* ELF 헤더를 담을 구조체 */
	struct file *file = NULL;        /* 실행 파일을 가리키는 포인터 */
	off_t file_ofs;                  /* Program header를 읽기 위한 파일 오프셋 */
	bool success = false;
	int i;

	/* ---- 인자 파싱을 위한 임시 버퍼 및 배열들 ---- */
	char *file_name_copy = NULL;     /* file_name을 복사해 파싱에 사용할 버퍼 */
	enum { MAX_ARGS = LOADER_ARGS_LEN / 2 + 1 }; /* 최대 인자 개수 상한 */
	char *argv[MAX_ARGS];            /* 각 인자 문자열 포인터 보관 */
	uintptr_t argv_addrs[MAX_ARGS];  /* 스택 위에 복사된 인자의 주소 보관 */
	int argc = 0;                    /* 인자 개수 */
	char *token, *save_ptr;

	/* 전체 커맨드 라인을 페이지 하나 크기의 버퍼에 복사 */
	file_name_copy = palloc_get_page (PAL_ZERO);
	if (file_name_copy == NULL)
		goto done;                  /* 메모리 할당 실패 시 종료 */
	strlcpy (file_name_copy, file_name, PGSIZE);

	/* 공백을 기준으로 커맨드 라인을 토큰화 해서 argv[]에 저장
	 * 예: "echo x y" -> argv[0]="echo", argv[1]="x", argv[2]="y" */
	for (token = strtok_r (file_name_copy, " ", &save_ptr);
	     token != NULL;
	     token = strtok_r (NULL, " ", &save_ptr)) {
		if (argc >= MAX_ARGS) {   /* 인자 개수가 너무 많으면 실패 처리 */
			goto done;
		}
		argv[argc++] = token;
	}

	/* 인자가 하나도 없으면(실행 파일 이름도 없는 경우) 실패 */
	if (argc == 0)
		goto done;

	/* ---- 페이지 디렉터리(PML4) 생성 및 활성화 ---- */
	t->pml4 = pml4_create ();
	if (t->pml4 == NULL)
		goto done;
	process_activate (thread_current ());

	/* ---- 실행 파일 열기 ----
	 * 첫 번째 토큰(argv[0])을 실제 실행 파일 이름으로 사용한다. */
	file = filesys_open (argv[0]);
	if (file == NULL) {
		printf ("load: %s: open failed\n", argv[0]);
		goto done;
	}

	/* ---- ELF 헤더 읽기 및 유효성 검증 ---- */
	if (file_read (file, &ehdr, sizeof ehdr) != sizeof ehdr
			|| memcmp (ehdr.e_ident, "\177ELF\2\1\1", 7)   /* ELF 매직 & 64비트 */
			|| ehdr.e_type != 2                           /* 실행 파일 타입인지 */
			|| ehdr.e_machine != 0x3E                     /* amd64 아키텍처인지 */
			|| ehdr.e_version != 1                        /* ELF 버전 */
			|| ehdr.e_phentsize != sizeof (struct Phdr)   /* Program header 크기 */
			|| ehdr.e_phnum > 1024) {                     /* Program header 개수 제한 */
		printf ("load: %s: error loading executable\n", file_name);
		goto done;
	}

	/* ---- Program header들을 읽어서 각 세그먼트를 메모리에 적재 ---- */
	file_ofs = ehdr.e_phoff; /* 🔥 edward: ELF Program Header Offset */
	for (i = 0; i < ehdr.e_phnum; i++) {
		struct Phdr phdr;

		/* 파일 범위를 벗어나면 실패 */
		if (file_ofs < 0 || file_ofs > file_length (file))
			goto done;
		file_seek (file, file_ofs);

		/* Program header 하나 읽기 */
		if (file_read (file, &phdr, sizeof phdr) != sizeof phdr)
			goto done;
		file_ofs += sizeof phdr;

		/* p_type(세그먼트 타입)에 따라 처리 */
		switch (phdr.p_type) {
			case PT_NULL:
			case PT_NOTE:
			case PT_PHDR:
			case PT_STACK:
			default:
				/* 무시해도 되는 세그먼트는 그냥 스킵 */
				break;

			case PT_DYNAMIC:
			case PT_INTERP:
			case PT_SHLIB:
				/* 동적 로딩 등은 Pintos에서 지원하지 않으므로 실패 */
				goto done;

			case PT_LOAD:
				/* 실제로 메모리에 적재해야 하는 세그먼트 */
				if (validate_segment (&phdr, file)) {
					bool writable = (phdr.p_flags & PF_W) != 0;   /* 쓰기 가능 여부 */
					uint64_t file_page = phdr.p_offset & ~PGMASK; /* 파일에서 읽기 시작할 페이지 */
					uint64_t mem_page  = phdr.p_vaddr  & ~PGMASK; /* 매핑할 가상 주소 페이지 */
					uint64_t page_offset = phdr.p_vaddr & PGMASK; /* 페이지 내 오프셋 */
					uint32_t read_bytes, zero_bytes;

					if (phdr.p_filesz > 0) {
						/* 일반적인 세그먼트:
						 * 앞 부분은 파일에서 읽고 나머지는 0으로 채움(BSS). */
						read_bytes = page_offset + phdr.p_filesz;
						zero_bytes = (ROUND_UP (page_offset + phdr.p_memsz, PGSIZE)
								- read_bytes);
					} else {
						/* 완전히 0으로만 이뤄진 세그먼트(BSS 전용).
						 * 파일에서 읽지 않고 전부 0으로 채움. */
						read_bytes = 0;
						zero_bytes = ROUND_UP (page_offset + phdr.p_memsz, PGSIZE);
					}

					/* load_segment()가 실제 물리 프레임 할당 + 파일 읽기 + 매핑까지 처리 */
					if (!load_segment (file, file_page, (void *) mem_page,
					                   read_bytes, zero_bytes, writable))
						goto done;
				}
				else
					goto done;
				break;
		}
	}

	/* ---- 사용자 스택 페이지 설정 ---- */
	if (!setup_stack (if_))
		goto done;

	/* ---- 프로그램 시작 주소(RIP) 설정 ---- */
	if_->rip = ehdr.e_entry;

	/* =======================================================
	 *            인자 전달(argument passing) 부분
	 * ======================================================= */

	/* 1) 각 인자 문자열을 스택에 "뒤에서부터" 복사
	 *    (스택이 위에서 아래로 자라므로, 마지막 인자부터 push) */
	for (i = argc - 1; i >= 0; i--) {
		size_t arg_len = strlen (argv[i]) + 1;   /* '\0' 포함 길이 */
		if_->rsp -= arg_len;                     /* 스택 포인터를 문자열 크기만큼 내려감 */
		memcpy ((void *) if_->rsp, argv[i], arg_len); /* 실제 문자열 복사 */
		argv_addrs[i] = if_->rsp;                /* 나중에 argv[i]가 가리킬 주소로 저장 */
	}

	/* 2) 스택을 16바이트 단위로 정렬(ABI 규약 맞추기)
	 *    rsp % 16 이 0이 되도록 0 패딩을 push */
	size_t padding = if_->rsp % 16;
	if (padding) {
		if_->rsp -= padding;
		memset ((void *) if_->rsp, 0, padding);
	}

	/* 3) argv[argc] = NULL 에 해당하는 NULL sentinel push */
	if_->rsp -= sizeof (uintptr_t);
	memset ((void *) if_->rsp, 0, sizeof (uintptr_t));

	/* 4) 방금 스택에 복사한 각 인자 문자열의 주소를 역순으로 push
	 *    결과적으로, 스택에는 [argv[0] 주소, argv[1] 주소, ...] 순서로 쌓이게 됨 */
	for (i = argc - 1; i >= 0; i--) {
		if_->rsp -= sizeof (uintptr_t);
		memcpy ((void *) if_->rsp, &argv_addrs[i], sizeof (uintptr_t));
	}
	uintptr_t argv_start = if_->rsp; /* 여기 주소가 결국 argv 포인터가 됨 */

	/* 5) 이제 argv 포인터(위에서 정한 argv_start)를 스택에 push */
	if_->rsp -= sizeof (uintptr_t);
	memcpy ((void *) if_->rsp, &argv_start, sizeof (uintptr_t));

	/* 6) argc 값을 push */
	if_->rsp -= sizeof (uintptr_t);
	memcpy ((void *) if_->rsp, &argc, sizeof (uintptr_t));

	/* 7) fake return address (0) 하나 더 push (관례적으로 사용) */
	if_->rsp -= sizeof (uintptr_t);
	memset ((void *) if_->rsp, 0, sizeof (uintptr_t));

	/* 8) 실제로 유저 모드에서 main을 부를 때
	 *    rdi = argc, rsi = argv 로 전달되도록 레지스터 설정 */
	if_->R.rdi = argc;
	if_->R.rsi = argv_start;

	success = true;

done:
	/* 여기로는 성공이든 실패든 반드시 도달함. */
	if (file != NULL)
		file_close (file);              /* 파일 열려 있으면 닫기 */
	if (file_name_copy != NULL)
		palloc_free_page (file_name_copy); /* 인자 파싱에 쓴 페이지 해제 */
	return success;
}

```