## 왜 하필 4 레벨 페이지 모델을 쓸까?

```
PML4 (Page-Map Level 4)
PDPT (Page-Directory Pointer Table)
PD (Page Directory)
PT (Page Table)
```

1. 64비트 중 우리는 48비트만 사용한다.
2. 페이지의 크기가 4KB라서 페이지 내부 오프셋을 위해 12비트가 필요하다.
3. 나머지 남는 비트는 36비트.
4. 9비트씩 사용해서 4레벨로 설계.
5. 각 테이블의 엔트리가 512개로 유지.
6. 지금까지 파악한 이유는 이정도..

