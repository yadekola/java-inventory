package com.tech365.inventory;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import java.util.Map;
import java.net.URI;
@RestController
@RequestMapping("/api")
public class ProductController {
 private final ProductRepository products;
 public ProductController(ProductRepository products){this.products=products;}
 @GetMapping public Map<String,Object> info(){return Map.of("name","Tech365 Java Inventory","version","1.0.0","currency","NGN");}
 @GetMapping("/products") public Map<String,Object> list(@RequestParam(defaultValue="1") int page,@RequestParam(defaultValue="10") int limit,@RequestParam(defaultValue="") String search,@RequestParam(defaultValue="") String category){
  if(page<1||page>1000000||limit<1||limit>100||search.length()>160||category.length()>80)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid pagination or filter.");
  return products.list(page,limit,search.trim(),category.trim());
 }
 private Product getProduct(long id){if(id<1)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"ID must be positive.");return products.find(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found."));}
 @GetMapping("/products/{id}") public Map<String,Product> get(@PathVariable long id){return Map.of("data",getProduct(id));}
 @PostMapping(value="/products",consumes="application/json") public ResponseEntity<Map<String,Product>> create(@Valid @RequestBody ProductInput input){var p=products.create(input);return ResponseEntity.created(URI.create("/api/products/"+p.id())).body(Map.of("data",p));}
 @PutMapping(value="/products/{id}",consumes="application/json") public Map<String,Product> update(@PathVariable long id,@Valid @RequestBody ProductInput input){getProduct(id);if(!products.update(id,input))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found.");return Map.of("data",getProduct(id));}
 @DeleteMapping("/products/{id}") public ResponseEntity<Void> delete(@PathVariable long id){getProduct(id);if(!products.delete(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found.");return ResponseEntity.noContent().build();}
}
