package com.alandevise.GeneralServer.controller;

import com.alandevise.GeneralServer.dao.StudentMapper;
import com.alandevise.GeneralServer.entity.PgUser;
import com.alandevise.GeneralServer.entity.QuartzBean;
import com.alandevise.GeneralServer.entity.Student;
import com.alandevise.GeneralServer.entity.User;
import com.alandevise.GeneralServer.service.UserService;
import com.alandevise.GeneralServer.util.IGlobalCache;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MySQLTestTest {

    @Mock
    private StudentMapper mockStudentMapper;
    @Mock
    private UserService mockUserService;
    @Mock
    private SqlSessionFactory mockSqlSessionFactory;
    @Mock
    private Scheduler mockScheduler;
    @Mock
    private IGlobalCache mockIGlobalCache;
    @Mock
    private StringRedisTemplate mockStringRedisTemplate;

    @InjectMocks
    private MySQLTest mySQLTestUnderTest;

    @BeforeEach
    void setUp() {
        mySQLTestUnderTest.studentMapper = mockStudentMapper;
        mySQLTestUnderTest.userService = mockUserService;
        mySQLTestUnderTest.sqlSessionFactory = mockSqlSessionFactory;
    }

    @Test
    void testFirstTest() {
        // Setup
        // Run the test
        final String result = mySQLTestUnderTest.FirstTest();

        // Verify the results
        assertEquals("运行到了Controller层", result);
    }

    @Test
    void testAopDemo() {
        // Setup
        // Run the test
        final String result = mySQLTestUnderTest.AopDemo("param");

        // Verify the results
        assertEquals("result", result);
    }

    @Test
    void testAopDemo2() {
        // Setup
        // Run the test
        final String result = mySQLTestUnderTest.AopDemo2("name", "age");

        // Verify the results
        assertEquals("result", result);
    }

    @Test
    void testForSingle() {
        // Setup
        // Run the test
        mySQLTestUnderTest.forSingle();

        // Verify the results
        verify(mockStudentMapper).insert(new Student("name", 0, "addr", "addrNum"));
    }

    @Test
    void testSql() {
        // Setup
        // Run the test
        mySQLTestUnderTest.sql();

        // Verify the results
        verify(mockStudentMapper).insertSplice(Arrays.asList(new Student("name", 0, "addr", "addrNum")));
    }

    @Test
    void testForSaveBatch() throws Exception {
        // Setup
        // Configure SqlSessionFactory.openSession(...).
        final SqlSession mockSqlSession = mock(SqlSession.class);
        when(mockSqlSessionFactory.openSession(ExecutorType.BATCH)).thenReturn(mockSqlSession);

        // Run the test
        mySQLTestUnderTest.forSaveBatch();

        // Verify the results
        verify(mockSqlSession).close();
    }

    @Test
    void testForStreamSearch() throws Exception {
        // Setup
        // Configure SqlSessionFactory.openSession(...).
        final SqlSession mockSqlSession = mock(SqlSession.class);
        when(mockSqlSessionFactory.openSession()).thenReturn(mockSqlSession);

        // Run the test
        mySQLTestUnderTest.forStreamSearch();

        // Verify the results
        verify(mockSqlSession).close();
    }

    @Test
    void testForMapperSearch() {
        // Setup
        // Configure SqlSessionFactory.openSession(...).
        final SqlSession mockSqlSession = mock(SqlSession.class);
        when(mockSqlSessionFactory.openSession()).thenReturn(mockSqlSession);

        // Run the test
        mySQLTestUnderTest.forMapperSearch();

        // Verify the results
        verify(mockSqlSession).close();
    }

    @Test
    void testCreate() {
        // Setup
        final User user = User.builder().build();
        when(mockUserService.create(User.builder().build())).thenReturn(false);

        // Run the test
        final String result = mySQLTestUnderTest.create(user);

        // Verify the results
        assertEquals("创建失败", result);
    }

    @Test
    void testBatchCreate() throws Exception {
        // Setup
        final User user = User.builder().build();
        when(mockUserService.batchCreate(User.builder().build())).thenReturn(false);

        // Run the test
        final String result = mySQLTestUnderTest.batchCreate(user);

        // Verify the results
        assertEquals("创建失败", result);
    }

    @Test
    void testQuery() {
        // Setup
        final User expectedResult = User.builder().build();
        when(mockUserService.query(0L)).thenReturn(User.builder().build());

        // Run the test
        final User result = mySQLTestUnderTest.query(0L);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testQueryByName() {
        // Setup
        final List<User> expectedResult = Arrays.asList(User.builder().build());
        when(mockUserService.queryByName("name")).thenReturn(Arrays.asList(User.builder().build()));

        // Run the test
        final List<User> result = mySQLTestUnderTest.queryByName("name");

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testQueryByName_UserServiceReturnsNoItems() {
        // Setup
        when(mockUserService.queryByName("name")).thenReturn(Collections.emptyList());

        // Run the test
        final List<User> result = mySQLTestUnderTest.queryByName("name");

        // Verify the results
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void testUpdateUser() {
        // Setup
        final User user = User.builder().build();
        when(mockUserService.updateUser(User.builder().build())).thenReturn(false);

        // Run the test
        final String result = mySQLTestUnderTest.updateUser(user);

        // Verify the results
        assertEquals("更新失败", result);
    }

    @Test
    void testCreateJob() {
        // Setup
        final QuartzBean quartzBean = QuartzBean.builder()
                .jobName("jobName")
                .jobClass("jobClass")
                .cronExpression("cronExpression")
                .build();

        // Run the test
        final String result = mySQLTestUnderTest.createJob(quartzBean);

        // Verify the results
        assertEquals("创建失败", result);
    }

    @Test
    void testPauseJob() {
        // Setup
        // Run the test
        final String result = mySQLTestUnderTest.pauseJob();

        // Verify the results
        assertEquals("暂停失败", result);
    }

    @Test
    void testRunOnce() {
        // Setup
        // Run the test
        final String result = mySQLTestUnderTest.runOnce();

        // Verify the results
        assertEquals("运行一次失败", result);
    }

    @Test
    void testResume() {
        // Setup
        // Run the test
        final String result = mySQLTestUnderTest.resume();

        // Verify the results
        assertEquals("启动失败", result);
    }

    @Test
    void testUpdate() {
        // Setup
        final QuartzBean quartzBean = QuartzBean.builder()
                .jobName("jobName")
                .jobClass("jobClass")
                .cronExpression("cronExpression")
                .build();

        // Run the test
        final String result = mySQLTestUnderTest.update(quartzBean);

        // Verify the results
        assertEquals("启动失败", result);
    }

    @Test
    void testDelete() {
        // Setup
        final QuartzBean quartzBean = QuartzBean.builder()
                .jobName("jobName")
                .jobClass("jobClass")
                .cronExpression("cronExpression")
                .build();

        // Run the test
        final String result = mySQLTestUnderTest.delete(quartzBean);

        // Verify the results
        assertEquals("删除定时任务失败", result);
    }

    @Test
    void testTestID() {
        // Setup
        // Run the test
        final String result = mySQLTestUnderTest.testID();

        // Verify the results
        assertEquals("执行失败", result);
        verify(mockStudentMapper).insertId("id");
    }

    @Test
    void testSendMessage() {
        // Setup
        // Run the test
        mySQLTestUnderTest.sendMessage("channelName", "content");

        // Verify the results
        verify(mockStringRedisTemplate).convertAndSend("channelName", "content");
    }

    @Test
    void testTestBatchInsertUser() throws Exception {
        // Setup
        // Configure SqlSessionFactory.openSession(...).
        final SqlSession mockSqlSession = mock(SqlSession.class);
        when(mockSqlSessionFactory.openSession(ExecutorType.BATCH, false)).thenReturn(mockSqlSession);

        // Run the test
        mySQLTestUnderTest.testBatchInsertUser();

        // Verify the results
        verify(mockSqlSession).close();
    }

    @Test
    void testTestCirculateInsertUser() throws Exception {
        // Setup
        // Configure SqlSessionFactory.openSession(...).
        final SqlSession mockSqlSession = mock(SqlSession.class);
        when(mockSqlSessionFactory.openSession(ExecutorType.BATCH, false)).thenReturn(mockSqlSession);

        // Run the test
        mySQLTestUnderTest.testCirculateInsertUser();

        // Verify the results
        verify(mockSqlSession).close();
    }

    @Test
    void testMybatisBatchInsert() throws Exception {
        // Setup
        // Configure SqlSessionFactory.openSession(...).
        final SqlSession mockSqlSession = mock(SqlSession.class);
        when(mockSqlSessionFactory.openSession(ExecutorType.BATCH, false)).thenReturn(mockSqlSession);

        // Run the test
        mySQLTestUnderTest.mybatisBatchInsert();

        // Verify the results
        verify(mockSqlSession).close();
    }

    @Test
    void testTestJDBCBatchInsertUser() {
        // Setup
        // Run the test
        mySQLTestUnderTest.testJDBCBatchInsertUser();

        // Verify the results
    }

    @Test
    void testTestFilesAPI_1() {
        // Setup
        // Run the test
        mySQLTestUnderTest.TestFilesAPI_1();

        // Verify the results
    }

    @Test
    void testTestSearchFromPgsql() {
        // Setup
        final PgUser expectedResult = new PgUser();
        expectedResult.setUserId("userId");
        expectedResult.setUsername("username");
        expectedResult.setUserAddress("userAddress");

        // Configure UserService.testSearchFromPgsql(...).
        final PgUser pgUser = new PgUser();
        pgUser.setUserId("userId");
        pgUser.setUsername("username");
        pgUser.setUserAddress("userAddress");
        when(mockUserService.testSearchFromPgsql("id")).thenReturn(pgUser);

        // Run the test
        final PgUser result = mySQLTestUnderTest.testSearchFromPgsql("id");

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testTestFunction() {
        // Setup
        // Run the test
        MySQLTest.TestFunction("data");

        // Verify the results
    }

    @Test
    void testMain() {
        // Setup
        // Run the test
        MySQLTest.main(new String[]{"args"});

        // Verify the results
    }
}
