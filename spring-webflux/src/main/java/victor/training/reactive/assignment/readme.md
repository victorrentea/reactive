###Customer Audit
You are given a flux of customerIds that placed an order just now. 
1. For each, load the Customer entity from the CustomerReactiveRepo.
2. If the Customer#isActive is false send an email using the injected EmailSender
3. If the Customer#isActive is true, to GET localhost:9999/customer-audit/{customerId} using a non-blocking WebClient.
   Use this URL: baseUrl + "/customer-audit/{id}"
4. [HARD] Right after fetching the customers from the repo, some customer might have Customer#isExternal true.
   Group those customers in pages of size=2 and retrieve their data as JSON from GET /external-customer/{ids}, separating the customer ids with a comma
   Example:  baseUrl + "/external customer/1,4", Example: if customer ids = 1 and 4
   Then, resume the flow as before.
   
send the requests for  the "external" flag If the Customer#isActive is true, send them in pages of 2 items to POST localhost:9999/customer-audit as a JSON list, using non-blocking WebClient.
   Example body (for customer id 1 and 2): [1, 2]

Note: Do not change the signature of the public method.



### Selective

