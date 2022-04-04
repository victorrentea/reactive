

## Non Blocking
- Start with a List<productId>
- Call in a loop externalApi.getProductData(productId):Product using RestTemplate; setup wiremock with delay
- Get back a Flux<Product> (start with Flux.fromIterable(ids))
- Fetch the data from a function returning Mono.just(result)
- Run the blocking REST call in the appropriate Scheduler (which one?)
- Check the threads status in visualvm
- Move to nonBlocking REST calls with WebClient

## Optimize network calls
- Play: what if you have 10k products to fetch ?
- Call their API with max 10 requests in parallel <<<
- Avoid calling network in a loop by fetching "pages of Products" 
  ExternalApi.getProductData(List<productId>): List<Product>
  
## Audit
- An audit API REST call should happen passing the id of the product.
- The Audit API call should only happen for products with resealed=true

## Enhance Data
- Complement the Product with rating fetched from a REST API call to RatingService 
- Before doing the previous call, check an external cache (ExternalCacheClient#lookupInCache). 
  If the returned Mono is empty, perform the call, otherwise use the cached rating.
  
- After the call to RatingService completes, put the rating back in cache (ExternalCacheClient#putInCache)

## Parallelism
- Run rating fetching (with optional caching)  in parallel with auditing the resealed items.

## Resilience
- Add a max timeout of 1 second for the rating service
- Allow 1 extra retry for the auditing service