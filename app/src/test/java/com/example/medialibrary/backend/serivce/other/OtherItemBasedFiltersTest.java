package com.example.medialibrary.backend.serivce.other;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import models.other.Other;
import models.other.OtherItem;
import models.shared.Filter;
import serivce.OtherService;

public class OtherItemBasedFiltersTest {
    @Test
    public void testOtherItemBasedFilters_AnyOwnedTrue() {
        OtherService service = new OtherService(null);
        List<Other> list = new ArrayList<>();
        
        Other o1 = new Other();
        o1.Items = new ArrayList<>();
        OtherItem i1 = new OtherItem(); i1.Owned = true;
        o1.Items.add(i1);
        
        list.add(o1);
        
        Filter filter = new Filter();
        filter.AnyOwned = true;
        
        List<Other> result = service.OtherItemBasedFilters(list, filter);
        assertEquals(1, result.size());
        assertTrue(result.get(0).CurrentOwnAny);
    }
}
