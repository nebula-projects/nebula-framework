/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nebula.framework.core;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;

/**
 * Test class (JUnit4) for FixedPeriodCron.
 *
 * @author ielia
 */
// TODO: Refactor.
public class CronExpressionTest {

  protected static final int DATES = 2;
  protected static final int CRONS = 2;
  protected static final int[] CHECK_FIELDS = {Calendar.DATE, Calendar.MONTH,
                                               Calendar.YEAR, Calendar.HOUR_OF_DAY, Calendar.MINUTE,
                                               Calendar.SECOND};
  CronExpression[] crons;
  Calendar[] referenceDates;
  Calendar[][] previousDates, nextDates;
  Long[][] nextMatchesInMillis, periodsInMillis;

  @Before
  public void setUp() throws Exception {
    /**
     * Crons
     */
    this.crons = new CronExpression[CRONS];
    this.crons[0] = new CronExpression("0 0 0 ? * 1 *");
    this.crons[1] = new CronExpression("0 10-45/15 0 */2 * ? *");

    /**
     * Reference dates
     */
    this.referenceDates = new Calendar[DATES];
                /* Wednesday, 28th of February, 2001, 23:59:00.000 */
    this.referenceDates[0] = Calendar.getInstance();
    this.referenceDates[0].set(Calendar.DATE, 28);
    this.referenceDates[0].set(Calendar.MONTH, Calendar.FEBRUARY);
    this.referenceDates[0].set(Calendar.YEAR, 2001);
    this.referenceDates[0].set(Calendar.HOUR_OF_DAY, 23);
    this.referenceDates[0].set(Calendar.MINUTE, 59);
    this.referenceDates[0].set(Calendar.SECOND, 0);
    this.referenceDates[0].set(Calendar.MILLISECOND, 0);
                /* Saturday, 1st of January, 2000, 12:00:00.000 */
    this.referenceDates[1] = Calendar.getInstance();
    this.referenceDates[1].set(Calendar.DATE, 1);
    this.referenceDates[1].set(Calendar.MONTH, Calendar.JANUARY);
    this.referenceDates[1].set(Calendar.YEAR, 2000);
    this.referenceDates[1].set(Calendar.HOUR_OF_DAY, 12);
    this.referenceDates[1].set(Calendar.MINUTE, 0);
    this.referenceDates[1].set(Calendar.SECOND, 0);
    this.referenceDates[1].set(Calendar.MILLISECOND, 0);

    /**
     * Dates previous to the references, relative to the crons
     */
    this.previousDates = new Calendar[CRONS][DATES];
		/* Sunday, 25th of February, 2001, 00:00:00.000 */
    this.previousDates[0][0] = Calendar.getInstance();
    this.previousDates[0][0].set(Calendar.DATE, 25);
    this.previousDates[0][0].set(Calendar.MONTH, Calendar.FEBRUARY);
    this.previousDates[0][0].set(Calendar.YEAR, 2001);
    this.previousDates[0][0].set(Calendar.HOUR_OF_DAY, 0);
    this.previousDates[0][0].set(Calendar.MINUTE, 0);
    this.previousDates[0][0].set(Calendar.SECOND, 0);
    this.previousDates[0][0].set(Calendar.MILLISECOND, 0);
		/* Sunday, 26th of December, 1999, 00:00:00.000 */
    this.previousDates[0][1] = Calendar.getInstance();
    this.previousDates[0][1].set(Calendar.DATE, 26);
    this.previousDates[0][1].set(Calendar.MONTH, Calendar.DECEMBER);
    this.previousDates[0][1].set(Calendar.YEAR, 1999);
    this.previousDates[0][1].set(Calendar.HOUR_OF_DAY, 0);
    this.previousDates[0][1].set(Calendar.MINUTE, 0);
    this.previousDates[0][1].set(Calendar.SECOND, 0);
    this.previousDates[0][1].set(Calendar.MILLISECOND, 0);
		/* Tuesday, 27th of February, 2001, 00:40:00.000 */
    this.previousDates[1][0] = Calendar.getInstance();
    this.previousDates[1][0].set(Calendar.DATE, 27);
    this.previousDates[1][0].set(Calendar.MONTH, Calendar.FEBRUARY);
    this.previousDates[1][0].set(Calendar.YEAR, 2001);
    this.previousDates[1][0].set(Calendar.HOUR_OF_DAY, 0);
    this.previousDates[1][0].set(Calendar.MINUTE, 40);
    this.previousDates[1][0].set(Calendar.SECOND, 0);
    this.previousDates[1][0].set(Calendar.MILLISECOND, 0);
		/* Saturday, 1st of January, 2000, 00:40:00.000 */
    this.previousDates[1][1] = Calendar.getInstance();
    this.previousDates[1][1].set(Calendar.DATE, 1);
    this.previousDates[1][1].set(Calendar.MONTH, Calendar.JANUARY);
    this.previousDates[1][1].set(Calendar.YEAR, 2000);
    this.previousDates[1][1].set(Calendar.HOUR_OF_DAY, 0);
    this.previousDates[1][1].set(Calendar.MINUTE, 40);
    this.previousDates[1][1].set(Calendar.SECOND, 0);
    this.previousDates[1][1].set(Calendar.MILLISECOND, 0);

    /**
     * Dates after the references, relative to the crons
     */
    this.nextDates = new Calendar[CRONS][DATES];
		/* Sunday, 4th of March, 2001, 00:00:00.000 */
    this.nextDates[0][0] = Calendar.getInstance();
    this.nextDates[0][0].set(Calendar.DATE, 4);
    this.nextDates[0][0].set(Calendar.MONTH, Calendar.MARCH);
    this.nextDates[0][0].set(Calendar.YEAR, 2001);
    this.nextDates[0][0].set(Calendar.HOUR_OF_DAY, 0);
    this.nextDates[0][0].set(Calendar.MINUTE, 0);
    this.nextDates[0][0].set(Calendar.SECOND, 0);
    this.nextDates[0][0].set(Calendar.MILLISECOND, 0);
		/* Sunday, 2nd of January, 2000, 00:00:00.000 */
    this.nextDates[0][1] = Calendar.getInstance();
    this.nextDates[0][1].set(Calendar.DATE, 2);
    this.nextDates[0][1].set(Calendar.MONTH, Calendar.JANUARY);
    this.nextDates[0][1].set(Calendar.YEAR, 2000);
    this.nextDates[0][1].set(Calendar.HOUR_OF_DAY, 0);
    this.nextDates[0][1].set(Calendar.MINUTE, 0);
    this.nextDates[0][1].set(Calendar.SECOND, 0);
    this.nextDates[0][1].set(Calendar.MILLISECOND, 0);
		/* Friday, 1st of March, 2001, 00:10:00.000 */
    this.nextDates[1][0] = Calendar.getInstance();
    this.nextDates[1][0].set(Calendar.DATE, 1);
    this.nextDates[1][0].set(Calendar.MONTH, Calendar.MARCH);
    this.nextDates[1][0].set(Calendar.YEAR, 2001);
    this.nextDates[1][0].set(Calendar.HOUR_OF_DAY, 0);
    this.nextDates[1][0].set(Calendar.MINUTE, 10);
    this.nextDates[1][0].set(Calendar.SECOND, 0);
    this.nextDates[1][0].set(Calendar.MILLISECOND, 0);
		/* Sunday, 3rd of January, 2000, 00:10:00.000 */
    this.nextDates[1][1] = Calendar.getInstance();
    this.nextDates[1][1].set(Calendar.DATE, 3);
    this.nextDates[1][1].set(Calendar.MONTH, Calendar.JANUARY);
    this.nextDates[1][1].set(Calendar.YEAR, 2000);
    this.nextDates[1][1].set(Calendar.HOUR_OF_DAY, 0);
    this.nextDates[1][1].set(Calendar.MINUTE, 10);
    this.nextDates[1][1].set(Calendar.SECOND, 0);
    this.nextDates[1][1].set(Calendar.MILLISECOND, 0);

    /**
     * Next matches in milliseconds
     */
    this.nextMatchesInMillis = new Long[CRONS][DATES];
		/* 3 days + 1 minute */
    this.nextMatchesInMillis[0][0] = (long) 3 * 24 * 60 * 60 * 1000 + 60 * 1000;
		/* 12 hours */
    this.nextMatchesInMillis[0][1] = (long) 12 * 60 * 60 * 1000;
		/* 11 minutes */
    this.nextMatchesInMillis[1][0] = (long) 11 * 60 * 1000;
		/* 1 day + 12 hours + 10 minutes */
    this.nextMatchesInMillis[1][1] = (long) 24 * 60 * 60 * 1000 + 12 * 60 * 60 * 1000 +
                                     10 * 60 * 1000;

    /**
     * Periods in milliseconds
     */
    this.periodsInMillis = new Long[CRONS][DATES];
		/* 7 days */
    this.periodsInMillis[0][0] = (long) 7 * 24 * 60 * 60 * 1000;
		/* 7 days */
    this.periodsInMillis[0][1] = (long) 7 * 24 * 60 * 60 * 1000;
		/* 2 days - 30 minutes */
    this.periodsInMillis[1][0] = (long) 2 * 24 * 60 * 60 * 1000 - 30 * 60 * 1000;
		/* 2 days - 30 minutes */
    this.periodsInMillis[1][1] = (long) 2 * 24 * 60 * 60 * 1000 - 30 * 60 * 1000;
  }

//    @Test
//    public void testGetClosestDateBeforeOrSame() {
//        for (int i = 0; i < CRONS; ++i) {
//            for (int j = 0; j < DATES; ++j) {
//                Calendar result = this.crons[i].getClosestDateBeforeOrSame(
//                        this.referenceDates[j]);
//                /**
//                 * Not comparing dates, but their fields, so the assert will
//                 * tell me which one is wrong.
//                 */
//                for (int field : CHECK_FIELDS) {
//                    assertEquals("i: " + i + ", j: " + j + ", field: " + field,
//                            this.previousDates[i][j].get(field),
//                            result.get(field));
//                }
//            }
//        }
//    }

//    @Test
//    public void testGetClosestDateBeforeOrSameWithNonZeroCronRunMilliseconds() {
//        CronExpression cron = new CronExpression("* * * * * *");
//        Calendar reference = new GregorianCalendar(2000, 0, 1, 0, 1, 0);
//        reference.set(Calendar.MILLISECOND, 1);
//        Calendar result = cron.getClosestDateBeforeOrSame(reference, 0, 0);
//        for (int field : CHECK_FIELDS) {
//            if (field == Calendar.MILLISECOND) {
//                assertEquals("field: " + field, 0, result.get(field));
//            } else {
//                assertEquals("field: " + field, reference.get(field),
//                        result.get(field));
//            }
//        }
//        result = cron.getClosestDateBeforeOrSame(reference, 0, 1);
//        for (int field : CHECK_FIELDS) {
//            assertEquals("field: " + field, reference.get(field),
//                    result.get(field));
//        }
//        result = cron.getClosestDateBeforeOrSame(reference, 0, 2);
//        for (int field : CHECK_FIELDS) {
//            if (field == Calendar.MINUTE) {
//                assertEquals("field: " + field, reference.get(field) - 1,
//                        result.get(field));
//            } else if (field == Calendar.MILLISECOND) {
//                assertEquals("field: " + field, 2, result.get(field));
//            } else {
//                assertEquals("field: " + field, reference.get(field),
//                        result.get(field));
//            }
//        }
//    }

//    @Test
//    public void testGetClosestDateAfter() {
//
//        for (int i = 0; i < CRONS; ++i) {
//            for (int j = 1; j < DATES; ++j) {
//                Calendar result = this.crons[i].getClosestDateAfter(
//                        this.referenceDates[j]);
//                /**
//                 * Not comparing dates, but their fields, so the assert will
//                 * tell me which one is wrong.
//                 */
//                for (int field : CHECK_FIELDS) {
//                    assertEquals("i: " + i + ", j: " + j + ", field: " + field,
//                            this.nextDates[i][j].get(field), result.get(field));
//                }
//            }
//        }
//    }

