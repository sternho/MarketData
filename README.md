# Readme

## Information



## Functions

*Functional*
* call publishAggregatedMarketData method with has data come to the application.
And ensure 100 times per second.
* Each symbol would run once only within 1 second.

*Technical Design*
* Use Producer and Consumer design pattern for write the hold application
* Their 3 different limitation for limit 100 process per second
  * SlidingWindow - create an array (size=100) and make down the time. 
  the array run as a loop to check last running time. e.g.: 0, 100, 200 times.
  * TokenBucket - use a count and upper limit as 100. reduce the count before run the process.
  The count will add 1 every 1/100 second.
  * GuavaRateLimiter - Used google guava library to implement the logic same as tokenBucket.
* Used com.lmax.RingBuffer library ack as queue. And the queue will store the symbol only.
All the data is stored in another Hashmap. It can make sure the price is latest.
* Create a HashMap for record down the latest ran record's update time to prevent post reduce record.
