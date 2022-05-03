# Thread 란 ( 스레드 )
<br><br>
## 개념 - 일반적으로 아래와 같이 말한다.. 넘 딱딱하긴하지만.. 아래 설명과 같다..
### thread란 하나의 프로세스에서 단독적으로 실행되는 하나의 작업 단위 또는 하나의 공간으로 볼 수 있습니다.
#### 메모리 구조는 code, data, stack, heap으로 4군데로 나눠져있으며 이중에 stack영역만 공유하고있다.
<br><br>
## Thread 생성 방법
<br>

### Thread 사용
<img src="https://user-images.githubusercontent.com/42057185/166498033-c4a50bb1-9388-4a81-a6b8-cc74e69b0424.png"/>
<br><br>

### Runnable 사용
<img src="https://user-images.githubusercontent.com/42057185/166496813-b6b5cc73-bfc3-4a40-b598-3b564baa84e5.png"/>

## 동시성 문제 해결 방법
### 스레드 
### Synchronized 선언방식
#### 동작원리 : 하나의 블럭이 만들어지는 걸로 동시에 하나의 스레드만 접근이 가능하다, 즉 실행이 끝날 때까지는 다른 스레드는 접근이 불가능하다.
### volatile 선언방식
#### 동작원리 : 선언한 영역에서 
##### volatile 키워드란 CPU메모리 영역에서 캐싱된(작업된) 값이 아니라 항상 최신의 값을 가지고 오도록한다.
##### 문제점 : 모든 동기화 문제가 해결되는 것은 아니고 단지 최신 값을 보여주는 것이다.
