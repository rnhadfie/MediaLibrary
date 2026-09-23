package com.example.medialibrary.backend.Serivce.other;

import static org.junit.Assert.*;

import com.example.medialibrary.backend.Serivce.OtherService;
import com.example.medialibrary.backend.models.other.Other;
import com.example.medialibrary.backend.models.other.OtherItem;
import com.example.medialibrary.backend.models.shared.Filter;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

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
