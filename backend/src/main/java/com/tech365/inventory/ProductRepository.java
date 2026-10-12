package com.tech365.inventory;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Statement;
import java.time.OffsetDateTime;
@Repository
public class ProductRepository {
 private final JdbcTemplate db;
 public ProductRepository(JdbcTemplate db){this.db=db;}
 private final RowMapper<Product> mapper=(rs,n)->new Product(rs.getLong("id"),rs.getString("sku"),rs.getString("name"),rs.getString("category"),rs.getInt("price_minor"),rs.getInt("quantity"),rs.getObject("created_at",OffsetDateTime.class),rs.getObject("updated_at",OffsetDateTime.class));
 public Optional<Product> find(long id){return db.query("SELECT * FROM products WHERE id=?",mapper,id).stream().findFirst();}
 public Map<String,Object> list(int page,int limit,String search,String category){
  var args=new ArrayList<Object>();var sql=new StringBuilder(" FROM products WHERE 1=1");
  if(!search.isBlank()){sql.append(" AND (LOWER(name) LIKE ? OR LOWER(sku) LIKE ?)");args.add("%"+search.toLowerCase(Locale.ROOT)+"%");args.add("%"+search.toLowerCase(Locale.ROOT)+"%");}
  if(!category.isBlank()){sql.append(" AND category=?");args.add(category);}
  long total=db.queryForObject("SELECT COUNT(*)"+sql,Long.class,args.toArray());
  args.add(limit);args.add((page-1)*limit);
  var data=db.query("SELECT *"+sql+" ORDER BY id DESC LIMIT ? OFFSET ?",mapper,args.toArray());
  return Map.of("data",data,"pagination",Map.of("page",page,"limit",limit,"total",total,"total_pages",(total+limit-1)/limit));
 }
 public Product create(ProductInput p){
  var key=new GeneratedKeyHolder();
  db.update(connection->{var s=connection.prepareStatement("INSERT INTO products(sku,name,category,price_minor,quantity) VALUES (?,?,?,?,?)",new String[]{"id"});s.setString(1,p.sku().toUpperCase(Locale.ROOT));s.setString(2,p.name().trim());s.setString(3,p.category().trim());s.setInt(4,p.priceMinor());s.setInt(5,p.quantity());return s;},key);
  return find(Objects.requireNonNull(key.getKey()).longValue()).orElseThrow();
 }
 public boolean update(long id,ProductInput p){return db.update("UPDATE products SET sku=?,name=?,category=?,price_minor=?,quantity=?,updated_at=CURRENT_TIMESTAMP WHERE id=?",p.sku().toUpperCase(Locale.ROOT),p.name().trim(),p.category().trim(),p.priceMinor(),p.quantity(),id)>0;}
 public boolean delete(long id){return db.update("DELETE FROM products WHERE id=?",id)>0;}
}
