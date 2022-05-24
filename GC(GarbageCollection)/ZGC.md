## ZGC에 대해서


## ✔ ZGC : a good fit for server applications, where large heaps are common, and fast application response times are a requirement.




## | Region

### ZGC는 Region을 ZPage로 정의하여 사용합니다. ZPage 는 동적으로 생성/삭제 되며, 2MB의 배수 형태로 관리합니다.
<img src="https://user-images.githubusercontent.com/42057185/170087772-510a8441-7494-4264-b500-1a826d2e076a.png"/>
<br>

## ZGC ZPage 할당 모습
<br>

<img src="https://user-images.githubusercontent.com/42057185/170087873-eb6a721c-1ef8-483b-a07d-b73b7da7f7ae.png"/>

### Large 타입 페이지는 하나의 객체만 저장합니다. (Large pages can only store exactly one object)

### 따라서 Medium 타입 페이지보다 작은 사이즈가 될 수도 있죠.


### 예) 6M 크기의 객체가 들어오면 4M 이상이므로 Large 타입 페이지에 저장해야하며,

### 이 때 생성된 Large 타입 페이지는 6M 가 될 것이므로 Medium (32M) 보다 작은 사이즈입니다.





## | Compact 개념

### 일반적으로 GC 가 수행되면 (가비지가 쌓여있는) 힙 영역을 비우기 위해 살아있는 객체를 이동시킵니다. 이때 객체를 위치시키기 위해 빈 공간을 찾아야 하는데 이것은 새로운 영역을 할당해서 채우는 것보다 비용이 훨씬 더 듭니다.

### ZGC에서의 compact 과정은 기존 region의 빈 곳을 찾아서 넣는게 아니라 새로운 region을 생성한 후 

### 살아있는 객체들을 채우도록 동작하는데요. 다만, 이때 고려해야 할 점이 있습니다. 


### 아래의 예시를 통해 살펴보면,

### Region 2 의 영역이 꽉 차서 GC 대상이라고 가정했을 때, Region 2에서 살아있는 객체를 새로운 Region 3를 만들어서 옮기게 됩니다.

<img src="https://user-images.githubusercontent.com/42057185/170087983-9fa2e8f2-d0eb-4f7b-bdf4-93c7f970332a.png"/>


### 이어서 GC가 진행되면서 Region 2 영역이 삭제되고 Region 1에서의 reference f 는 Region 3의 새로 

### 생성된 객체를 가리키게 (remapping) 될텐데요. 이때 만일 remapping이 완료되기 전에, Region 1 에서 참조하던 Region 2의 객체 값을 a=3 식으로 변경해 버리면 새로 할당한 Region 3 의 객체의 값과 불일치가 일어나게 됩니다.

<img src="https://user-images.githubusercontent.com/42057185/170088047-eadfba52-103a-467f-8cff-aaf7b4f851a2.png"/>





## | ZGC의 몇 가지 전략

### ZGC에서는 이러한 문제를 해결하기 위해 몇 가지 전략을 사용합니다.


### Concurrent GC 를 사용해서 객체의 GC 메타데이터를 객체 주소에 저장 (e.g Reference coloring)

### JIT를 사용해 GC 를 돕기 위해 작은 코드를 주입 (e.g GC Barriers)


## ■ Reference coloring (Colored Pointers)

### ZGC는 GC 메타데이터를 객체의 메모리 주소에 표시합니다. ZGC는 64비트만 지원하는데, 메모리의 주소 파트로 42비트(4TB)를 사용하고 다른 4비트를 GC metadata (finalizable, remap, mark1, mark0) 를 저장하는 용도로 사용합니다.
<img src="https://user-images.githubusercontent.com/42057185/170088107-0ebc5b56-d21e-46fb-9c59-ae5d02bae2f8.png"/>


### ✔ colored pointer의 bit가 모두 0인 경우는 없습니다.





## - Multi-mapping

