## ForkJoinPool
### 우선 ForkJoinPool이란 ThreadPool의 단점인 먼저 작업이 끝나 노는 Thread가 발생하는 문제점을 보완하기 위해서 나왔다..!!
### ThreadPool은 하나의 작업을 Thread 각각마다 할당을 해주었다, ForkJoinPool은 하나의 작업을 쪼갠다음에 각각의 Thread에게 일을 할당한다.
<br>

### 동작 방법
#### 1. 3개의 A, B, C Thread가 있다
#### 2. A의 Thread가 하나의 잡을 잡아서 자신의 로컬 큐에 1차로 분할작업을 한다.
#### 3. B의 Thread가 A의 Thread의 로컬 큐에 있는 잡을 훔쳐서 일을 한다.
#### 4. B의 Thread의 잡이 많아서 분할작업을 한다.
#### 5. C의 Thread가 B의 Thread의 로컬 큐에 있는 잡을 훔쳐서 일을 한다.
#### 6. 이로써 모든 Thread가 일을 종료하는 시간이 비슷해진다.
<br>

#### 아래 이미지와 같다.
<img src="https://user-images.githubusercontent.com/42057185/167151293-499fc64b-f01d-4402-bb9a-9fb631df067b.png"/>
<br><br>


### 하지만 ForkJoinPool을 쓴다고 무조건 좋은 것은 아니다
#### 모든 잡이 동일한 경우에는 ForkJoinPool의 Thread의 작업을 분배하는 시간이 추가되어 ThreadPool보다 시간이 더 오래 걸릴수있다!!
<br>

#### java의 stream parallel이나 completableFuture 등이 ForkJoinPool 방식을 기본으로 사용하고 있다.
