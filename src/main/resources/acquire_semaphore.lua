--print the argv
--uuid
redis.log(redis.LOG_NOTICE,"uuid:".. ARGV[1])
--now
redis.log(redis.LOG_NOTICE,"now:" .. ARGV[4])

local semaphore = KEYS[1]
local uuid = ARGV[1]
local timeout = ARGV[2]
local limit = ARGV[3]
local now = ARGV[4]
--local timestamp = redis.call("time");
--local now = timestamp[1]*1000+timestamp[2]/1000
----now
--redis.log(redis.LOG_NOTICE,"now:".. now)
redis.call("zremrangebyscore",semaphore,0,now-timeout)
redis.call("zadd",semaphore,now,uuid)
local rank = redis.call("zrank",semaphore,uuid)
redis.log(redis.LOG_NOTICE,"rank:" ..rank)
--all the args are considered as string type by redis
if (rank<tonumber(limit)) then
    redis.log(redis.LOG_NOTICE,"success!")
    return rank
else
    redis.log(redis.LOG_NOTICE,"fair!")
    redis.call("zrem",semaphore,uuid)
    return -1
end