### reference coloring에 의해 메모리 offset (주소) x 에 대해서 특정 시점에는 mark0, mark1, remap 비트 중 하나가 1이 되기에 offset x 에 페이지(ZPage)를 할당할때 ZGC는 동일한 페이지를 3개의 다른 주소 

### 영역에도 할당합니다. (3 different address : zPhysicalMemoryBacking_linux_x86.cpp).

### for marked0: (0b0001 << 42) | x

### for marked1: (0b0010 << 42) | x

### for remapped: (0b0100 << 42) | x

### 이때 3개의 매핑되는 영역의 물리 메모리 주소를 모두 동일하게 설정합니다. 즉, ZGC는 서로 다른 범위의 가상 메모리를 동일한 물리 메모리로 매핑하는 일종의 trick을 사용합니다. (공유 라이브러리-so파일-과 같은 이치)

<img src="https://user-images.githubusercontent.com/42057185/170088189-e4845da7-fca9-4e4f-b046-381f2b5a0680.png"/>


### (OS의 virtual memory, physical memory, MMU, TLB 에 대한 이해도가 있다는 가정하에 관련 설명은 생략)
### 예) 10K 사이즈의 작은 객체를 “생성” 하려면, small type zpage를 먼저 할당하고 해당 페이지에 위치시킬텐데, 이때 만들어지는 small type zpage 는 총 3곳(marked0, marked1, remapped) 의 영역에 동시에 만들어집니다.


### 이렇게 가상 메모리 위치는 다르지만 동일한 물리 메모리를 보게 함으로써, 객체의 주소에 GC 메타데이터 비트가 변경되어도 실제 다루는 객체 값은 항상 동일합니다. 전체적인 메모리 구조를 정리하면 아래와 같습니다.

<img src="https://user-images.githubusercontent.com/42057185/170088237-f8188f5e-b1e4-46e2-95c3-626ed9829e36.png"/>


[Address Space & Pointer Layout ]




### ZGC는 메모리 주소 4TB 에서부터 20T까지 총 16TB 용량의 주소 공간을 예약합니다. (실제로 이 메모리를 모두 사용하지는 않음, 12~16T 영역은 미사용)


### ✔ 메모리 주소에 GC 메타데이터가 존재하더라도 메타데이터를 제거한 후 실제 주소 파트만 사용할 수도 있겠지만, 이를 위해선 항상 마스킹(메타데이터 비트 제거)을 해야 하고, 비용이 많이 들기 때문에 ZGC는 multi-mapping 방식을 사용합니다.


### ✔ Solaris/SPARC, ARM AArch64은 주소의 load, store시에 메타데이터 비트를 마스킹할 수 있도록 H/W적으로 지원하기에 1종류의 힙 영역만 사용 가능합니다.



### ⚠ ZGC에서 java 프로세스의 RSS 사이즈는 3배정도 더 크게 보일 수 있습니다.

### 예) -xmx=16G → RSS ~= 50G : 실제 메모리는 16G 까지 쓸지라도 RSS 사이즈는 50G 정도 까지 보일 수 있습니다.

### 따라서 Cloud에서는 OOM killers (e.g AWS)에 의해 강제 종료 되는걸 조심해야 합니다.

### 참고 링크 

### 글 후반부에 메모리 사용 분석 참고



## ■ Load Barrier

### 어플리케이션 쓰레드는 힙 메모리에 있는 객체를 참조할때 Load barrier를 만나게 되는데, 이것은 JIT에 

### 의해 주입된 코드입니다. 이 코드는 주소의 colored point(즉, GC메타데이터 비트)가 bad color인지 체크하고, 만약 bad color라면 객체를 상황에 따라서 mark/relocate/remapping 합니다.


### ✔ 힙 참조시 항상 load barrier 를 거쳐야 하므로 최대 4%까지의 성능 저하가 있지만, 전체적으로 다른 GC와의 성능을 비교해 보면 결과적으로 크게 이득이라고 할 수 있습니다.
<img src="https://user-images.githubusercontent.com/42057185/170088290-2bbe4a0d-3766-427e-8309-f8609696c45d.png"/>


### [load barrier가 트리거되는 예시]

