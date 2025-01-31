package jbook.jshop;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class TestSyi
{
    public static void main(String[] args) {
        new Child();
    }
}

@Slf4j
abstract class Parent {
    Parent() {
        log.info("1) Parent 생성자 시작");

        // ⚠ 문제: 오버라이딩될 가능성이 있는 getInputStream()을 호출
//        InputStream in = getInputStream();
//        log.info("2) Parent 생성자 - getInputStream() 결과: " + in);
//        log.info("2) Parent 생성자 - getInputStream() 결과: " + in);
        log.info("2) Parent 생성자 끝");
    }

    public void init() {
        log.info("[Parent] init() 시작");
        // 자식이 오버라이드한 getInputStream()을 불러도 괜찮음(이미 자식 필드 초기화 끝)
        log.info("[Parent] getInputStream() 결과: " + getInputStream());
        log.info("[Parent] init() 끝");
    }

    // 자식에서 오버라이드할 수도 있는 메서드
    protected abstract InputStream getInputStream();
}

@Slf4j
class Child extends Parent {
    private InputStream input;

    Child() {
        log.info("3) Child 생성자 시작");

        // 여기서야 input이 초기화됨
        this.input = System.in;

        init();

        log.info("4) Child 생성자 - input 초기화 완료: " + input);
        log.info("5) Child 생성자 끝");
    }

    @Override
    protected InputStream getInputStream() {
        log.info("Child::getInputStream() 호출됨");
        return input; // 이 시점에서는 input이 아직 null일 가능성 있음
    }
}

