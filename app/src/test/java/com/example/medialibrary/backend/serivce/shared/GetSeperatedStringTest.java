package com.example.medialibrary.backend.serivce.shared;

import static org.junit.Assert.*;

import serivce.SharedService;
import repository.database.MediaLibraryDbHelper;

import org.junit.Test;

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