<img src="https://user-images.githubusercontent.com/42057185/170088355-636d34d5-e893-4ca0-b729-e57eb0c64495.png"/>

### [load barrier color testing pseudo code 예시]
### Load Barrier의 보다 상세한 동작은 글 후반부에 다시 설명하겠습니다.




## | GC Phase
### GC는 크게 marking (ZPhaseMark, ZPhaseMarkCompleted)과 relocating(ZPhaseRelocate)이라는 

### 2개의 주요한 단계를 가집니다. (실제로는 좀 더 단계가 세분화 되어 있습니다. : gc cycle source )

<img src="https://user-images.githubusercontent.com/42057185/170088431-d3c644a1-a475-4c59-a0d6-ce447d15c42b.png"/>


### ■ 1번 Mark Start 단계

### ZGlobalPhase == ZPhaseMark

### 모든 어플리케이션 쓰레드를 멈추고 (STW) 각 쓰레드마다 가지고 있는 local variable 들을 스캔합니다.

### thread local variable에서 힙으로의 참조를 GC Root 라고 하며 GC Root set을 만듭니다. 일반적으로 gc root 개수는 적은 편이라 mark start 단계의 STW 는 “극히 짧은 시간”만 걸립니다.



### ■ 2번 Concurrent Marking 진행

### root set에서 시작해 객체 그래프를 탐색하며 도달할 수 있는 객체는 살아있는 걸로 표시(mark)합니다. (최종적으로 도달되지 못하는 객체는 Garbage로 판단)

<img src="https://user-images.githubusercontent.com/42057185/170088511-e4af56e9-d38f-42d8-8a89-cef8654679c5.png"/>

### ZGC는 각 page 의 livemap 이라고 불리는 곳에 살아있는 객체 정보를 저장합니다. livemap 은 주어진 인덱스의 객체가 strongly-reachable하거나 final-reachable 한지 등의 정보를 비트맵 형태로 저장하고 있습니다.

<img src="https://user-images.githubusercontent.com/42057185/170088618-97c3db35-d63f-44cc-a5b2-66e2549dd820.png"/>

### 이 단계에서 어플리케이션 쓰레드의 경우 load barrier를 통해 객체의 참조에 대해 테스팅을 진행하며, 참조가 bad color라면 slow_path로 진입한 후 marking을 위해 thread-local marking buffer(큐)에 추가합니다 : mark_object()



### 이 버퍼가 가득 차면 GC 쓰레드가 이 버퍼의 소유권을 가져오고 이 버퍼에서 도달할 수 있는 모든 객체를 재귀적으로 탐색합니다. 즉, 어플리케이션 쓰레드에서의 marking은 주소를 버퍼로 넣기만 할 뿐이고, GC 쓰레드가 객체 그래프를 탐색하고 live map을 업데이트 하는 역할을 하는 것이죠.




### 이 단계가 모두 끝나면 살아있는 객체와 가비지 객체로 나뉘게 됩니다.



### ■ 3번 Mark End 단계

### 모든 어플리케이션 쓰레드를 멈추고 (STW) thread-local의 marking buffer 를 탐색하며 비웁니다.

### 이 때, 아직 marking 하지 않은 참조들 중에 큰 하위 객체 그래프를 발견하게 되면 처리해야 하는 시간이 많아 STW 시간 역시 오래 걸릴 수도 있기 때문에 ZGC는 1밀리초 후에 Mark End 단계를 끝내고 Concurrent Mark 단계로 돌아간 다음 다시 Mark End 단계로 진입합니다. (전체 그래프를 모두 통과할때까지)



### ■ 4번 Concurrent Processing


### ZGlobalPhase == ZPhaseRelocate


### Concurrent Reset Relocation Set

### Concurrent Destroy Detached Pages : 비어있는 (가비지로 가득찬) page는 메모리를 해제합니다. 이때 불필요한 클래스는 unload 합니다. (2번째 GC 사이클부터 나타납니다… 뒤에 다시 설명)

### Concurrent Select Relocation Set

