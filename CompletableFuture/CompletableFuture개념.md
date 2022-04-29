## CompletableFuture 이란

#### - 자바에서 비동기(Asynchronous) 프로그래밍을 가능하게하는 인터페이스 -> Java 5버전의 Future 제약사항을 해결 <br>
#### - Future와 CompletionStage를 구현하는 구현체
#### - 예외처리를 지원한다.
#### - 순서의 의존관계 설정이 가능한 스레드 프로그래밍
#### - 콜백을 지원하며 여러 스레드를 컨트롤 또는 하나로 묶어 처리가 가능하다.
<br><br>
#### 또한 CompletableFuture의 스레드는 ExecutorService을 사용해서 스레드를 직접 주입해서 사용이 가능하지만<br>
#### 스레드를 직접 관리를 하지 않아도 Java 7 에서 나온 ForkJoinPool을 CompletableFuture 내부에서 사용하여 문제는 없다.


<br><br><br>
<img src="https://user-images.githubusercontent.com/42057185/165993830-15d2e179-0b82-43a4-a966-60e35ae8116b.png"/>
<br><br>

## Future의 제약사항으로는 아래와 같다<br>
#### 1. 예외 처리용 Api를 제공하지 않는다.
#### 2. 여러 Future를 조합할 수가 없다 ( ex: 여러 이벤트 조합, 이벤트에 필요한 정보 동시 조회 )
#### 3. Future를 외부에서 완료시킬수가 없다. 취소 또는 타입아웃 설정은 가능
#### 4. 블로킹 코드(get())을 호출하기 전까지는 Future의 콜백을 얻을 수가 없어서 컨트롤하기 힘듬 -> 이 부분이 제일 오바인거 같음


## CompletableFuture의 자주 쓰이는 메서드로는 아래와 같다<br>
### 비동기로 작업 실행하기 메서드
#### - runAsync() : 리턴 값이 없을 경우
#### - supplyAsync() : 리턴 값이 있을 경우<br>
### 콜백 제공 메서드
#### - thenApply(Function) : 리턴 값을 받아서 다른 값으로 바꾸는 콜백 ( Javascript의 Promise와 비슷 )
#### - thenAccept(Consumer) : 리턴 값을 받아 또 다른 작업을 수행을 하지만 반환 값은 존재하지 않은 콜백
#### - thenRun(Runnable) : 리턴 값을 받지 않고 다른 작업을 수행하는 콜백<br>
#### - 콜백 자체를 다른 메서드에서도 실행이 가능하다
### 다른 CompletableFuture와 조합하는 메서드
#### - thenCompose() : 두 작업이 서로 이어서 실행하도록 조합한다
#### - thenCombine() : 두 작업을 서로 독립적으로 싱행을 하고 둘 다 정상적으로 종료가 되었을 때 콜백한다.
#### - allOf() : 여러 작업을 모두 정상적으로 실행하고 모든 작업결과에 대한 콜백한다.
#### - anyOf() : 여러 작업 중에서 가장 빨리 처리가 되어 오는 결과에 대해서 콜백한다.<br>
### 예외처리 메서드 -> 에러메시지, Exception, 결과 값 컨트롤 가능
#### - exeptionally(Function) : 해당 stage에 도달을 하였을 때, 전달되는 Exception이 있으면 실행되며 다시 콜백하는데 사용 ( 잘 모르겠음 )
#### - handle(BiFunction) : 이전 stage에서 excetpion의 발생 여부에 상관없이 실행된다 ( 개인적으로 에러 메시지 컨트롤 하기엔 더 적합함 )


## CompletableFuture을 어느 때 사용을 하여야 하는가?<br>
#### - 입출력을 기다리는 작업을 병렬로 실행할 때
#### - 비동기로 처리되는 로직을 순차적으로 실행할 때
#### - 비동기로 처리되는 로직을 순차적으로 처리하면서 각 비동기 처리에 대한 결과 값으로 그 다음 무언가를 처리할 때
#### - 콜백지옥을 탈출하기 위해 ㅋㅋ
