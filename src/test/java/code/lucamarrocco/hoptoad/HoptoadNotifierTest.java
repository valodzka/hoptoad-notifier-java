// Modified or written by Luca Marrocco for inclusion with hoptoad.
// Copyright (c) 2009 Luca Marrocco.
// Licensed under the Apache License, Version 2.0 (the "License")

package code.lucamarrocco.hoptoad;

import org.apache.commons.logging.*;
import org.hamcrest.*;
import org.junit.*;

import java.util.*;

import static code.lucamarrocco.hoptoad.Exceptions.*;
import static code.lucamarrocco.hoptoad.Slurp.*;
import static java.util.Arrays.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class HoptoadNotifierTest {

  protected static final Map<String, Object> ENVIRONMENT = new HashMap<String, Object>();

	private final Log logger = LogFactory.getLog(getClass());

	private final Map<String, Object> EC2 = new HashMap<String, Object>();

	private HoptoadNotifier notifier;

  private static final int RATE_LIMITED_RESPONSE = 503;

  private <T> Matcher<T> internalServerError() {
		return new BaseMatcher<T>() {
			public void describeTo(final Description description) {
				description.appendText("internal server error");
			}

			public boolean matches(final Object item) {
				return item.equals(500);
			}
		};
	}

	@Before
	public void setUp() {
		ENVIRONMENT.put("A_KEY", "test");
		EC2.put("AWS_SECRET", "AWS_SECRET");
		EC2.put("EC2_PRIVATE_KEY", "EC2_PRIVATE_KEY");
		EC2.put("AWS_ACCESS", "AWS_ACCESS");
		EC2.put("EC2_CERT", "EC2_CERT");
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
    final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, newException(ERROR_MESSAGE), "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testNotifyToHoptoadUsingBuilderAndSessionVars() throws InterruptedException {
    final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, newException(ERROR_MESSAGE), "test") {
      {
        setRequest("http://localhost:3000/", "controller");
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("color", "orange");
        session(map);
      }
    }.newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testNotifyToHoptoadUsingBuilderNoticeFromExceptionInEnvAndSystemProperties() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, EXCEPTION, "test") {
			{
				filteredSystemProperties();
			}

		}.newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testNotifyToHoptoadUsingBuilderNoticeInEnv() throws InterruptedException {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, ERROR_MESSAGE, "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendExceptionNoticeWithFilteredBacktrace() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, new QuietRubyBacktrace(), EXCEPTION, "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendExceptionToHoptoad() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, EXCEPTION).newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendExceptionToHoptoadUsingRubyBacktrace() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, new RubyBacktrace(), EXCEPTION, "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

  @Test
	public void testSendExceptionToHoptoadUsingRubyBacktraceAndFilteredSystemProperties() throws InterruptedException {
		final Exception EXCEPTION = newException(ERROR_MESSAGE);
		final HoptoadNotice notice = new HoptoadNoticeBuilderUsingFilterdSystemProperties(TestAccount.KEY, new RubyBacktrace(), EXCEPTION, "test").newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendNoticeToHoptoad() throws InterruptedException {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, ERROR_MESSAGE).newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendNoticeWithFilteredBacktrace() throws InterruptedException {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, ERROR_MESSAGE) {
			{
				backtrace(new QuietRubyBacktrace(strings(slurp(read("backtrace.txt")))));
			}
		}.newNotice();

    assertNoticeReturnsSuccess(notice);
  }

	@Test
	public void testSendNoticeWithLargeBacktrace() throws InterruptedException {
		final HoptoadNotice notice = new HoptoadNoticeBuilder(TestAccount.KEY, ERROR_MESSAGE) {
			{
				backtrace(new Backtrace(strings(slurp(read("backtrace.txt")))));
			}
		}.newNotice();

    assertNoticeReturnsSuccess(notice);
  }

  private void assertNoticeWithBacktraceReturnsSuccess(final String backtraceLine) throws InterruptedException {
    HoptoadNoticeBuilder builder = new HoptoadNoticeBuilder(TestAccount.KEY, ERROR_MESSAGE) {
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