### Prepare Relocation Set : Relocation Set은 (가비지 대상이라 비워져야 하는) page 들의 Set으로, relocation set에 들어있는 page 의 객체들을 대상으로 forwarding table 을 할당합니다. forwarding table은 기본적으로 객체가 재배치된 주소를 저장하는 hash map 입니다.


### 아래 그림은 concurrent processing 단계에서 relocation set 을 선택하고 forwarding table을 할당한 모습입니다. (아래 그림은 연속성을 가지며 뒷 부분 설명에서도 계속 사용할 예정입니다.)

<img src="https://user-images.githubusercontent.com/42057185/170088702-db0446cc-aad6-4cbf-b0c1-d3c1d79b71d0.png"/>





### ■ 5번 Relocation Start

### 모든 어플리케이션 쓰레드를 멈추고 (STW) relocation set의 page에 있는 객체 중 GC Root에서 참조되는 것들은 모두 일괄 relocation/remapping 합니다 : Relocation Root




### ■ 6번 Concurrent Relocation

### GC 쓰레드는 relocation set 의 살아있는 객체를 탐색하고 아직 재배치(relocate)되지 않은 모든 객체를 새로운 ZPage로 재배치하며 이러한 재배치는 어플리케이션 쓰레드(load barrier)를 통해 일어날 수도 있습니다.

<img src="https://user-images.githubusercontent.com/42057185/170088791-e8e92c87-4f64-449f-9882-a507f35cb373.png"/>


### 어떤 객체는 순간적으로 GC 쓰레드나 어플리케이션 쓰레드에 의해 동시에 재배치되는 경우가 발생할 수도 있는데, 이 경우는 처음 재배치 한 쓰레드가 win 하게 됩니다. (ZGC는 atomic CAS operation 을 통해 winner를 결정)

<img src="https://user-images.githubusercontent.com/42057185/170088860-557d50fb-6bfb-4224-aa19-bbbae246b4f0.png"/>

### 재배치(relocation) 단계는 GC 쓰레드가 relocation set을 마지막까지 모두 따라가는것을 마치는 즉시 완료됩니다.



### ■ 7번 Concurrent Remapping

### 모든 재배치(relocation)가 끝났으니 old 객체가 아닌 새로운 객체로 참조를 변경(remapping)합니다. 사실 이 단계는 별도로 정해진 단계는 아닙니다. (GC는 6번 Concurrent Relocate 단계까지만 존재)

### remapping은 어플리케이션 쓰레드의 load barrier 에 의해서 진행(뒤에 다시 설명)되지만 다음 GC cycle(새로운 marking cycle)전까지 모두 완료되지 않을 수도 있습니다.

### 즉, 다음 GC 사이클의 marking(ZPhaseMark) 단계에서도 진행될 수도 있기에 GC marking 단계시에는 필요하다면 remapping을 위해 forward table까지 확인해야 합니다.

### 이러한 흐름은 객체 참조에 두 개의 마킹 비트(marked0, marked1)가 존재하는 이유와 관련되며, marking 단계는 marked0 이나 marked1을 번갈아 사용합니다. relocation 단계 (ZPhaseRelocate) 이후에 아직 remapping되지 않은 참조가 존재할 수 있으며, 마지막 marking 주기 세트의 bit가 여전히 남아 있을 수 있습니다. (예를 들어) 만약 새로운 marking 단계에서 동일한 marking bit를 사용한다면 load barrier는 이 참조를 이미 marking 된걸로 판단할 수 있죠.

### 참고로 ZGC가 초기화 되었을때는 첫 marked는 0번을 사용하고, address view는 remapped를 사용하는걸 알 수 있습니다.





### ■ 실제 GC 사이클의 전체 모습

### 지금까지의 설명을 토대로, 실제 흐름은 아래와 같은 형태로 진행될 것으로 보입니다.

<img src="https://user-images.githubusercontent.com/42057185/170088935-fb9bd29d-8981-4685-9624-bd3c04df21b1.png"/>





