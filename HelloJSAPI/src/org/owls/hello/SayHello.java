package org.owls.hello;

import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineModeDesc;
import javax.speech.EngineStateError;
import javax.speech.recognition.GrammarException;
import javax.speech.recognition.Recognizer;
import javax.speech.recognition.Result;
import javax.speech.recognition.ResultAdapter;
import javax.speech.recognition.ResultEvent;
import javax.speech.recognition.ResultToken;
import javax.speech.recognition.RuleGrammar;

// javax.speech(JSAPI) 는 CMUSphinx 를 설치해야만 사용할 수 있음.
// 1. Sphinx 의 bin.tar 를 해제해서 안에 있는 lib 폴더로 이동 
// 2. jsapi.sh 실행 (권한 변경 필요)
// 3. 생성된 jsapi.jar 임포트  

// ResultAdapter 는 Result 객체를 통한 결과값을 처리하는 클래스 
// 상속하는 메소드들은 실제로 아무것도 하지 않지만 개발 편의를 위해 정의되어 있음
public class SayHello extends ResultAdapter {
		
	// 대략 Grammer에 따라 Result를 반환하는 역할인듯 
	private static Recognizer rec;
	
	// 사용될 grammar 파일의 위치 - 원본은 args[0] 이었는데 변경.
	private static final String GRAMMAR = "test";
	
	// ResultAdapter로부터 상속받은 메소드 
	@Override
	public void resultAccepted(ResultEvent event) {
		
		// Result 객체는 Recognizer 에서 들어오는 말 중 정의된 Grammar 와 일치하는 것을 인식. 
		// 상태값 종류 :
		// - UNFINALIZED : 작업 중 
		// - ACCEPTED : 인식 결과 분석 완료 + 패턴 일치 
		// - REJECTED : 인식 결과 분석 완료 + 패턴 불일치 
		Result result = (Result) event.getSource();
		
		// ResultToken 은 Spoken-form text 정보를 가지고 있음 
		ResultToken tokens[] = result.getBestTokens();
		
		// 들어온 구두 문자열(Spoken-form text) 을 출력  
		for(int i = 0; i < tokens.length; i++) {
			System.out.println(tokens[i].getSpokenText() + " ");
		}
		
		// Recognizer 해제 후 프로그램 종료 
		try {
			rec.deallocate();
		} catch (EngineException | EngineStateError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}


	public static void main(String[] args) {
		
		try {
			
			System.out.println("Voice Recognition Test.");
			
			// Recognizer 인터페이스 초기화 
			rec = Central.createRecognizer(
					new EngineModeDesc(Locale.ENGLISH));
			
			// Recognizer 인터페이스는 Engine 인터페이스를 상속, 
			// allocate 함수는 Engine 으로부터 상속받은 메소드 
			
			System.out.println("Recognizer Object : " + rec);
			
			// Engine에 필요한 리소스를 분배함. 성공한다면 Engine 에  ALLOCATED 비트가 새팅됨.
			// 확인은 testEngineState(Engine.ALLOCATED) 로 boolean 체크 
			rec.allocate();
			
			FileReader reader = new FileReader(GRAMMAR);
			
			// JSGF - Java Speech Grammar Format
			// API 에 의하면 RuleGrammer 인터페이스는 반드시 Recognizer 를 통해서 생성/관리되어야 함. 
			RuleGrammar grammar = rec.loadJSGF(reader);
			
			// 이 옵션이 활성화되어 있어야 유저의 입력을 받을 수 있음 
			grammar.setEnabled(true);
			
			// 결과값을 처리할 리스너 새팅 
			rec.addResultListener(new SayHello());
			
			// Recognizer 가 생성된 이후 변경된 사항들을 반영 
			rec.commitChanges();
			
			// 유저가 어플리케이션에 말할 때 포커스를 변경 
			rec.requestFocus();
			
			// 정지 상태(pause) 의 엔진이 오디오 스트림을 받을 수 있도록 변경
			// Engine에 연결된 모든 어플리케이션의 상태가 변경됨 
			rec.resume();
			
		} catch (IllegalArgumentException | EngineException | SecurityException | 
				GrammarException | EngineStateError | IOException | 
				AudioException e) {
			//첫 줄은 Central.createRecognizer 의 Exception
			//두번째 줄은 RuleGrammer의 Exception 
			//세번째 줄은 Recognizer.resume 의 Exception
			e.printStackTrace();
		} 
	}
};