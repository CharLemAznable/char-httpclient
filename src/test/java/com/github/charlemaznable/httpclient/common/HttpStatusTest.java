package com.github.charlemaznable.httpclient.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpStatusTest {

    @Test
    public void testHttpStatus() {
        assertThrows(IllegalArgumentException.class,
                () -> HttpStatus.valueOf(600));
        assertThrows(IllegalArgumentException.class,
                () -> HttpStatus.Series.valueOf(600));

        assertTrue(HttpStatus.CONTINUE.is1xxInformational());
        assertFalse(HttpStatus.CONTINUE.is2xxSuccessful());
        assertFalse(HttpStatus.CONTINUE.is3xxRedirection());
        assertFalse(HttpStatus.CONTINUE.is4xxClientError());
        assertFalse(HttpStatus.CONTINUE.is5xxServerError());
        assertFalse(HttpStatus.CONTINUE.isError());
        assertEquals(1, HttpStatus.CONTINUE.series().value());

        assertFalse(HttpStatus.OK.is1xxInformational());
        assertTrue(HttpStatus.OK.is2xxSuccessful());
        assertFalse(HttpStatus.OK.is3xxRedirection());
        assertFalse(HttpStatus.OK.is4xxClientError());
        assertFalse(HttpStatus.OK.is5xxServerError());
        assertFalse(HttpStatus.OK.isError());
        assertEquals(2, HttpStatus.OK.series().value());

        assertFalse(HttpStatus.MULTIPLE_CHOICES.is1xxInformational());
        assertFalse(HttpStatus.MULTIPLE_CHOICES.is2xxSuccessful());
        assertTrue(HttpStatus.MULTIPLE_CHOICES.is3xxRedirection());
        assertFalse(HttpStatus.MULTIPLE_CHOICES.is4xxClientError());
        assertFalse(HttpStatus.MULTIPLE_CHOICES.is5xxServerError());
        assertFalse(HttpStatus.MULTIPLE_CHOICES.isError());
        assertEquals(3, HttpStatus.MULTIPLE_CHOICES.series().value());

        assertFalse(HttpStatus.BAD_REQUEST.is1xxInformational());
        assertFalse(HttpStatus.BAD_REQUEST.is2xxSuccessful());
        assertFalse(HttpStatus.BAD_REQUEST.is3xxRedirection());
        assertTrue(HttpStatus.BAD_REQUEST.is4xxClientError());
        assertFalse(HttpStatus.BAD_REQUEST.is5xxServerError());
        assertTrue(HttpStatus.BAD_REQUEST.isError());
        assertEquals(4, HttpStatus.BAD_REQUEST.series().value());

        assertFalse(HttpStatus.INTERNAL_SERVER_ERROR.is1xxInformational());
        assertFalse(HttpStatus.INTERNAL_SERVER_ERROR.is2xxSuccessful());
        assertFalse(HttpStatus.INTERNAL_SERVER_ERROR.is3xxRedirection());
        assertFalse(HttpStatus.INTERNAL_SERVER_ERROR.is4xxClientError());
        assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.is5xxServerError());
        assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.isError());
        assertEquals(5, HttpStatus.INTERNAL_SERVER_ERROR.series().value());
    }
}
