package com.quantumn.tiger;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author: huajun.wu
 * @create: 2019-12-04
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class RedisRunnerSample {
    @Autowired
    private RedisTemplate redisTemplate;
    private String luaScript1 = "redis.call('set', KEYS[1] ,ARGV[1]) return ARGV[1]";
    private String luaScript2 = "redis.log(redis.LOG_NOTICE,ARGV[1]) redis.call('incrby',KEYS[1],ARGV[1]) return redis.call('get',KEYS[1])";
    @Test
    public void runSimpleLua1() {
        DefaultRedisScript redisScript = new DefaultRedisScript();
        redisScript.setScriptText(luaScript1);
        redisScript.setResultType(String.class);
        String ans1 = (String) redisTemplate.execute(redisScript, Collections.singletonList("testLua1"), new String[]{"tiger"});
        log.info("ans1:{}",ans1);
        redisScript.setScriptText(luaScript2);
        redisScript.setResultType(Integer.class);
        Integer ans2 = (Integer) redisTemplate.execute(redisScript, Collections.singletonList("testLua2"), 1);
        log.info("ans2:{}",ans2);
    }

    @Test
    public void runSimpleLua2() {
        RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
        Integer ans = (Integer) redisTemplate.execute(new RedisCallback<Integer>() {
            @Override
            public Integer doInRedis(RedisConnection connection) throws DataAccessException {
                //redis默认返回字节数组，需要反序列化
                byte[] result = connection.eval(luaScript2.getBytes(), ReturnType.INTEGER, 1, "testLua2".getBytes(), valueSerializer.serialize(2));
                return result == null ? null : (Integer) valueSerializer.deserialize(result);
            }
        });
        log.info("ans:{}",ans);
    }

    @Test
    public void runPipeline() {
        //pipeline return a list which contains each result of the command in pipeline
        List<Object> result = redisTemplate.executePipelined(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                BoundListOperations listOperations = operations.boundListOps("testList1");
                listOperations.leftPush("003");
                listOperations.leftPush( "004");
                listOperations.range(0, -1);
                return null;
            }
        });
        for (Object ans : result) {
            log.info("ans:{}",ans);
        }
    }

    @Test
    public void runTransaction() {
        List<Object> result = (List<Object>) redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                List<Object> result = null;
                do {
                    operations.watch("testList1");
                    operations.multi();
                    operations.opsForValue().increment("testTrans2");
                    operations.opsForValue().increment("testTrans3",2);
                    operations.opsForList().range("testTrans1", 0, -1);
                    result = operations.exec();
                } while (result == null);//watch is optimistic lock in redis
                return result;
            }
        });
        for (Object ans : result) {
            log.info("ans:{}",ans);
        }
    }

    @Test
    public void runZSet() {
        List result = redisTemplate.executePipelined(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                BoundZSetOperations zSet = operations.boundZSetOps("testZset2");
                long startTime = DateTools.getNowTimeStampMilliSeconds();
                zSet.add("d", DateTools.getNowTimeStampMilliSeconds());
                zSet.add("e", DateTools.getNowTimeStampMilliSeconds());
                zSet.add("f", DateTools.getNowTimeStampMilliSeconds());
                zSet.rangeByScoreWithScores(startTime,DateTools.getNowTimeStampMilliSeconds());
                return null;
            }
        });
        Set ansSet = (Set) result.get(3);
        for (Object ans : ansSet) {
            DefaultTypedTuple tuple = (DefaultTypedTuple) ans;
            log.info("value={},sorce={}",tuple.getValue(),tuple.getScore().longValue());
        }
    }

}
