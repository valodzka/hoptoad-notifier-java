// Modified or written by Luca Marrocco for inclusion with hoptoad.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package code.lucamarrocco.hoptoad;

import org.apache.commons.logging.*;
import org.junit.*;

import static code.lucamarrocco.hoptoad.Exceptions.*;
import static code.lucamarrocco.hoptoad.Slurp.*;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class HoptoadNotifierTest {

	private final Log logger = LogFactory.getLog(getClass());

	private HoptoadNotifier notifier;

  private static final int RATE_LIMITED_RESPONSE = 503;
  public static final String KEY = "a7bad952a319d10540fbbd64b597260d";

  @Before
	public void setUp() {
		notifier = new HoptoadNotifier();
	}

	@Test
	public void testHowBacktraceHoptoadNotInternalServerError() throws InterruptedException {
		assertNoticeWithBacktraceReturnsSuccess(ERROR_MESSAGE);
		assertNoticeWithBacktraceReturnsSuccess("java.lang.RuntimeException: an expression is not valid");
		assertNoticeWithBacktraceReturnsSuccess("Caused by: java.lang.NullPointerException");
		assertNoticeWithBacktraceReturnsSuccess("at code.lucamarrocco.notifier.Exceptions.newException(Exceptions.java:11)");
		assertNoticeWithBacktraceReturnsSuccess("... 23 more");
	}

	@Test
	public void testLogErrorWithException() {
		logger.error("error", newException(ERROR_MESSAGE));
	}

	@Test
	public void testLogErrorWithoutException() {
		logger.error("error");
	}

	@Test
	public void testLogThresholdLesserThatErrorWithExceptionDoNotNotifyToHoptoad() {
		logger.info("info", newException(ERROR_MESSAGE));
		logger.warn("warn", newException(ERROR_MESSAGE));
	}

	@Test
	public void testLogThresholdLesserThatErrorWithoutExceptionDoNotNotifyToHoptoad() {
		logger.info("info");
		logger.warn("warn");
	}

	@Test
	public void testNotifyToHoptoadUsingBuilderNoticeFromExceptionInEnv() throws InterruptedException {
    final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, newException(ERROR_MESSAGE), "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testNotifyToHoptoadUsingBuilderAndSessionVars() throws InterruptedException {
    final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, newException(ERROR_MESSAGE), "test") {
      {
        setRequest("http://localhost:3000/", "controller");
        addSessionKey("color", "orange");
      }
    }.newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testNotifyToHoptoadUsingBuilderNoticeFromExceptionInEnvAndSystemProperties() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, EXCEPTION, "test") {
			{
				filteredSystemProperties();
			}
		}.newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testNotifyToHoptoadUsingBuilderNoticeInEnv() throws InterruptedException {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, ERROR_MESSAGE, "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendExceptionNoticeWithFilteredBacktrace() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, new QuietRubyBacktrace(), EXCEPTION, "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendExceptionToHoptoad() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, EXCEPTION).newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendExceptionToHoptoadUsingRubyBacktrace() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, new RubyBacktrace(), EXCEPTION, "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

  @Test
	public void testSendExceptionToHoptoadUsingRubyBacktraceAndFilteredSystemProperties() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilderUsingFilteredSystemProperties(KEY, new RubyBacktrace(), EXCEPTION, "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendNoticeToHoptoad() throws InterruptedException {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, ERROR_MESSAGE).newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendNoticeWithFilteredBacktrace() throws InterruptedException {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, ERROR_MESSAGE) {
			{
				backtrace(new QuietRubyBacktrace(strings(slurp(read("backtrace.txt")))));
			}
		}.newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendNoticeWithLargeBacktrace() throws InterruptedException {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(KEY, ERROR_MESSAGE) {
			{
				backtrace(new Backtrace(strings(slurp(read("backtrace.txt")))));
			}
		}.newNotice();

    assertNoticeReturnsSuccess(notice);
  }

  private void assertNoticeWithBacktraceReturnsSuccess(final String backtraceLine) throws InterruptedException {
    HoptoadNoticeBuilder builder = new HoptoadNoticeBuilder(KEY, ERROR_MESSAGE) {
      {
        backtrace(new Backtrace(asList(backtraceLine)));
      }
    };

    assertNoticeReturnsSuccess(builder.newNotice());
  }

  private void assertNoticeReturnsSuccess(HoptoadNotice notice) throws InterruptedException {
    int attempts = 5;
    int secondsToWait = 20;

    while (true) {
      assertTrue("hoptoadapp.com returned failure after multiple attempts", attempts > 0);

      int response = notifier.notify(notice);
      if (response == RATE_LIMITED_RESPONSE) {
        System.err.println("hoptoadapp.com returned rate limited response, waiting " + secondsToWait + " seconds...");
        Thread.sleep(secondsToWait * 1000);
        attempts--;
      } else {
        assertThat(response, is(200));
        break;
      }
    }
  }
}
