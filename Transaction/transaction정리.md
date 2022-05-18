## Transaction
### 트랜잭션(Transaction 이하 트랜잭션)이란, 데이터베이스의 상태를 변화시키기 해서 수행하는 작업의 단위를 뜻
### (DML 등 명령을 이용하는 것)
<br><br>

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



