package com.tech365.inventory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
class ProductApiTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Test void crud() throws Exception {
  String body="{\"sku\":\"TEST-1\",\"name\":\"Keyboard\",\"category\":\"Accessories\",\"price_minor\":150000,\"quantity\":4}";
  var response=mvc.perform(post("/api/products").contentType("application/json").content(body)).andExpect(status().isCreated()).andReturn();
  long id=json.readTree(response.getResponse().getContentAsString()).path("data").path("id").asLong();
  mvc.perform(get("/api/products/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.data.sku").value("TEST-1"));
  mvc.perform(put("/api/products/"+id).contentType("application/json").content(body.replace("150000","200000"))).andExpect(status().isOk()).andExpect(jsonPath("$.data.price_minor").value(200000));
  mvc.perform(get("/api/products?search=Keyboard&category=Accessories")).andExpect(status().isOk()).andExpect(jsonPath("$.pagination.total").value(1));
  mvc.perform(post("/api/products").contentType("application/json").content(body)).andExpect(status().isConflict());
  mvc.perform(delete("/api/products/"+id)).andExpect(status().isNoContent());
  mvc.perform(get("/api/products/"+id)).andExpect(status().isNotFound());
 }
 @Test void validation() throws Exception {
  mvc.perform(post("/api/products").contentType("application/json").content("{}")).andExpect(status().isBadRequest());
  mvc.perform(get("/api/products?page=0")).andExpect(status().isBadRequest());
  mvc.perform(get("/api/products/abc")).andExpect(status().isBadRequest());
 }
}