### | Load Barrier
### 어플리케이션 쓰레드는 힙 메모리 주소를 참조할때 load barrier 코드를 실행하게 되며 load barrier 에서는 bad color 인지 테스트합니다. 만약 bad color로 판단되면 객체를 mark를 하거나 relocate 혹은 remapping을 해야 하고 이 과정을 slow_path라고 부릅니다.


### 전역 변수인 ZAddressGoodMask와 ZAddressBadMask에는 참조가 좋은 상태인지 아니면 나쁜(뭔가 조치가 필요함을 뜻함) 상태인지를 뜻하는 bit mask가 지정되며, 이 변수들은 marking 시작 단계(STW)나 relocation 단계(STW)에서 동시(same time)에 세팅됩니다. ZGC source 의 아래 표는 이러한 bit mask가 어떤 상태일 수 있는지에 대한 개요를 제공합니다.

<img src="https://user-images.githubusercontent.com/42057185/170089004-67fb0529-80ed-43ee-8096-51e0f93ca4b7.png"/>

### ✔ 참조의 형태에 따라 마스킹이 다릅니다. 예를 들어 Weak 참조의 경우는 WeakBadMask 로 bad color인지를 테스트합니다. (slow_path 또한weak 참조를 위한 slow_path 진입)


### GoodMask와 BadMask는 GC 단계에 따라 값이 결정됩니다.

### Mark 단계(ZPhaseMark)에서 address view가 Marked0이라면, GoodMask는 001, BadMask는 110입니다.

<img src="https://user-images.githubusercontent.com/42057185/170089082-424e1186-676f-4770-97a9-f49fc5d4b0d7.png"/>

### ✔ 9번 소스라인에서 볼 수 있듯이 GC 사이클이 실행될때마다 ZAddressMetadataMarked는 marked0과 marked1이 교대로 세팅되는걸 알 수 있습니다.


### Relocation Start 단계(ZPhaseRelocate)에서는 address view는 Remapped이고 GoodMask는 100, BadMask는 011입니다.

<img src="https://user-images.githubusercontent.com/42057185/170089146-3d05436b-48ff-44fe-9d82-e65a9f81aa8f.png"/>


### 예) bad mask가 011 인데, 참조하려는 주소의 메타데이터 bit가 001 이면 bad color 로 판단합니다.




### ■ slow_path

### slow_path는 현재 단계(ZPhaseMark 혹은 ZPhaseRelocate)와 mask 상태에 따라서 케이스별로 나뉩니다. 가장 많이 사용되는 slow_path의 예를 아래 코드를 통해 살펴보겠습니다.

<img src="https://user-images.githubusercontent.com/42057185/170089221-8e5aad8d-96a6-48aa-aac0-44de21cc3b23.png"/>

### 예) 현재 GC 사이클이 Mark 단계라고 한다면, 위의 소스라인 9번을 통해 아래 mark() 함수가 호출됩니다.

<img src="https://user-images.githubusercontent.com/42057185/170089267-802067d7-dd6f-459c-96f3-7abb036abd18.png"/>


### 예) 현재 address view가 Marked0 이고 (GoodMask=001), 테스트한 객체의 메타데이터 비트가 010 이라서 bad color라고 판단된 경우라면, 이 객체는 이전 GC 사이클(Marked1)이 완료되며 relocation은 완료되었으나 아직 remapping이 안된걸 뜻합니다. 따라서 현재의 GC 사이클 (Mark단계)에서는 위의 소스 13번 라인을 통해 remapping을 진행하게 됩니다.




### ■ Load barrier에 의해 참조가 조정되는 모습

### load barrier의 slow_path 를 거치면 항상 self_heal 도 수행되며, self_heal 은 참조하는 주소 값을 업데이트 하는 것을 뜻합니다.

<img src="https://user-images.githubusercontent.com/42057185/170089327-d1d97c9d-5954-43b0-8f0c-32796ce08eaa.png"/>

<img src="https://user-images.githubusercontent.com/42057185/170089366-52b1a9b4-229c-4882-92ca-3887743bd469.png"/>


