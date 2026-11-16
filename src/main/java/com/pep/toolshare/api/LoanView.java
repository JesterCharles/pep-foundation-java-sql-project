package com.pep.toolshare.api;

import com.pep.toolshare.domain.Loan;

/** JSON shape of a loan. Dates are ISO text such as {@code 2026-10-15}; {@code returnedOn} is null while open. */
public record LoanView(long id, String memberId, String toolCode, String loanedOn, String dueOn, String returnedOn,
                       long feeCents) {
    public static LoanView from(Loan loan) {
        return new LoanView(loan.id(), loan.memberId(), loan.toolCode(), loan.loanedOn().toString(),
                loan.dueOn().toString(), loan.returnedOn() == null ? null : loan.returnedOn().toString(),
                loan.feeCents());
    }
}
