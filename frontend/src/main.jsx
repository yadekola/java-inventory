import React,{useEffect,useState} from 'react';
import {createRoot} from 'react-dom/client';
import {request} from './api.js';
import './styles.css';
const empty={name:'',sku:'',category:'',price:'',quantity:'0'};
const money=new Intl.NumberFormat('en-NG',{style:'currency',currency:'NGN'});
function App(){
 const [form,setForm]=useState({...empty}),[editing,setEditing]=useState(null);
 const [products,setProducts]=useState([]),[total,setTotal]=useState(0),[page,setPage]=useState(1);
 const [search,setSearch]=useState(''),[category,setCategory]=useState(''),[filters,setFilters]=useState({search:'',category:''});
 const [revision,setRevision]=useState(0),[loading,setLoading]=useState(true),[busy,setBusy]=useState(false),[notice,setNotice]=useState(null);
 const pages=Math.max(1,Math.ceil(total/10));
 useEffect(()=>{const timer=setTimeout(()=>{setPage(1);setFilters({search,category});},250);return()=>clearTimeout(timer);},[search,category]);
 useEffect(()=>{
  const controller=new AbortController();setLoading(true);
  request('/products?'+new URLSearchParams({...filters,page,limit:10}),{signal:controller.signal})
   .then(result=>{if(controller.signal.aborted)return;setTotal(result.pagination.total);const last=Math.max(1,result.pagination.total_pages);if(page>last){setPage(last);return;}setProducts(result.data);})
   .catch(error=>{if(error.name!=='AbortError')setNotice({error:true,text:error.message});})
   .finally(()=>{if(!controller.signal.aborted)setLoading(false);});
  return()=>controller.abort();
 },[filters,page,revision]);
 function change(e){setForm({...form,[e.target.name]:e.target.value});}
 function reset(){setEditing(null);setForm({...empty});}
 function edit(product){setEditing(product.id);setForm({name:product.name,sku:product.sku,category:product.category,price:(product.price_minor/100).toFixed(2),quantity:String(product.quantity)});}
 async function save(e){
  e.preventDefault();if(busy)return;
  const match=/^(\d+)(?:\.(\d{1,2}))?$/.exec(form.price);
  if(!match){setNotice({error:true,text:'Use a price with at most two decimal places.'});return;}
  const data={name:form.name,sku:form.sku,category:form.category,price_minor:Number(match[1])*100+Number((match[2]||'').padEnd(2,'0')),quantity:Number(form.quantity)};
  setBusy(true);setNotice(null);
  try{await request(editing?`/products/${editing}`:'/products',{method:editing?'PUT':'POST',body:JSON.stringify(data)});setNotice({text:editing?'Product updated.':'Product created.'});reset();setPage(1);setRevision(x=>x+1);}
  catch(error){setNotice({error:true,text:error.message});}finally{setBusy(false);}
 }
 async function remove(product){
  if(busy||!window.confirm(`Delete ${product.name} (${product.sku})?`))return;
  setBusy(true);setNotice(null);
  try{await request(`/products/${product.id}`,{method:'DELETE'});if(editing===product.id)reset();setNotice({text:'Product deleted.'});setRevision(x=>x+1);}
  catch(error){setNotice({error:true,text:error.message});}finally{setBusy(false);}
 }
 return <><header><div className="brand"><span className="mark">T</span><span>TECH365 <b>INVENTORY</b></span></div><span className="pill">React + Spring Boot + PostgreSQL</span></header><main>
  <section className="intro"><p className="eyebrow">PRODUCT OPERATIONS</p><h1>Your inventory, in one place.</h1><p>Keep product details, prices and stock levels up to date.</p></section>
  {notice&&<div id="message" role={notice.error?'alert':'status'} className={notice.error?'error':'success'}>{notice.text}</div>}
  <div className="layout"><section className="panel editor"><h2>{editing?'Edit product':'Add a product'}</h2><p className="muted">Prices are entered in Nigerian naira.</p>
  <form onSubmit={save}><fieldset disabled={busy}>
   <label>Product name<input name="name" value={form.name} onChange={change} required maxLength={160} placeholder="Wireless keyboard"/></label>
   <label>SKU<input name="sku" value={form.sku} onChange={change} required maxLength={40} pattern="[A-Za-z0-9][A-Za-z0-9_-]*" placeholder="KB-001"/><small>A unique product code.</small></label>
   <label>Category<input name="category" value={form.category} onChange={change} required maxLength={80} placeholder="Accessories"/></label>
   <div className="two"><label>Price (NGN)<input name="price" type="number" value={form.price} onChange={change} required min="0" max="10000000" step="0.01"/></label><label>Quantity<input name="quantity" type="number" value={form.quantity} onChange={change} required min="0" max="1000000000" step="1"/></label></div>
   <div className="actions"><button className="primary" type="submit">{busy?'Saving...':editing?'Save changes':'Add product'}</button>{editing&&<button type="button" onClick={reset}>Cancel edit</button>}</div>
  </fieldset></form></section>
  <section className="panel listing"><div className="list-heading"><h2>Product catalogue</h2><span className="count">{total} products</span></div>
   <div className="filters"><label>Search<input type="search" value={search} onChange={e=>setSearch(e.target.value)} placeholder="Product name or SKU"/></label><label>Category<input value={category} onChange={e=>setCategory(e.target.value)} placeholder="Exact category"/></label><button onClick={()=>{setNotice(null);setRevision(x=>x+1);}}>Refresh</button></div>
   {loading?<p role="status">Loading products...</p>:products.length===0?<p>No matching products. Add a product to begin.</p>:<div className="table-wrap"><table><thead><tr><th>Product</th><th>Category</th><th>Price</th><th>Stock</th><th>Actions</th></tr></thead><tbody>{products.map(product=><tr key={product.id}><td><strong>{product.name}</strong><small>{product.sku}</small></td><td>{product.category}</td><td>{money.format(product.price_minor/100)}</td><td className={product.quantity===0?'out':'stock'}>{product.quantity}</td><td className="row-actions"><button disabled={busy} onClick={()=>edit(product)}>Edit</button><button disabled={busy} onClick={()=>remove(product)}>Delete</button></td></tr>)}</tbody></table></div>}
   <nav aria-label="Product pages"><button disabled={page<=1||loading||busy} onClick={()=>setPage(p=>p-1)}>Previous</button><span>Page {page} of {pages}</span><button disabled={page>=pages||loading||busy} onClick={()=>setPage(p=>p+1)}>Next</button></nav>
  </section></div><footer>Tech365 Inventory · Prices in NGN · <a href="/api" target="_blank" rel="noopener">API information</a></footer>
 </main></>;
}
createRoot(document.getElementById('root')).render(<App/>);
