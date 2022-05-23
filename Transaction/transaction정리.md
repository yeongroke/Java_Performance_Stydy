## Transaction
### 트랜잭션(Transaction 이하 트랜잭션)이란, 데이터베이스의 상태를 변화시키기 해서 수행하는 작업의 단위를 뜻
### (DML 등 명령을 이용하는 것)
### 기본적인 CS지식 Transaction의 ACID개념을 알아두는 것이 좋다. ( ACID개념은 복붙 )
#### 원자성(Atomic) : 하나의 트랜잭션은 모두 하나의 단위로 처리한다. A와 B작업이 하나의 트랜잭셕으로 묶여 있는 경우 A는 성공, B는 실패할 경우 해당 작업단위는 실패로 끝나야 한다. A,B모두 RollBack되야 한다는 원칙
#### 일관성(Consistency) : 트랜잭션이 성공했다면 DB의 모든 데이터는 일관성을 유지해야한다.
#### 격리성(Isolation) : 트랜잭션으로 처리되는 중간에 외부에서의 간섭은 없어야한다.
#### 영속성(Durability) : 트랜잭션이 성공적으로 처리되면 그 결과는 영구적으로 보존되야 한다.
<br><br>

## 트랜잭션 동작 방식을 이해하려면 Spring Aop에 대한 지식이 있어야한다
## AOP는 기본적으로 두가지 방식이 있는데 아래와 같다.
### ( AOP란 관점 지향 프로그래밍으로 각 관점에서 모든 부분에서 계속 반복해서 쓰이는 로직들을 모듈화하여 모듈화 하는 것이다 EX) Transaction )
### ( AspectJ의 AOP도 있으며 Spring AOP와 AspectJ의 AOP는 추후에 글을 쓸 예정이다. ) 
### 간단하게 정리하면 아래와 같다
### AspectJ의 AOP는 AspectJ의 클래스들이 바로 컴파일 되기 때문에 런타임시에는 아무동작도 하지않는다.
### 어떠한 디자인 패턴도 요구하지는 않지만, 코드에 Weaving하기위해서는 AspectJ Compiler(AJC)라는 컴파일러를 도입하였다.
<br>

## Spring AOP는 기본적으로 디자인 패턴 중 하나인 Proxy 패턴을 사용하여 구현되었다.
### CGLIB(bytecode 생성) <- 부트에서 기본
#### 클래스에 대한 Proxy가 가능합니다 ( Spring Container에 의해 관리가 되는 Beans에 적용 가능 )
#### 처음 호출 되었을때 동적으로 bytecode를 생성하여 이후 호출에서는 재사용합니다
#### ( Dynamic Proxy에 비해서 예외(오류) 터지는 경우가 적다 )
### JDK(Dynamic) Proxy (Reflection) <- mvc 기본
#### 인터페이스에 대한 Proxy만을 지원한다
#### JVM에 의해서 Intercept한 다음 invoke 메써드를 호출할 때 JDK의 reflection을 이용하여 호출하는 동작원리
#### reflection를 사용하기 때문에 JDK Proxy를 사용할 때 실행속도를 저하시키는 원인이 된다.
<br>

<img src="https://user-images.githubusercontent.com/42057185/169704332-d72c74ad-9208-406a-a8de-225976a98592.png"/>
<br>

## 공통점으로는
### 대상 클래스가 thread-safe 한 경우 생성된 프록시도 thread-safe하다
<br>

## 트랜잭션 동작 원리는
### @Transactional을 클래스 또는 메소드 필요한 곳에 명시를 하면 Spring AOP를 통해 타겟이 상속하고 있는 타겟(클래스나 메소드 등등) 또는 인터페이스를 상속받은 Proxy객체가 생성이된다.
### 이때 프록시 객체의 메소드를 호출하게 되면 프록시 객체가 가지고 있는 타겟 메소드의 호출과 끝으로 트랜잭션 처리를 수행한다.
<img src="https://user-images.githubusercontent.com/42057185/169704937-6769f573-2ca8-484c-b532-7d59dda00abb.png"/>


### 위 사진과 같이 
### Caller에서 생성된 AOP Proxy를 타고, Proxy를 호출한다
### AOP Proxy는 트랜잭션 Advisor를 호출하며 이 과정에서 커밋이 되거나 롤백이 진행된다
### 또는 Custom Advisor가 있다면 트랜잭션 Advisor를 실행 전후로 동작한다
### Custom Adivsor는 타겟 메소드를 호출하여 비즈니스 로직를 호출하게된다
### 끝난 후에는 순서대로 리턴
<br>

