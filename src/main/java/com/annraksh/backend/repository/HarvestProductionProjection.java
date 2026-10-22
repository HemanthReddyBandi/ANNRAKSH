package com.annraksh.backend.repository;

import java.math.BigDecimal;

public interface HarvestProductionProjection {

    Long getCropId();

    String getCropName();

    String getQuantityUnit();

    Long getHarvestCount();

    BigDecimal getTotalPlannedQuantity();

    BigDecimal getTotalHarvestedQuantity();

    BigDecimal getTotalSoldQuantity();

    BigDecimal getTotalStoredQuantity();

    BigDecimal getTotalDiscardedQuantity();

    BigDecimal getTotalAreaHectares();
}
