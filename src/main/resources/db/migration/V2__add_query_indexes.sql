CREATE INDEX idx_crop_demand_location_crop_date
    ON crop_demand (LOWER(location), crop_id, recorded_date DESC, id DESC);

CREATE INDEX idx_cultivation_plans_farmer_planting_date
    ON cultivation_plans (farmer_id, planting_date DESC);

CREATE INDEX idx_cultivation_plans_farmer_harvested
    ON cultivation_plans (farmer_id)
    WHERE actual_harvest_quantity IS NOT NULL;

CREATE INDEX idx_recommendations_farmer_created_at
    ON recommendations (farmer_id, created_at DESC);
