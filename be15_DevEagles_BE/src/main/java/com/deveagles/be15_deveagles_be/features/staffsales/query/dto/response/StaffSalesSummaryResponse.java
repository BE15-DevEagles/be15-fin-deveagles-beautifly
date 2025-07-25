package com.deveagles.be15_deveagles_be.features.staffsales.query.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffSalesSummaryResponse {

  private int totalGrossSales;
  private int totalNetSales;
  private int totalDiscount;
  private int totalPrepaid;
  private int totalCoupon;
  private int totalDeduction;
  private int totalIncentiveAmount;
}