## 전파레벨 (Propagation, 전파옵션)
### REQUIRED	
#### - 기존의 트랜잭션이 존재하면 그 트랜잭션에 같이 탑승! 없다면 새로운 트랜잭션을 시작.
#### - 가장 자주 사용되는 옵션
### SUPPORTS	
#### - 기존의 트랜잭션이 존재하면 그 트랜잭션을 지원, 없다면 비-트랜잭션 상태로 수행
### MANDATORY	
#### - 반드시 Transaction 내에서 메소드가 실행되어야 한다. 없으면 예외발생
### REQUIRES_NEW	
#### - 언제나 새로운 트랜잭션을 수행, 이미 활성화된 트랜잭션이 있다면 일시정지 한다.
### NOT_SUPPORTED	
#### - 새로운 Transaction을 필요로 하지는 않지만, 기존의 Transaction이 있는 경우에는 Transaction 내에서 메소드를 실행한다.
### NEVER	
#### - Manatory와 반대로 Transaction 없이 실행되어야 하며 Transaction이 있으면 예외를 발생시킨다
### NESTED	
#### - 현재의 트랜잭션이 존재하면 중첩된 트랜잭션내에서 실행, 없으면 REQUIRED 처럼 동작
<br><br>

## 격리레벨 (Isolation, 격리수준)
### DEFAULT	
#### - 데이터베이스에 의존
### UNCOMMITTED	
#### - 격리레벨중 가장 낮은 격리 레벨이다.
#### - 다른 Commit되지 않은 트랜잭션에 의해 변경된 데이터를 볼 수 있다.
#### - 거의 트랜잭션의 기능을 수행하지 않는다.
### READ_COMMITTED	
#### - 데이터베이스에서 디폴트로 지원하는 격리 레벨이다.
#### - 다른 트랜잭션에 의해 Commit되지 않은 데이터는 다른 트랜잭션에서 볼 수 없다.
#### - 일반적 으로 가장 많이 사용한다.
### REPEATABLE_READ	
#### - 다른 트랜잭션이 새로운 데이터를 입력했다면 볼 수 있다.
#### - 트랜잭션이 완료 될때까지 SELECT 문장이 사용하는 모든 데이터에 Shared Lock이 걸리므로 다른 사용자는 그 영역에 해당되는 데이터에대한 수정이 불가능하다
#### - ISOLATION_SERIALIZABLE	하나의 트랜잭션이 완료된 후에 다른 트랜잭션이 실행하는 것처럼 지원한다.
#### - 동일한 데이터에 대해서 동시에 두 개 이상의 트랜잭션이 수행 될 수 없다.
<br><br>

### 기본 propagation 값이 PROPAGATION_REQUIRED 라서 @Transactional 이 붙어 있는 메서드가 호출될 때 이미 시작된 Transaction가 있다면 새 Transaction를 만들지 않고 이미 시작된 Transaction에 참여한다.
### 아래와 같이 하는 경우에는 Exception이 터지는 경우 db insert 1, 2, 3, 4단계 모두 롤백이 된다.
<img src="https://user-images.githubusercontent.com/42057185/169098125-0f194538-46e7-4ebc-b0e5-a06f0312b024.png"/>
<br>

### 아래같은 경우에는 noRollbackFor = RuntimeException.class 옵션으로 RuntimeException 경우에는 예외처리를 하여 모든 insert가 롤백이 안된다.
<img src="https://user-images.githubusercontent.com/42057185/169098639-51d14292-505d-4204-8fb2-418a4b35b9b5.png"/>
<br>

### 아래의 경우에는 noRollbackFor = RuntimeException.class, propagation = Propagation.REQUIRES_NEW 두개 옵션으로 RuntimeException 경우에는 예외처리를 하고 Propagation.REQUIRES_NEW으로 상위메서드 트랜잭션에 상관없이 새로운 트랜잭션 생성을 하고 db insert 1, 2가 롤백이 된다.
<img src="https://user-images.githubusercontent.com/42057185/169098886-03828b2c-8502-4aa4-bd07-c5cea77dcdb5.png"/>
<br><br>



