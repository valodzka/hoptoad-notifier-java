// Modified or written by Luca Marrocco for inclusion with hoptoad.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package code.lucamarrocco.hoptoad;

import org.junit.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class NoticeApi2XmlTest {

  private HoptoadNoticeBuilder builder;

  @SuppressWarnings({"ThrowableInstanceNeverThrown"})
  @Before
	public void setUp() {
    String env = "<blink>production</blink>";
    builder = new HoptoadNoticeBuilder("apiKey", new RuntimeException("errorMessage"), env);
	}

	@Test
	public void testApiKey() {
		assertThat(xml(), containsString("<api-key>apiKey</api-key>"));
	}

  @Test
	public void testError() {
		assertThat(xml(), containsString("error>"));
	}

	@Test
	public void testErrorBacktrace() {
		assertThat(xml(), containsString("backtrace>"));
	}

	@Test
	public void testErrorBacktraceLine() {
		assertThat(xml(), containsString("<line method=\"org.junit.internal.runners.MethodRoadie.run\" file=\"MethodRoadie.java\" number=\"42\"/>"));
	}

	@Test
	public void testErrorClass() {
		assertThat(xml(), containsString("<class>java.lang.RuntimeException</class>"));
	}

	@Test
	public void testErrorMessage() {
		assertThat(xml(), containsString("<message>errorMessage</message>"));
	}

	@Test
	public void testNoticeVersion() {
		assertThat(xml(), containsString("notice version=\"2.0.0\""));
	}

	@Test
	public void testNotifier() {
		assertThat(xml(), containsString("notifier>"));
	}
	
	@Test
	public void testNotifierName() {
		assertThat(xml(), containsString("<name>hoptoad</name>"));
	}
	
	@Test
	public void testNotifierUrl() {
		assertThat(xml(), containsString("<url>http://hoptoad.googlecode.com</url>"));
	}

	@Test
	public void testNotifierVersion() {
		assertThat(xml(), containsString("<version>1.7-socrata-SNAPSHOT</version>"));
	}

  @Test
  public void testEscapesAngleBrackets() throws Exception {
    assertThat(xml(), containsString("&lt;blink&gt;production&lt;/blink&gt;"));
  }

  private String xml() {
    HoptoadNotice notice = builder.newNotice();
    NoticeApi2 noticeApi2 = new NoticeApi2(notice);
    return noticeApi2.toString();
  }
}