### 어플리케이션 쓰레드의 Load Barrier 에 4번 객체가 가지는 5번객체에 대한 참조가 self-heal되는 모습 예시

<img src="https://user-images.githubusercontent.com/42057185/170089417-f43e4d1b-b0c1-44e8-b258-63800ae77529.png"/>

### 다시 한 번 정리하자면, N번째 GC cycle의 remapping은 N+1번째 GC Cycle의 Mark End 단계에서 모두 완료됩니다.



### 추가로, 아래는 2번째 GC Cycle의 Concurrent Prepare for relocate 단계에서 미사용 page 가 회수되는 예시입니다.

<img src="https://user-images.githubusercontent.com/42057185/170089463-055de962-2d71-4369-8322-b825c3d2db62.png"/>




### ■ Flow chart

### slow_path의 흐름을 간략히 아래 그림으로 설명하겠습니다.

<img src="https://user-images.githubusercontent.com/42057185/170089543-5a0cc395-d465-4ec5-a14e-edd702a01023.png"/>

### A : ZPhaseMark 단계이므로 good(x)는 주소 x에 marked0 이나 marked1 비트가 표시된 주소를 리턴합니다.

### B : forwarding table 에 to_addr이 존재할 수 밖에 없습니다. mark 단계에서 remap bit가 없고, bad_color란 얘기는 이전 GC 사이클에서 mark 된 상태를 뜻하며, relocation set에 존재한다는 것은 이전 GC 사이클에서 concurrent relocation 과정이 끝난 후 현재 GC cycle의 mark 단계로 온 것을 뜻합니다.

### C : ZPhaseRelocate 단계이므로 good(x)는 주소 x에 remapp 비트가 표시된 주소를 리턴합니다.





### | 기타

### ■ G1GC vs ZGC 메모리 상태 비교


### 실제 어플리케이션이 수행하는 업무 내용이나 특성, 그리고 트래픽 등 환경에 따라서 G1GC, ZGC를 비교한다는 것은 다양한 결과가 나오겠지만, 두 GC의 대략적인 차이점 정도만 살펴보기 위해서 Apache Kafka Broker 를 1대 띄워서 서로 비교한 자료를 토대로 아래 내용을 정리해보겠습니다.


### 공통 환경

### 8Core(Intel XEON) 8G Memory VM

### OpenJDK 64-Bit Server VM (build 13.0.2+8, mixed mode, sharing)


### JVM OPT

<img src="https://user-images.githubusercontent.com/42057185/170089658-372457b7-7085-4b65-8def-11aa346cc876.png"/>




### G1GC vs ZGC 비교

<img src="https://user-images.githubusercontent.com/42057185/170089705-43f8f4e6-09e4-4b7e-803e-e91c2effeaf9.png"/>

<img src="https://user-images.githubusercontent.com/42057185/170089735-a24da19f-fd3f-4b5f-8a12-ec62a0b8ed20.png"/>

<img src="https://user-images.githubusercontent.com/42057185/170089826-f348fbc4-8449-407b-8879-ac9cd00fc105.png"/>



### 정리해보면 ZGC를 사용하는 java 프로세스는 실제 사용하려는 메모리 (xms등으로 설정한..)와 GC를 위한 부가적인 사용 메모리 등의 공간을 필요로 할텐데, 실제로는 이보다 월등히 큰 공간을 사용하는 것처럼 시스템에서 보입니다.


### 예) 다루는 가상 페이지 크기도 크고, 가상 페이지 크기가 크니 실제 OS에서 다룰 페이징 파일도 더 많이 필요할테고….




### 이상 ZGC 의 동작 원리에 대해서 살펴보았습니다. FLO에서는 실제 상용 환경에서 GC 를 ZGC로 변경하여 매우 월등한 응답성을 경험하고 있습니다. 여러분들도 적극 활용해 보시길 권해 드립니다.


<br><br><br><br><br><br>

##### 출처 : https://www.blog-dreamus.com/post/zgc%EC%97%90-%EB%8C%80%ED%95%B4%EC%84%9C
