// Relative URLs keep the browser on the frontend origin. Nginx/Vite proxies /api.
export async function request(path,options={}){
  const response=await fetch('/api'+path,{...options,headers:{'Content-Type':'application/json',...options.headers}});
  if(response.status===204)return null;
  const body=await response.json().catch(()=>({error:{message:'Unexpected response. Check the API server.'}}));
  if(!response.ok){
    const details=body.error?.details;
    throw new Error((body.error?.message||'Request failed.')+(details?' '+Object.entries(details).map(([k,v])=>`${k}: ${v}`).join(' '):''));
  }
  return body;
}