  @Test
  public void testGetClosestDateAfter() {

    for (int i = 0; i < CRONS; ++i) {
      for (int j = 0; j < DATES; ++j) {

        Calendar result = Calendar.getInstance();
        result.setTime(
            this.crons[i].getNextValidTimeAfter(
                this.referenceDates[j].getTime()));
        /**
         * Not comparing dates, but their fields, so the assert will
         * tell me which one is wrong.
         */
        for (int field : CHECK_FIELDS) {
          assertEquals("i: " + i + ", j: " + j + ", field: " + field,
                       this.nextDates[i][j].get(field), result.get(field));
        }
      }
    }
  }

//
//    @Test
//    public void testGetClosestSecondAfter() {
//        CronExpression cron = new CronExpression("1/2 * * * * *");
//        Calendar reference = new GregorianCalendar(2000, 0, 1, 0, 1, 0);
//        reference.set(Calendar.MILLISECOND, 1);
//        Calendar result = cron.getClosestDateAfter(reference);
//        for (int field : CHECK_FIELDS) {
//            if (field == Calendar.SECOND ) {
//                assertEquals("field: " + field, reference.get(field) + 1,
//                        result.get(field));
//            } else {
//                assertEquals("field: " + field, reference.get(field),
//                        result.get(field));
//            }
//        }
//
//    }

//    @Test
//    public void testNextMatchInMillis() {
//        for (int i = 0; i < CRONS; ++i) {
//            for (int j = 0; j < DATES; ++j) {
//                Long result = this.crons[i].nextMatchInMillis(
//                        this.referenceDates[j]);
//                assertEquals("i: " + i + ", j: " + j,
//                        this.nextMatchesInMillis[i][j], result);
//            }
//        }
//    }

//    @Test
//    public void testPeriodInMillis() {
//        for (int i = 0; i < CRONS; ++i) {
//            for (int j = 0; j < DATES; ++j) {
//                Long result = this.crons[i].periodInMillis(
//                        this.referenceDates[j]);
//                assertEquals("i: " + i + ", j: " + j,
//                        this.periodsInMillis[i][j], result);
//            }
//        }
//    }

//    @Test
//    public void testSameMinuteAsTheMatch() {
//        CronExpression cron = new CronExpression("* * * * *");
//        Calendar reference = new GregorianCalendar(2000, 0, 1, 0, 0, 0);
//        Long nextMatch;
//        Long period = 60000L;
//        for (int i = 1; i < 60; ++i) {
//            reference.set(Calendar.SECOND, i);
//            nextMatch = (long) ((60 - i) % 60) * 1000L;
//            assertEquals("i: " + i, nextMatch,
//                    cron.nextMatchInMillis(reference));
//            assertEquals("i: " + i, period, cron.periodInMillis(reference));
//        }
//        for (int i = 1; i < 60; ++i) {
//            reference.set(Calendar.SECOND, i);
//            nextMatch = (long) ((60 - i) % 60) * 1000L;
//            assertEquals("i: " + i, nextMatch,
//                    cron.nextMatchInMillis(reference));
//            assertEquals("i: " + i, period, cron.periodInMillis(reference));
//        }
//        reference.set(Calendar.SECOND, 0);
//        reference.set(Calendar.MILLISECOND, 1);
//        nextMatch = 59999L;
//        assertEquals(nextMatch, cron.nextMatchInMillis(reference, 0, 0));
//        assertEquals(period, cron.periodInMillis(reference, 0, 0));
//        nextMatch = 60000L;
//        assertEquals(nextMatch, cron.nextMatchInMillis(reference, 0, 1));
//        assertEquals(period, cron.periodInMillis(reference, 0, 1));
//        nextMatch = 1L;
//        assertEquals(nextMatch, cron.nextMatchInMillis(reference, 0, 2));
//        assertEquals(period, cron.periodInMillis(reference, 0, 2));
//    }
//
//
//    @Test
//    public void testMatches() {
//        Calendar reference = new GregorianCalendar(2005, 0, 2, 0, 1, 1);
//        CronExpression cron = new CronExpression("1 * * * 7");
//        assertFalse(cron.matches(reference));
//        assertFalse(cron.matches(reference, true));
//        assertTrue(cron.matches(reference, false));
//        assertFalse(cron.matches(reference, 0, 0, true));
//        assertTrue(cron.matches(reference, 0, 0, false));
//        assertTrue(cron.matches(reference, 1, 0, true));
//        assertTrue(cron.matches(reference, 1, 0, false));
//        assertFalse(cron.matches(reference, 1, 1, true));
//        assertTrue(cron.matches(reference, 1, 0, false));
//        reference.set(Calendar.MINUTE, 2);
//        assertFalse(cron.matches(reference));
//        assertFalse(cron.matches(reference, true));
//        assertFalse(cron.matches(reference, false));
//        assertFalse(cron.matches(reference, 0, 0, true));
//        assertFalse(cron.matches(reference, 0, 0, false));
//        assertFalse(cron.matches(reference, 1, 0, true));
//        assertFalse(cron.matches(reference, 1, 0, false));
//        assertFalse(cron.matches(reference, 1, 1, true));
//        assertFalse(cron.matches(reference, 1, 0, false));
//        reference.set(Calendar.MINUTE, 1);
//        reference.set(Calendar.YEAR, 2006);
//        assertFalse(cron.matches(reference));
//        assertFalse(cron.matches(reference, true));
//        assertFalse(cron.matches(reference, false));
//        assertFalse(cron.matches(reference, 0, 0, true));
//        assertFalse(cron.matches(reference, 0, 0, false));
//        assertFalse(cron.matches(reference, 1, 0, true));
//        assertFalse(cron.matches(reference, 1, 0, false));
//        assertFalse(cron.matches(reference, 1, 1, true));
//        assertFalse(cron.matches(reference, 1, 0, false));
//    }
//
//    @Test
//    public void testThroughoutTheYears() {
//        CronExpression cron = new CronExpression("0 0 6 1 0");
//        Calendar reference = new GregorianCalendar(2000, 0, 6, 0, 0, 0);
//        Calendar beforeOrSame = cron.getClosestDateBeforeOrSame(reference, 0, 0);
//        for (int field : CHECK_FIELDS) {
//            if (field == Calendar.YEAR) {
//                assertEquals("field: " + field, 1991, beforeOrSame.get(field));
//            } else {
//                assertEquals("field: " + field, reference.get(field),
//                        beforeOrSame.get(field));
//            }
//        }
//        cron = new CronExpression("0 0 3 1 0");
//        reference = new GregorianCalendar(2000, 0, 3, 0, 0, 0);
//        Calendar after = cron.getClosestDateAfter(reference, 0, 0);
//        for (int field : CHECK_FIELDS) {
//            if (field == Calendar.YEAR) {
//                assertEquals("[YEAR] field: " + field, 2010, after.get(field));
//            } else {
//                assertEquals("field: " + field, reference.get(field),
//                        after.get(field));
//            }
//        }
//    }
}