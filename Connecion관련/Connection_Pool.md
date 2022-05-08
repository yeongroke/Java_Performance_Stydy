## 커넥션 풀 ( Connect Pool , DBCP ) 이란
### WAS가 실행되면서 DB와 연결하는 Connection 객체를 미리 생성을 하는데 이 공간을 Connection Pool이라고 한다.
<br>

### 구조
<img src="https://user-images.githubusercontent.com/42057185/167291086-a1c24aaf-5671-4552-9356-1a5cd4463c89.png"/>
<br>

##### 출처 : https://www.holaxprogramming.com/2013/01/10/devops-how-to-manage-dbcp/
<br>

### 동작 원리
#### WAS가 실행되면서 미리 일정량의 DB Connection 객체를 생성하고 Pool 이라는 공간에 저장해 둔다.
#### HTTP 요청에 따라 필요할 때 Pool에서 Connection 객체를 가져다 쓰고 반환한다.
#### 이와 같은 방식으로 HTTP 요청 마다 DB Driver를 로드하고 물리적인 연결에 의한 Connection 객체를 생성하는 비용이 줄어들게 된다.
<br>

### DB Connection Pool 종류
#### Apache의 Commons DBCP와 Tomcat-JDBC, BoneCP, HikariCP가 있다.
#### ( HikariCP이란 Springboot 2.0부터 default로 설정되어 있는 DB Connection Pool로써 Zero-Overhead가 특징으로 높은 성능을 자랑하는 DB Connection Pool )

#### 추가적으로 웹 어플리케이션의 스레드 수와 DB Connection수를 적절히 조합을 해야한다.. 일반적으로 효율적으로 설계를 하려면 웹 어플리케이션 수에 비해 DB Connection수를 적게 설계한다.
#### 톰캣의 기본 스레드 수는 200개이다.
