redis.log(redis.LOG_NOTICE,KEYS[1])
--cjson.decode here if necessary
redis.log(redis.LOG_NOTICE,ARGV[1])
if redis.call('get', KEYS[1]) == ARGV[1]
then
redis.log(redis.LOG_NOTICE,"equal then delete")
return redis.call('del', KEYS[1])
else
redis.log(redis.LOG_NOTICE,"not equal")
return 0
end
redis.log(redis.LOG_NOTICE,"finish")

