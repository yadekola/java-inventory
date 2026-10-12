package com.tech365.inventory;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
public record Product(long id,String sku,String name,String category,
 @JsonProperty("price_minor") int priceMinor,int quantity,
 @JsonProperty("created_at") OffsetDateTime createdAt,
 @JsonProperty("updated_at") OffsetDateTime updatedAt) {}
