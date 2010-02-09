// Modified or written by Luca Marrocco for inclusion with hoptoad.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package code.lucamarrocco.hoptoad;

import org.junit.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class NoticeApi2XmlTest {

	private HoptoadNotice notice;

	private String clean(String string) {
		return string.replaceAll("\\\"", "");
	}

	@SuppressWarnings({"ThrowableInstanceNeverThrown"})
  @Before
	public void setUp() {
    String env = "<blink>production</blink>";
    HoptoadNoticeBuilder builder = new HoptoadNoticeBuilder("apiKey", new RuntimeException("errorMessage"), env);
    notice = builder.newNotice();
	}

	@Test
	public void testApiKey() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<api-key>apiKey</api-key>"));
	}

	@Test
	public void testError() {
		assertThat(xml(new NoticeApi2(notice)), containsString("error>"));
	}

	@Test
	public void testErrorBacktrace() {
		assertThat(xml(new NoticeApi2(notice)), containsString("backtrace>"));
	}

	@Test
	public void testErrorBacktraceLine() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<line method=org.junit.internal.runners.MethodRoadie.run file=MethodRoadie.java number=42/>"));
	}

	@Test
	public void testErrorClass() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<class>java.lang.RuntimeException</class>"));
	}

	@Test
	public void testErrorMessage() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<message>errorMessage</message>"));
	}

	@Test
	public void testNoticeVersion() {
		assertThat(xml(new NoticeApi2(notice)), containsString("notice version=2.0"));
	}

	@Test
	public void testNotifier() {
		assertThat(xml(new NoticeApi2(notice)), containsString("notifier>"));
	}
	
	@Test
	public void testNotifierName() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<name>hoptoad</name>"));
	}
	
	@Test
	public void testNotifierUrl() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<url>http://hoptoad.googlecode.com</url>"));
	}

	@Test
	public void testNotifierVersion() {
		assertThat(xml(new NoticeApi2(notice)), containsString("<version>1.7-socrata-SNAPSHOT</version>"));
	}

  @Test
  public void testEscapesAngleBrackets() throws Exception {
    String actual = xml(new NoticeApi2(notice));
    assertThat(actual, containsString("&lt;blink&gt;production&lt;/blink&gt;"));
  }

	private String xml(NoticeApi2 noticeApi) {
		return clean(noticeApi.toString());
	}
}
