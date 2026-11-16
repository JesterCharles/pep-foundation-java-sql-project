package com.pep.toolshare;

import com.pep.toolshare.basics.LendingMath;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Week 2: types, operators, control flow, strings, methods, recursion, arrays, lists and maps. */
class LendingMathCompletionTest {
    @Test
    void lateFeeMultipliesDaysByRate() {
        assertEquals(300, LendingMath.lateFeeCents(3, 100, 2_000));
        assertEquals(25, LendingMath.lateFeeCents(1, 25, 2_000));
    }

    @Test
    void lateFeeIsZeroWhenOnTimeOrEarly() {
        assertEquals(0, LendingMath.lateFeeCents(0, 100, 2_000));
        assertEquals(0, LendingMath.lateFeeCents(-4, 100, 2_000));
    }

    @Test
    void lateFeeStopsAtTheCap() {
        assertEquals(2_000, LendingMath.lateFeeCents(21, 100, 2_000));
        assertEquals(2_000, LendingMath.lateFeeCents(20, 100, 2_000), "exactly at the cap");
    }

    @Test
    void lateFeeDoesNotOverflowForHugeValues() {
        long cap = 9_000_000_000_000L;
        assertEquals(cap, LendingMath.lateFeeCents(1_000_000, 1_000_000_000L, cap),
                "a fee above the cap is capped; multiply as long so nothing overflows");
        assertEquals(2_000_000_000L * 3, LendingMath.lateFeeCents(2_000_000_000, 3, Long.MAX_VALUE));
    }

    @Test
    void membershipTierBoundaries() {
        assertEquals("STARTER", LendingMath.membershipTier(0));
        assertEquals("STARTER", LendingMath.membershipTier(4));
        assertEquals("REGULAR", LendingMath.membershipTier(5));
        assertEquals("REGULAR", LendingMath.membershipTier(19));
        assertEquals("CHAMPION", LendingMath.membershipTier(20));
        assertEquals("CHAMPION", LendingMath.membershipTier(250));
        assertEquals("INVALID", LendingMath.membershipTier(-1));
    }

    @Test
    void formatCentsUsesDollarsCentsAndThousands() {
        assertEquals("$0.05", LendingMath.formatCents(5));
        assertEquals("$0.50", LendingMath.formatCents(50));
        assertEquals("$12.00", LendingMath.formatCents(1_200));
        assertEquals("$999.99", LendingMath.formatCents(99_999));
        assertEquals("$1,234.56", LendingMath.formatCents(123_456));
        assertEquals("$1,000,000.00", LendingMath.formatCents(100_000_000));
        assertEquals("$0.00", LendingMath.formatCents(0));
    }

    @Test
    void formatCentsShowsNegativeAmounts() {
        assertEquals("-$2.25", LendingMath.formatCents(-225));
    }

    @Test
    void parseCentsAcceptsStaffInput() {
        assertEquals(1_250, LendingMath.parseCents(" $12.5 "));
        assertEquals(700, LendingMath.parseCents("7"));
        assertEquals(1_207, LendingMath.parseCents("12.07"));
        assertEquals(5, LendingMath.parseCents("0.05"));
        assertEquals(0, LendingMath.parseCents("$0"));
    }

    @Test
    void parseCentsRejectsEverythingElse() {
        for (String bad : Arrays.asList(null, "", "   ", "$", "12.", ".5", "1,200", "12.345", "-3", "abc", "1.2.3",
                "$$4", "4$")) {
            assertEquals(-1, LendingMath.parseCents(bad), "should reject: " + bad);
        }
    }

    @Test
    void checkDigitAddsDigitsUntilOneRemains() {
        assertEquals(0, LendingMath.checkDigit(0));
        assertEquals(7, LendingMath.checkDigit(7));
        assertEquals(2, LendingMath.checkDigit(1001));
        assertEquals(1, LendingMath.checkDigit(1009), "1009 -> 10 -> 1");
        assertEquals(9, LendingMath.checkDigit(9999), "9999 -> 36 -> 9");
        assertEquals(9, LendingMath.checkDigit(987_654_321), "45 -> 9");
    }

