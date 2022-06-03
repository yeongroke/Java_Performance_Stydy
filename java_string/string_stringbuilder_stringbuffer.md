## String
### 새로운 값을 할당할 때마다 새로 클래스에 대한 객체가 생성된다.
### 값 변경 불가능(inmutable), 값을 변경하면 기존에 있던 String Stack 메모리의 주소가 사라져 GC에 쌓이게되면서 새로운 메모리를 생성한다
### 각각의 String 주소값이 Stack영역에 쌓인다 
### Garbage Collector가 호출되기 전까지 생성된 String 객체들의 메모리는 Heap영역에 쌓이기 때문에 메모리가 매우 불필요하게 생긴다
### String을 반복문 돌려서 자주 값을 더하거나 변경하는 경우에는 StringBuffer나 StringBuilder를 사용하는 것이 좋다.
<br>

## StringBuilder, StringBuffer
### 처음에 생성된 memory에 append하는 방식으로, 클래스에 대한 객체를 직접 생성하지 않음
<br>

## StringBuilder
### 변경가능한(mutable) 문자열
### 비동기 처리
<br>

## StringBuffer
### 변경가능한(mutable) 문자열
### 동기 처리
### multiple thread 환경에서 안전한 클래스(thread safe)
<br>

## Java 버전별 String 변경 사항
### JDK 1.5 버전 이전에서는 문자열 연산(+ 또는 concat 등)을 할 때 조합하는 문자열을 새로운 메모리에 할당하여 참조해 성능상의 이슈 존재
### JDK 1.5 버전 이후에는 컴파일 단계에서 String 객체를 StringBuilder로 컴파일 되도록 변경됨
### 그래서 JDK 1.5 이후 버전에서는 String 클래스를 사용해도 StringBuilder와 성능 차이는 없다
### 하지만 반복문을 사용해서 문자열을 더하거나 변경하는 경우에는 객체를 계속 새로운 메모리에 할당함
### String 클래스를 사용하는 것 보다는 스레드와 관련이 있으면 StringBuffer, 스레드 안전 여부와 상관이 없으면 StringBuilder를 사용하는 것을 권장한다
