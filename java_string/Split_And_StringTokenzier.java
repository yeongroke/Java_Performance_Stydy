// Yeongroke


/*

위의 코드를 실행할 경우

java 공홈에서도 split을 권장하고 있고 StringTokenzier은 레거시라고 부르고 있는 추세이다

하지만 직접 찾아본 결과로는 아래의 글은 StringTokenzier을 분석해 준 글을 보게 되었다.

http://blog.naver.com/PostView.nhn?blogId=chogahui05&logNo=221474002967&categoryNo=12&parentCategoryNo=0&viewDate=&currentPage=1&postListTopCurrentPage=1&from=postView

위의 블로그에 나와있듯이 StringTokenzier의 내부 함수들이 구분자와 문자를 일일이 비교하고 

구분자가 유니코드일 경우, nextToken 또는 hasMoreToke을 호출 시 문자열과 구분자 전체를 비교하는 로직이 있어 효율이 좋지 않다. 

따라서 여러 구분자를 세팅하고 구분자가 유니코드일 경우, 즉 hasMoreToke와 nestToken을 많이 찾을수록 성능이 확 나빠지는 것이다.

하지만 Split을 자주 쓴다고 마냥 좋은 것은 아니다 

그 이유는 split를 내부를 직접 분석하며 보면 아래와 정규 표현식을 이용하기 때문에 때문에 많이 호출하게 되면 성능이 떨어지게 된다.


결론은 성능은 Split이 더 좋지만 많이 호출하는 것은 지양하는 것이 좋다.

*/

public static void main(String[] args) {
    StringBuilder sb = new StringBuilder();
    for(int i = 1; i < 1000; i++) sb.append(i).append(' ');
    String sample = sb.toString();
    int runs = 100000;
    for(int i = 0; i < 5; i++) {
        {
            long start = System.nanoTime();
            for(int r = 0; r < runs; r++) {
                StringTokenizer st = new StringTokenizer(sample);
                List<String> list = new ArrayList<>();
                while (st.hasMoreTokens()) list.add(st.nextToken());
            }
            long time = System.nanoTime() - start;
            System.out.printf("StringTokenizer took an average of %.1f us%n", time / runs / 1000.0);
        }
        {
            long start = System.nanoTime();
            for(int r = 0; r < runs; r++) {
                List<String> list = Arrays.asList(sample.split(" "));
            }
            long time = System.nanoTime() - start;
            System.out.printf("split took an average of %.1f us%n", time / runs / 1000.0);
        }
        {
            long start = System.nanoTime();
            for(int r = 0; r < runs; r++) {
                List<String> list = new ArrayList<String>();
                int pos = 0, end;
                while ((end = sample.indexOf(' ', pos)) >= 0) {
                    list.add(sample.substring(pos, end)); pos = end + 1;
                }
            }
            long time = System.nanoTime() - start;
            System.out.printf("indexOf loop took an average of %.1f us%n", time / runs / 1000.0);
        }
    }
}
