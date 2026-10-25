package com.accommodation.dto;

public class FeeStatsDTO {

    private Double totalFeesDue;
    private Double totalAmountCollected;
    private Double totalOutstandingBalance;
    private Long overdueStudentsCount;
    private Long partiallyPaidStudentsCount;
    private Long upcomingPaymentsCount;
    private Double collectionRate;

    public FeeStatsDTO() {
    }

    public FeeStatsDTO(Double totalFeesDue, Double totalAmountCollected, Double totalOutstandingBalance,
                       Long overdueStudentsCount, Long partiallyPaidStudentsCount, Long upcomingPaymentsCount) {
        this.totalFeesDue = totalFeesDue != null ? Math.round(totalFeesDue * 100.0) / 100.0 : 0.0;
        this.totalAmountCollected = totalAmountCollected != null ? Math.round(totalAmountCollected * 100.0) / 100.0 : 0.0;
        this.totalOutstandingBalance = totalOutstandingBalance != null ? Math.round(totalOutstandingBalance * 100.0) / 100.0 : 0.0;
        this.overdueStudentsCount = overdueStudentsCount != null ? overdueStudentsCount : 0L;
        this.partiallyPaidStudentsCount = partiallyPaidStudentsCount != null ? partiallyPaidStudentsCount : 0L;
        this.upcomingPaymentsCount = upcomingPaymentsCount != null ? upcomingPaymentsCount : 0L;
        if (this.totalFeesDue > 0) {
            this.collectionRate = Math.round((this.totalAmountCollected / this.totalFeesDue) * 1000.0) / 10.0;
        } else {
            this.collectionRate = 0.0;
        }
    }

    public Double getTotalFeesDue() {
        return totalFeesDue;
    }

    public Double getTotalAmountCollected() {
        return totalAmountCollected;
    }

    public Double getTotalOutstandingBalance() {
        return totalOutstandingBalance;
    }

    public Long getOverdueStudentsCount() {
        return overdueStudentsCount;
    }

    public Long getPartiallyPaidStudentsCount() {
        return partiallyPaidStudentsCount;
    }

    public Long getUpcomingPaymentsCount() {
        return upcomingPaymentsCount;
    }

    public Double getCollectionRate() {
        return collectionRate;
    }
}
