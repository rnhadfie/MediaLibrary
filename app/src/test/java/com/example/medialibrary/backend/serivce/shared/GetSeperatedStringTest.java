package com.example.medialibrary.backend.serivce.shared;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import repository.database.MediaLibraryDbHelper;
import serivce.SharedService;

public class GetSeperatedStringTest {
    @Test
    public void testGetSeperatedString_CamelCase() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        String input = "MyCoolMediaItem";
        String expected = "My Cool Media Item";
        String result = service.GetSeperatedString(input);
        assertEquals(expected, result);
    }

    @Test
    public void testGetSeperatedString_AlreadySeperated() {
        SharedService service = new SharedService((MediaLibraryDbHelper) null);
        String input = "Already Seperated";
        String expected = "Already Seperated";
        String result = service.GetSeperatedString(input);
        assertEquals(expected, result);
    }
}
