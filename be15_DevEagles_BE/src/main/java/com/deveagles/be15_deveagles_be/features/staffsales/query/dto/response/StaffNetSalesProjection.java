package com.deveagles.be15_deveagles_be.features.staffsales.query.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StaffNetSalesProjection {
  private String payments;
  private Integer amount;
}
