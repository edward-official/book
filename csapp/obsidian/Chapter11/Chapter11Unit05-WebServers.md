#### 11.5.1 Web Basics
웹 클라이언트와 서버는 HTTP(Hypertext Transfer Protocol) 이라는 텍스트 기반의 응용 계층 프로토콜을 사용해 상호작용합니다.

HTTP는 매우 단순한 프로토콜입니다.
웹 클라이언트(보통 브라우저)는 인터넷을 통해 서버에 접속하고 일정한 콘텐츠(content) 를 요청합니다.
서버는 요청받은 콘텐츠를 응답한 뒤 연결을 닫습니다.
브라우저는 이 응답을 받아 화면에 표시합니다.


##### HTML과 하이퍼링크

웹 서비스가 ==FTP 같은 단순 파일 서비스와 다른 점==은, 웹 콘텐츠가 HTML(Hypertext Markup Language) 로 작성된다는 것입니다.

HTML은 브라우저가 텍스트나 그래픽을 어떻게 보여줄지를 지시하는 태그(tag) 들을 포함합니다.

예를 들어,

```html
<b>Make me bold!</b>
```

는 `<b>`와 `</b>` 사이의 문장을 굵게 표시하라는 뜻입니다.

HTML의 진짜 강점은 다른 콘텐츠로의 연결(hyperlink) 을 포함할 수 있다는 것입니다.
예를 들어,

```html
<a href="http://www.cmu.edu/index.html">Carnegie Mellon</a>
```

은 브라우저에게 “Carnegie Mellon”이라는 문구를 클릭 가능한 링크로 표시하고,
그 링크가 `index.html`이라는 HTML 파일로 연결되도록 합니다.


##### [Aside] 월드 와이드 웹의 기원

월드 와이드 웹(World Wide Web) 은 스위스 CERN의 물리학자 팀 버너스-리(Tim Berners-Lee) 가 발명했습니다.

1989년 그는 ==“링크로 연결된 메모의 웹”== 개념을 제시했고, ==CERN 과학자들이 정보를 공유할 수 있도록 시스템==을 설계했습니다.

1993년, Marc Andreessen이 만든 Mosaic 브라우저가 등장하면서 웹은 폭발적으로 성장했습니다.
2015년에는 웹사이트 수가 9억 7천 5백만 개를 돌파했습니다.


#### 11.5.2 Web Content
![[Chapter11/Figure11-23.png]]
웹 콘텐츠(content)는 바이트(byte)의 연속된 흐름이며, 각 콘텐츠에는 ==MIME 타입==이 지정됩니다.
웹 서버는 두 가지 방식으로 콘텐츠를 제공합니다:

1. 정적 콘텐츠(static content)
   → 디스크의 파일을 읽어서 그대로 클라이언트에 전송
   (예: HTML, 이미지 파일 등)

2. 동적 콘텐츠(dynamic content)
   → 실행 파일을 실행하여 그 출력 결과를 클라이언트에 전송
   (예: CGI 프로그램)


##### URL (Uniform Resource Locator)

웹 서버가 관리하는 각 파일에는 고유한 URL이 있습니다.

예를 들어:

```
http://www.google.com:80/index.html
```

이 URL은

* `www.google.com` 서버의
* 80번 포트에서
* `index.html` 파일을 요청한다는 뜻입니다.


동적 콘텐츠의 경우, URL에 프로그램 인자(arguments) 를 포함할 수도 있습니다.

예시:

```
http://bluefish.ics.cs.cmu.edu:8000/cgi-bin/adder?15000&213
```

이 URL은 `/cgi-bin/adder` 프로그램을 실행하고
`15000`과 `213`을 인자로 전달합니다.


##### URL 해석 규칙 요약

* URL 접미사에 대한 표준 규칙은 없습니다. 각 서버가 자체적으로 관리합니다.
* `/` 는 루트 디렉터리가 아니라 웹 서버의 홈 디렉터리를 의미합니다.
* `/index.html` 은 기본 홈 페이지로 확장됩니다.


#### 11.5.3 HTTP Transactions
HTTP는 텍스트 기반 프로토콜이므로 ==`telnet`== 명령어를 사용해 웹 서버와 직접 대화할 수 있습니다.

##### Figure 11.24

![[Figure11-24.png]]
이것이 HTTP 트랜잭션의 기본 구조입니다:

* 요청(request): 클라이언트 → 서버
* 응답(response): 서버 → 클라이언트


##### HTTP 요청 (HTTP Requests)

HTTP 요청은 다음과 같은 형식입니다:

```
method URI version
```

예:

```
GET /index.html HTTP/1.1
```

* method: GET, POST, PUT, DELETE 등
* URI: 요청 리소스의 경로
* version: HTTP 버전 (예: 1.1)

요청에는 추가적인 헤더(header) 들이 올 수 있습니다.
대표적인 예시는 `Host:` 헤더입니다.


##### HTTP 응답 (HTTP Responses)

HTTP 응답은 다음과 같은 구조를 가집니다:

```
version status-code status-message
```

예:

```
HTTP/1.1 200 OK
```


##### Figure 11.25
![[Figure11-25.png]]


#### 11.5.4 Serving Dynamic Content
서버가 동적 콘텐츠를 제공할 때 생기는 핵심 질문들:

* 클라이언트는 인자를 어떻게 서버로 전달하나?
* 서버는 이 인자를 자식 프로세스에게 어떻게 넘기나? ==(이 질문 자체가 이해가 안되는데??)==
* 생성된 결과는 어디로 보내나?

이 모든 문제는 ==CGI(Common Gateway Interface)== 표준으로 해결합니다.


##### 클라이언트 → 서버 인자 전달

GET 요청에서는 `?`와 `&`로 인자를 구분합니다.

```
GET /cgi-bin/adder?15000&213 HTTP/1.1
```


##### 서버 → 자식 프로세스 인자 전달
![[Figure11-26.png]]
서버는 CGI 프로그램을 실행할 때
환경 변수(environment variables)를 설정하여 인자를 전달합니다.


##### CGI 프로그램의 출력

CGI 프로그램은 표준 출력(stdout) 으로 결과를 보내며,
이 내용이 그대로 클라이언트에게 전달됩니다.


##### Figure 11.27
![[Figure11-27.png]]

##### Figure 11.28
![[Figure11-28.png]]