    @Test
    void normalizeToolCodeCleansTypedInput() {
        assertEquals("LT-1009-1", LendingMath.normalizeToolCode(" lt-1009- 1 "));
        assertEquals("LT-1001-2", LendingMath.normalizeToolCode("LT-1001-2"));
        assertEquals("LT-1001-2", LendingMath.normalizeToolCode("l t - 1 0 0 1 - 2"));
        assertEquals("", LendingMath.normalizeToolCode(null));
        assertEquals("", LendingMath.normalizeToolCode("   "));
    }

    @Test
    void validToolCodesHaveTheRightShapeAndCheckDigit() {
        assertTrue(LendingMath.isValidToolCode("LT-1009-1"));
        assertTrue(LendingMath.isValidToolCode("LT-1012-4"));
        assertTrue(LendingMath.isValidToolCode("LT-0000-0"));
        assertFalse(LendingMath.isValidToolCode("LT-1009-2"), "wrong check digit");
        assertFalse(LendingMath.isValidToolCode("lt-1009-1"), "lower case");
        assertFalse(LendingMath.isValidToolCode("LT-109-1"), "three digits");
        assertFalse(LendingMath.isValidToolCode("LT-10A9-1"), "letter in the number");
        assertFalse(LendingMath.isValidToolCode("LT-1009-X"), "letter as check digit");
        assertFalse(LendingMath.isValidToolCode("LT-1009/1"), "wrong separator");
        assertFalse(LendingMath.isValidToolCode("XX-1009-1"), "wrong prefix");
        assertFalse(LendingMath.isValidToolCode(null));
        assertFalse(LendingMath.isValidToolCode(""));
    }

    @Test
    void binarySearchFindsEveryPositionAndMisses() {
        int[] numbers = {1001, 1003, 1004, 1009, 1010, 1012, 1050};
        for (int index = 0; index < numbers.length; index++) {
            assertEquals(index, LendingMath.indexOfToolNumber(numbers, numbers[index]));
        }
        assertEquals(-1, LendingMath.indexOfToolNumber(numbers, 1000));
        assertEquals(-1, LendingMath.indexOfToolNumber(numbers, 1005));
        assertEquals(-1, LendingMath.indexOfToolNumber(numbers, 2000));
        assertEquals(-1, LendingMath.indexOfToolNumber(new int[0], 1001));
    }

    @Test
    void binarySearchHandlesALargeSortedArray() {
        int[] numbers = new int[100_001];
        for (int index = 0; index < numbers.length; index++) {
            numbers[index] = index * 2;
        }
        assertEquals(50_000, LendingMath.indexOfToolNumber(numbers, 100_000));
        assertEquals(-1, LendingMath.indexOfToolNumber(numbers, 99_999));
    }

    @Test
    void reverseInPlaceSwapsFromBothEnds() {
        int[] odd = {1, 2, 3, 4, 5};
        LendingMath.reverseInPlace(odd);
        assertArrayEquals(new int[] {5, 4, 3, 2, 1}, odd);
        int[] even = {7, 8, 9, 10};
        LendingMath.reverseInPlace(even);
        assertArrayEquals(new int[] {10, 9, 8, 7}, even);
        int[] empty = {};
        LendingMath.reverseInPlace(empty);
        assertArrayEquals(new int[0], empty);
    }

    @Test
    void overdueCodesKeepsInputOrder() {
        String[] codes = {"LT-1003-4", "LT-1006-7", "LT-1009-1", "LT-1002-3"};
        int[] daysLate = {6, 0, 3, -1};
        assertEquals(List.of("LT-1003-4", "LT-1009-1"), LendingMath.overdueCodes(codes, daysLate));
        assertEquals(List.of(), LendingMath.overdueCodes(new String[0], new int[0]));
    }

    @Test
    void overdueCodesReturnsAListYouCanAddTo() {
        List<String> result = LendingMath.overdueCodes(new String[] {"LT-1003-4"}, new int[] {2});
        assertTrue(result instanceof ArrayList, "return a new ArrayList");
        result.add("LT-1001-2");
        assertEquals(2, result.size());
    }

    @Test
    void countByCategoryNormalizesNames() {
        Map<String, Integer> counts = LendingMath.countByCategory(
                Arrays.asList("hand", " HAND ", "Power", null, "", "garden", "hand"));
        assertEquals(Map.of("HAND", 3, "POWER", 1, "GARDEN", 1), counts);
        assertEquals(Map.of(), LendingMath.countByCategory(List.of()));
    }
}
