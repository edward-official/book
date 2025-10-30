###### 목표
- 주소 변환의 기본 개념을 이해하기
- MMU의 역할을 이해하기

###### 주소 변환
공식적으로 주소 변환이란 가상 주소 공간(VAS)의 원소를 물리 주소 공간(PAS)의 원소로 매핑하는 함수를 말합니다.
즉, VAS 원소가 물리 메모리에 존재하지 않으면 NULL이 반환됩니다.

###### 페이지 테이블
###### Figure 9.12
![[Figure09-12.png]]
페이지 테이블이란 각 ==프로세스 별==로 가상 메모리의 ==커널 영역==에 할당되어 가상 주소를 물리 주소로 변환하기 위해서 사용하는 자료구조입니다. (참고로 가상주소는 ==페이지 단위==로 할당됩니다.)

MMU는 페이지 테이블을 이용해서 주소를 변환합니다.
CPU 내부에는 PTBR(Page Table Base Register)라는 레지스터가 존재하는데, 이는 현재 사용중인 페이지 테이블의 시작 주소를 가르킵니다.
자 그럼 MMU는 지금 페이지 테이블의 위치(PTBR)와 가상주소를 알고있습니다.
이 상황에서 가상주소는 두 부분(VPN, VPO)으로 나뉘는데 이 중 VPN을 이용해서 페이지 테이블의 PTE(Page Table Entry)를 찾아 valid bit를 보면 물리 메모리에 우리가 찾는 페이지가 캐시되어 있는지 확인할 수 있습니다. (valid bit)

- 만약 페이지가 캐시되어있다면 (valid bit: 1) 테이블 내의 PPN과 가상 주소의 PPO(=VPO)부분을 조합해서 물리 메모리 주소를 도출해낼 수 있습니다.
- 만약 페이지가 캐시되어있지 않다면 (valid bit: 0) 테이블 내의 PPN 부분은 디스크 주소로 환산하기 위한 공간으로 활용되어 page fault handler에 의해서 디스크 공간에 접근할 수 있게 됩니다. 또 page fault handler는 디스크에서 찾은 페이지를 메모리에 캐시하기 위해서 희생될 페이지를 정하기도 합니다.

#### 9.6.1 Integrating Caches and VM
###### Figure 6.25
![[Figure06-25.png]]
###### Figure 9.14
![[Figure09-14.png]]
가상 메모리 개념과 SRAM 캐시를 모두 사용하는 시스템에서는 SRAM 캐시 접근 시 가상 주소를 쓸 지 물리 주소를 쓸 지가 중요한 문제가 됩니다.
세부적인 설계상의 trade-off는 복잡하지만 대부분의 시스템은 물리 주소 방식을 선택합니다.
- 가상 페이지 공유가 간단하다.
- 접근 권한 검사도 주소 변환 과정에서 수행됨. (보호 문제 단순화)

#### 9.6.2 Speeding Up Address Translation with a TLB
일단 페이지 테이블 자체도 ==SRAM/DRAM 메모리==에 있기 때문에 이를 참조하는 것 자체도 오버헤드가 발생합니다.
따라서 MMU 내부에 TLB(Translation Lookaside Buffer)를 두고 최근에 사용된 PTE들을 캐시합니다.

#### 9.6.3 Multi-Level Page Tables
우리는 지금까지 single-level page table만 가정했습니다.
여기서 발생하는 문제는 페이지 테이블의 크기가 너무 크다는 것입니다. ([네이버 블로그](https://blog.naver.com/pmdrdocg/224052320974))
이를 위해서 도입된 개념이 바로 hierarchical page table입니다.
###### Figure 9.17
![[Figure09-17.png]]
single-level page table의 크기는 가상 메모리 공간의 최대 용량과 같은데, 그만큼이 필요하지가 않다는 것!!

#### 9.6.4 Putting It Together: End-to-End Address Translation
![[ImageForChapter09Unit06-4.png]]
작은 시스템 예시를 통해서 TLB, Page Table, Cache를 이용한 주소 과정을 살펴봅시다.
위의 이미지는 예시를 위한 시스템의 가정입니다.

###### Figure 9.19
![[Figure09-19.png]]
위의 이미지는 가상 주소와 물리 주소의 형태입니다.

###### Figure 9.20 (a)
![[Figure09-20a.png]]
위 Figure 9.20(a)는 ==4-way set associative==한 TLB를 보여주고 있습니다.
TLB를 관리하기 위해서 MMU는 VPN의 2개 비트를 이용해서 TLB 세트 인덱스를 정하고 나머지 6개의 비트를 이용해서 TLB 세트 내부의 태그 번호를 정합니다. ==(direct-mapped 방식과 반대되는 개념)==

###### Figure 9.20 (b)
![[Figure09-20b.png]]
위 Figure 9.20(b)는 싱글 페이지 테이블의 일부분을 보여주고 있습니다.

###### Figure 9.20 (c)
![[Figure09-20c.png]]
위의 Figure 9.20(c)는 direct-mapped 캐시의 구조를 보여주고 있습니다.
그림에서 보이듯이 ==4개의 비트(CI)는 캐시의 세트 번호를, 6개의 비트는 캐시 태그를, 2개의 비트는 캐시 오프셋==을 정하는데 사용이 됩니다.