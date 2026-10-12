param([string]$Base = 'http://localhost:8082')
$ErrorActionPreference = 'Stop'
$sku = 'SMOKE-' + [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$body = @{sku=$sku; name='Smoke product'; category='Checks'; price_minor=150000; quantity=2}
$product = Invoke-RestMethod "$Base/api/products" -Method Post -ContentType 'application/json' -Body ($body | ConvertTo-Json)
$id = $product.data.id
try {
    $read = Invoke-RestMethod "$Base/api/products/$id"
    if ($read.data.sku -ne $sku) { throw 'Read failed' }
    $body.quantity = 5
    $updated = Invoke-RestMethod "$Base/api/products/$id" -Method Put -ContentType 'application/json' -Body ($body | ConvertTo-Json)
    if ($updated.data.quantity -ne 5) { throw 'Update failed' }
    $list = Invoke-RestMethod "$Base/api/products?search=$sku"
    if ($list.pagination.total -ne 1) { throw 'Search failed' }
} finally {
    Invoke-RestMethod "$Base/api/products/$id" -Method Delete
}
Write-Output 'PASS: create, read, update, search and delete through frontend proxy.'
