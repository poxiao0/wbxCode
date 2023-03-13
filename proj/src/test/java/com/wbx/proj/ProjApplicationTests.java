package com.wbx.proj;

import com.wbx.proj.dao.DiscussPostMapper;
import com.wbx.proj.dao.LoginTicketMapper;
import com.wbx.proj.dao.MessageMapper;
import com.wbx.proj.dao.UserMapper;
import com.wbx.proj.entity.DiscussPost;
import com.wbx.proj.entity.LoginTicket;
import com.wbx.proj.entity.Message;
import com.wbx.proj.entity.User;
import com.wbx.proj.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ProjApplication.class)
public class ProjApplicationTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void selectUserTest() {
        User user1 = userMapper.selectById(111);
        System.out.println(user1);

        User user2 = userMapper.selectByName("aaa");
        System.out.println(user2);

        User user3 = userMapper.selectByEmail("nowcoder111@sina.com");
        System.out.println(user3);
    }

    @Test
    public void insertUserTest() {
        User user = new User();
        user.setId(150);
        user.setUsername("wbx");
        user.setPassword("123456");
        user.setCreateTime(new Date());
        int row = userMapper.insertUser(user);
        User user1 = userMapper.selectById(150);
        System.out.println(row+"---"+user1);
    }

    @Test
    public void updateUserTest() {
        System.out.println(userMapper.selectById(150).getStatus());
        userMapper.updateStatus(150,1);
        System.out.println(userMapper.selectById(150).getStatus());

        System.out.println(userMapper.selectById(150).getHeaderUrl());
        userMapper.updateHeader(150,"www.nowcoder.com/123");
        System.out.println(userMapper.selectById(150).getHeaderUrl());

        System.out.println(userMapper.selectById(150).getPassword());
        userMapper.updatePassword(150,"11111");
        System.out.println(userMapper.selectById(150).getPassword());
    }

    @Test
    public void selectDiscussPostTest(){
        List<DiscussPost> posts = discussPostMapper.selectDiscussPosts(111, 2, 10);
        for(DiscussPost post : posts) {
            System.out.println(post);
        }
        System.out.println(discussPostMapper.selectDiscussPostsRows(0));
        System.out.println(discussPostMapper.selectDiscussPostsRows(111));
    }

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void insertLoginTicketTest() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("hhh");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 60 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void selectByTicketTest() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("hhh");
        System.out.println(loginTicket);
    }

    @Test
    public void updateTicketTest() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("hhh");
        System.out.println(loginTicket.getStatus());
        loginTicketMapper.updateStatus("hhh", 0);
        loginTicket = loginTicketMapper.selectByTicket("hhh");
        System.out.println(loginTicket.getStatus());
    }
    //sensitive-words.txt
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String s = "禁止*赌博*，禁止--嫖娼，禁止*喝酒*，禁止cc，禁止吸毒!";
        System.out.println("过滤前：" + s);
        System.out.println("过滤后：" + sensitiveFilter.filter(s));
    }

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testMessageMapper() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }

        int messCnt = messageMapper.selectConversationCount(111);
        System.out.println(messCnt);

        List<Message> letters = messageMapper.selectLetters("111_112", 0, 20);
        for (Message message : messages) {
            System.out.println(message);
        }

        int lettCnt = messageMapper.selectLetterCount("111_112");
        System.out.println(lettCnt);

        int cnt = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(cnt);
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedisConfig() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "name", "wbx");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "name"));
    }

    @Test
    public void testList() {
        String redisKey = "test:list";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSets() {
        String redisKey = "test:sets";
        
        redisTemplate.opsForSet().add(redisKey, "雷电将军","八重神子","提纳里");
        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testSortedSets() {
        String redisKey = "test:sortedset";

        redisTemplate.opsForZSet().add(redisKey, "雷电将军", 1);
        redisTemplate.opsForZSet().add(redisKey, "八重神子", 2);
        redisTemplate.opsForZSet().add(redisKey, "提纳里", 3);
        redisTemplate.opsForZSet().add(redisKey, "魈", 8);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "魈"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 1,2));
    }

    @Test
    public void testKey() {
        //redisTemplate.delete("test:list");

        System.out.println(redisTemplate.hasKey("test:list"));

        redisTemplate.expire("test:sets",10, TimeUnit.SECONDS);
        System.out.println(redisTemplate.hasKey("test:sets"));
    }

    @Test
    public void testBoundOperators() {
        String redisKey = "test:sortedset";
        BoundZSetOperations operations = redisTemplate.boundZSetOps(redisKey);
        operations.add("旅行者", 100);
        System.out.println(operations.zCard());
    }

    @Test
    public void testTransaction() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";

                operations.multi();

                operations.opsForSet().add("test:tx","蒙德");
                operations.opsForSet().add("test:tx","璃月");
                operations.opsForSet().add("test:tx","稻妻");
                operations.opsForSet().add("test:tx","须弥");

                System.out.println(operations.opsForSet().members(redisKey));

                return operations.exec();
            }
        });
        System.out.println(obj);
    }


}
