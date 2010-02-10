// Modified or written by Luca Marrocco for inclusion with hoptoad.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package code.lucamarrocco.hoptoad;

import org.junit.*;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class NoticeApi2XmlTest {
  @Test
	public void testApiKey() {
		assertThat(xml(), containsString("<api-key>" + TestAccount.KEY + "</api-key>"));
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

  @Test
  public void testSendsRequest() throws Exception {
    HoptoadNoticeBuilder builder = new HoptoadNoticeBuilder(TestAccount.KEY, newThrowable()) {
      {
        setRequest("http://example.com", "carburetor");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("color", "orange");
        map.put("lights", "<blink>");
        session(map);
      }
    };

    String expected = "<request><url>http://example.com</url><component>carburetor</component><session><var key=\"color\">orange</var><var key=\"lights\">&lt;blink&gt;</var></session>";
    assertThat(xml(builder), containsString(expected));
  }

  private RuntimeException newThrowable() {
    return new RuntimeException("errorMessage");
  }

  @SuppressWarnings({"ThrowableInstanceNeverThrown"})
  private String xml() {
    String env = "<blink>production</blink>";
    HoptoadNoticeBuilder builder = new HoptoadNoticeBuilder(TestAccount.KEY, newThrowable(), env);
    return xml(builder);
  }

  private String xml(HoptoadNoticeBuilder builder) {
    HoptoadNotice notice = builder.newNotice();
    NoticeApi2 noticeApi2 = new NoticeApi2(notice);
    return noticeApi2.toString();
  }
}
