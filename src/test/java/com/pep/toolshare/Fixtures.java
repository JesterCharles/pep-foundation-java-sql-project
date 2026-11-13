package com.pep.toolshare;

import com.pep.toolshare.config.SampleData;
import com.pep.toolshare.config.Stores;
import com.pep.toolshare.lending.LendingService;

import java.time.LocalDate;

/** Shared test values. The sample data is dated around this fixed "today". */
public final class Fixtures {
    public static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    private Fixtures() {
    }

    public static LendingService memoryService() {
        Stores stores = SampleData.inMemory();
        return new LendingService(stores.members(), stores.tools(), stores.loans(), () -> TODAY);
    }

    public static LendingService service(Stores stores) {
        return new LendingService(stores.members(), stores.tools(), stores.loans(), () -> TODAY);
    }
}
