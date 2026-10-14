package com.hassantechno.traceo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNumberTest {
    @Test
    fun normalizesIranianNationalNumber() {
        assertEquals("+989123456789", normalizeIranPhone("09123456789"))
    }

    @Test
    fun normalizesInternationalPrefixes() {
        assertEquals("+989123456789", normalizeIranPhone("+989123456789"))
        assertEquals("+989123456789", normalizeIranPhone("00989123456789"))
        assertEquals("+989123456789", normalizeIranPhone("989123456789"))
    }

    @Test
    fun normalizesPersianAndArabicIndicDigits() {
        assertEquals("+989123456789", normalizeIranPhone("۰۹۱۲۳۴۵۶۷۸۹"))
        assertEquals("+989123456789", normalizeIranPhone("٠٩١٢٣٤٥٦٧٨٩"))
    }

    @Test
    fun ignoresCommonSeparators() {
        assertEquals("+989123456789", normalizeIranPhone("+98 912-345-6789"))
    }

    @Test
    fun rejectsInvalidNumbers() {
        assertNull(normalizeIranPhone(""))
        assertNull(normalizeIranPhone("02123456789"))
        assertNull(normalizeIranPhone("0912345678"))
        assertNull(normalizeIranPhone("091234567890"))
    }
}
