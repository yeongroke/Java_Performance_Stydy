## ThreadPool 이란
### 서비스에 적합한 갯수의 Thread를 미리 생성하여 갑자기 많은 요청이 들어왔을 때 아래와 같은 문제점을 방지할 수 있다.
### 제한된 Thread의 갯수를 정해놓고 작업 큐에 들어오는 작업들을 각각 미리 생성된 Thread가 맡는 것이다.
<br>

### 기존 Thread 같은 경우에 갑자기 많은 요청이 들어올 시 요청 만큼 Thread를 생성하게 되며 CPU가 바빠져 메모리 사용량이 급격히 늘어나는 문제점이 있다, 이에 따라서 서비스 성능도 급격히 떨어진다.
<br>



### ThreadPool 동작 구조
<img src="https://user-images.githubusercontent.com/42057185/166980043-02d860bc-8cd5-4586-a161-f783af49fccc.png"/>


<br><br>

### ThreadPool 장점
#### Thread를 사용했을 때 생겼던 생성/수거 비용이 발생하지 않는다.
#### 처음에 생성하는 비용이 발생하지만 작업이 들어왔을 시 이미 Thread가 대기 중인 상태에서 바로 작업을 실행하기 때문에 작업 딜레이가 발생하지 않는다.
#### 서비스 성능 저하를 방지하기 위해서 미리 Thread를 생성한다, 또는 다수의 사용자 요청을 처리하기 위해서 임시적으로 메모리 사용량을 높이지 않는다.
<br>

### ThreadPool 단점
#### 서비스에 적합하지 않게 많은 Thread를 생성할 경우에는 메모리 낭비가 발생한다.
#### ThreadPool을 사용하여 Thread를 미리 생성해서 작업을 분배하고 어느 특정 Thread의 작업이 먼저 끝난 경우에는 특정 Thread는 놀게된다, 이 문제점을 해결하기 위해 나온것이 ForkJoinPool이다.
<br>

<img src="https://user-images.githubusercontent.com/42057185/166982298-61c46f1a-0f34-42a7-9a07-bbbe4830507e.png"/>


#### 위의 사진을 보면 ThreadPool 단점 2번째를 이해할 수 있다.

<br>

### ThreadPool 기본 스레드 - ExecutorService 사용
#### 초기 스레드 수 : ExecutorService 객체가 생성될 때 기본적으로 생성되는 스레드 수
#### 코어 스레드 수 : 스레드가 증가한 후 사용되지 않은 스레드를 스레드 풀에서 제거할 때 최소한으로 유지해야할 수
#### 최대 스레드 수 : 스레드풀에서 관리하는 최대 스레드 수
<br>

### ThreadPool 생성 및 사용 메서드 - ExecutorService 사용
#### newCachedThreadPool()
##### 초기스레드 수, 코어스레드 수 0개 최대 스레드 수는 integer 데이터타입이 가질 수 있는 최대 값(Integer.MAX_VALUE)
##### 스레드 개수보다 작업 개수가 많으면 새로운 스레드를 생성하여 작업을 처리한다.
##### 만약 일 없이 60초동안 아무일을 하지않으면 스레드를 종료시키고 스레드풀에서 제거한다.
<br>

#### newFixedThreadPool(int nThreads)
##### 초기 스레드 개수는 0개 ,코어 스레드 수와 최대 스레드 수는 매개변수 nThreads 값으로 지정,
##### 이 스레드 풀은 스레드 개수보다 작업 개수가 많으면 마찬가지로 스레드를 새로 생성하여 작업을 처리한다.
##### 만약 일 없이 놀고 있어도 스레드를 제거하지 않고 내비둔다.  
<br>

#### newCachedThreadPool(),newFixedThreadPool() 메서드를 사용하지 않고 직접 스레드 개수들을 설정하고 싶다면
#### 직접 ThreadPoolExecutor 객체를 생성하면 된다. 
<br>

### ExecutorService 종료 메서드
#### 스레드 풀에 속한 스레드는 기본적으로 데몬스레드(주 스레드를 서포트하기 위해 만들어진 스레드, 주 스레드 종료시 강제 종료)가 아니기 때문에 main 스레드가 종료되어도 작업을 처리하기 위해 계속 실행 상태로 남아있다. 즉 main() 메서드가 실행이 끝나도 어플리케이션 프로세스는 종료되지 않는다. 어플리케이션 프로세스를 종료하기 위해선 스레드 풀을 강제로 종료시켜 스레드를 해체시켜줘야 한다. 
#### ExecutorService 구현객체에서는 기본적으로 3개 종료 메서드를 제공한다.
<br>

#### excutorService.shutdown();
##### 작업큐에 남아있는 작업까지 모두 마무리 후 종료 (오버헤드를 줄이기 위해 일반적으로 많이 사용.)
<br>

#### excutorService.shoutdownNow();
##### 작업큐 작업 잔량 상관없이 강제 종료
<br>

#### excutorService.awaitTermination(long timeout, TimeUnit unit);
##### 모든 작업 처리를 timeout 시간안에 처리하면 true 리턴 ,처리하지 못하면 작업스레드들을 interrupt()시키고 false리턴
