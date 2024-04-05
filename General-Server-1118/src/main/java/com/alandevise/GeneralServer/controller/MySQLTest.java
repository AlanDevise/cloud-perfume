package com.alandevise.GeneralServer.controller;

import cn.hutool.core.util.IdUtil;
import com.alandevise.GeneralServer.config.DatasourceContext;
import com.alandevise.GeneralServer.dao.StudentMapper;
import com.alandevise.GeneralServer.entity.*;
import com.alandevise.GeneralServer.service.UserService;
import com.alandevise.GeneralServer.util.IGlobalCache;
import com.alandevise.GeneralServer.util.QuartzUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.quartz.Scheduler;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @Filename: MySQLTest.java
 * @Package: com.alandevise.controller
 * @Version: V1.0.0
 * @Description: 1.
 * @Author: Alan Zhang [initiator@alandevise.com]
 * @Date: 2022-09-22 11:23
 */

@RestController
@RequestMapping("/general")
@Api(tags = "MySQL测试接口", value = "MySQL测试接口")
@Slf4j
public class MySQLTest {
    // Redis消息通道1
    public final String ChannelOne = "ChannelOne";
    // Redis消息通道2
    public final String ChannelTwo = "ChannelTwo";
    @Resource
    StudentMapper studentMapper;
    @Resource
    UserService userService;
    @Resource
    SqlSessionFactory sqlSessionFactory;
    // 注入任务调度
    @Resource
    private Scheduler scheduler;
    @Resource
    private IGlobalCache iGlobalCache;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // @Autowired
    // private FolderTree folderTree;

    // 创建一个堆栈来存储父节点ID，符合FILO
    static Stack<String> parentId = new Stack<>();

    @GetMapping("/getTest")
    @ApiOperation("MySQL基本操作测试-GET")
    public String FirstTest() {
        DatasourceContext.DataType datasourceType = DatasourceContext.getDatasourceType();
        log.warn(String.valueOf(datasourceType));
        return "运行到了Controller层";
    }

    @GetMapping("/hello")
    @ApiOperation("AOP切面Demo演示-GET")
    public String AopDemo(@RequestParam("name") String param) {
        return "接收到参数是：" + param;
    }

    @GetMapping("/helloAgain")
    @ApiOperation("AOP切面Demo演示-GET")
    public String AopDemo2(@RequestParam("name") String name,
                           @RequestParam("age") String age) {
        log.info("[Controller] 这里进入到了Controller");
        return "接收到参数是：" + name + age;
    }

    /**
     * 一次性插入大量数据的基本方法，一条数据一条数据插入，极为浪费系统性能，速度效率极慢<br/>
     * [极为不推荐此种方法进行大量数据写入操作]<br/>
     * record: 5W - 179s<br/>
     * 50W - 2468s<br/>
     */
    @GetMapping("/for")
    public void forSingle() {
        // 开始时间
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 500000; i++) {
            Student student = new Student("李毅" + i, 24, "张家界市" + i, i + "号");
            studentMapper.insert(student);
        }
        // 结束时间
        long endTime = System.currentTimeMillis();
        System.out.println("插入数据消耗时间：" + (endTime - startTime));
    }

    /**
     * 大量数据一次性插入的次优解，本质是将大量的插入语句合并成一句
     * 以减少重新获取数据库连接的次数，释放资源的次数，以及解析sql的次数，从而提高性能
     * 但同样存在一条sql的长度过长超过MySQL的默认限制的问题，需要手动增大<br/>
     * [ERROR] com.mysql.jdbc.PacketTooBigException: Packet for query is too large (39666728 > 4194304).
     */
    @GetMapping("/sql")
    public void sql() {
        ArrayList<Student> arrayList = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 500000; i++) {
            Student student = new Student("李毅" + i, 24, "张家界市" + i, i + "号");
            arrayList.add(student);
        }
        studentMapper.insertSplice(arrayList);
        long endTime = System.currentTimeMillis();
        System.out.println("插入数据消耗时间：" + (endTime - startTime) / 1000.0);
    }

    /**
     * 大量数据一次性插入MySQL的最优解 - 需要开启批处理 [Temporary]<br/>
     * record: 5W - 1s<br/>
     * 50W - 8.2s<br/>
     * 100W - 35.2s<br/>
     * 300W - 110.703s<br/>
     *
     * @throws SQLException
     */
    @GetMapping("/forSaveBatch")
    public void forSaveBatch() throws SQLException {
        // //  开启批量处理模式 BATCH 、关闭自动提交事务 false
        // SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        // //  反射获取，获取Mapper
        // StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
        // long startTime = System.currentTimeMillis();
        // for (int i = 0; i < 10000; i++) {
        //     Student student = new Student("李毅" + i, 24, "张家界市" + i, i + "号");
        //     studentMapper.insert(student);
        // }
        // // 一次性提交事务
        // sqlSession.commit();
        // sqlSession.clearCache();
        // // 关闭资源
        // sqlSession.close();
        // long endTime = System.currentTimeMillis();
        // log.error("总耗时： " + (endTime - startTime));


        // -------------------------------------------------------------------------

        // // Mapper直接执行SQL语句
        // //  开启批量处理模式 BATCH 、关闭自动提交事务 false
        // SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        // //  反射获取，获取Mapper
        // StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class);
        // long startTime = System.currentTimeMillis();
        // for (int i = 0; i < 10; i++) {
        //
        //     // String sql = "insert into student (`name`, age, addr, addr_num) values ('李毅', '27','深圳市', '123456')";
        //     // String sql = "insert into student (`name`, age, addr, addr_num) values ('李毅', '27','深圳市', '123456')";
        //     // String sql = "insert into student (`name`, age, addr, addr_num) values ('李毅', '27','深圳市', '123456')";
        //
        //     for (int j = 0; j < 10000; j++) {
        //         // SQL sqlStr = new SQL("student", "`name`, age, addr, addr_num", "'李毅', '27','深圳市', '123456'");
        //         TFAccrue tfAccrue = TFAccrue.builder()
        //                 .tagCode("73.OCT_121212.0.EPd" + "_" + i + j)
        //                 .tagNo("40")
        //                 .tagName("EPd")
        //                 .name("1212_日冻结正向有功总电能")
        //                 .tagType("0")
        //                 .tagClass("0")
        //                 .preci("2")
        //                 .unit("0")
        //                 .compute("1")
        //                 .deviceCode("73.OCT_121212")
        //                 .coefficient("1")
        //                 .radix("0")
        //                 .maxRange("1")
        //                 .minRange("1")
        //                 .filterMutational("0")
        //                 .mutationalPe("0")
        //                 .saveType("0")
        //                 .saveCyc("15")
        //                 .deadValue("0")
        //                 .count("0")
        //                 .showModel("")
        //                 .sort("0")
        //                 .fertProperty("0")
        //                 .details("")
        //                 .pointType("0")
        //                 .formula("")
        //                 .fertCode("0At4l1kxQfCS85eFGjL8NA")
        //                 .build();
        //         studentMapper.insertTFAccrue(tfAccrue);
        //         // 一次性提交事务
        //         sqlSession.commit();
        //         sqlSession.clearCache();
        //     }
        // }
        //
        // // 关闭资源
        // sqlSession.close();
        // long endTime = System.currentTimeMillis();
        // log.error("总耗时： " + (endTime - startTime) / 1000 + "秒");


        // -------------------------------------------------------------------------


        SqlSession sqlSession;
        Statement statement = null;
        long startTime = System.currentTimeMillis();
        try {
            int BATCH_SIZE = 10000;
            int DATA_COUNT = 10000;
            sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            statement = sqlSession.getConnection().createStatement();
            for (int i = 0; i < DATA_COUNT; i++) {

                // String sql = "insert into t_fert_accrue " +
                //         "        (`tag_code`," +
                //         "         `tag_no`, " +
                //         "         `tag_name`, " +
                //         "         `name`, " +
                //         "         `tag_type`, " +
                //         "         `tag_class`, " +
                //         "         `preci`, " +
                //         "         `unit`, " +
                //         "         `compute`, " +
                //         "         `device_code`, " +
                //         "         `coefficient`, " +
                //         "         `radix`, " +
                //         "         `max_range`, " +
                //         "         `min_range`, " +
                //         "         `filter_mutational`, " +
                //         "         `mutational_pe`, " +
                //         "         `save_type`, " +
                //         "         `save_cyc`, " +
                //         "         `dead_value`, " +
                //         "         `count`, " +
                //         "         `show_model`, " +
                //         "         `sort`, " +
                //         "         `fert_property`, " +
                //         "         `details`, " +
                //         "         `point_type`, " +
                //         "         `formula`, " +
                //         "         `fert_code`) " +
                //         "        values ('73.OCT_121212.0.EPd" + i + "'" + ", '1', '1', '1', '1', " +
                //         "                '1'," +
                //         "                '1', '1', '1', '1', " +
                //         "                '1', '1', " +
                //         "                '1', '1', '1', '1', " +
                //         "                '1', " +
                //         "                '1', '1', '1', '1', '1', " +
                //         "                '1', " +
                //         "                '1','1', '1','1')";

                String sql = "insert into student (name,age,addr,addr_num) values ('李毅','20','深圳市','号')";

                statement.addBatch(sql);
                if ((i % BATCH_SIZE == 0) || i == DATA_COUNT - 1) {
                    statement.executeBatch();
                    statement.clearBatch();
                    log.info("=== 执行批处理 {} 条", i);
                }
            }
            // sqlSession.commit();
        } finally {
            if (statement != null) {
                try {
                    statement.clearBatch();
                    statement.close();
                } catch (SQLException throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        sqlSession.commit();
        sqlSession.close();
        long endTime = System.currentTimeMillis();
        log.warn("总耗时： " + (endTime - startTime) / 1000.0 + "秒");
    }

    /**
     * MySQL流式查询
     *
     * @throws SQLException
     */
    @GetMapping("/forStreamSearch")
    public void forStreamSearch() throws SQLException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.setKeepTaskList(false);
        stopWatch.start();
        int count = 0;

        SqlSession sqlSession = sqlSessionFactory.openSession();
        Connection connection = sqlSession.getConnection();
        Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(Integer.MIN_VALUE);
        ResultSet resultSet = statement.executeQuery("select * from t_fert_accrue;");
        while (resultSet.next()) {
            String tagCode = resultSet.getString("tag_code");
            log.warn(tagCode);
            count++;
        }
        stopWatch.stop();
        log.warn("Count is " + count);
        log.warn("耗时：" + stopWatch.getLastTaskTimeMillis() / 1000.0 + "秒");
    }

    /*
     * MySQL - Mapper查询
     * */
    @GetMapping("/forMapperSearch")
    public void forMapperSearch() {

        StopWatch stopWatch = new StopWatch();
        stopWatch.setKeepTaskList(false);
        stopWatch.start();
        int count = 0;

        SqlSession sqlSession = sqlSessionFactory.openSession();
        StudentMapper mapper = sqlSession.getMapper(StudentMapper.class);
        Cursor<TFAccrue> cursor = mapper.selectInfo(null);
        while (cursor.iterator().hasNext()) {
            String tagCode = cursor.iterator().next().getTagCode();
            log.info(tagCode);
            count++;
        }
        stopWatch.stop();
        log.warn("Count is " + count);
        log.warn("耗时：" + stopWatch.getLastTaskTimeMillis() / 1000.0 + "秒");
    }

    /**
     * 创建单个用户
     *
     * @param user 用户实体类
     * @return java.lang.String
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 09:51
     */
    @RequestMapping("/create")
    public String create(User user) {
        Boolean result = userService.create(user);
        if (result) {
            return "创建成功";
        }
        return "创建失败";
    }

    /**
     * 批量创建用户
     *
     * @param user 用户实体类
     * @return java.lang.String
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 09:50
     */
    @RequestMapping("/batchCreate")
    public String batchCreate(User user) throws Exception {
        Boolean result = userService.batchCreate(user);
        if (result) {
            return "创建成功";
        }
        return "创建失败";
    }

    /**
     * 通过ID查询用户
     *
     * @param id 用户ID
     * @return com.alandevise.GeneralServer.entity.User
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 09:49
     */
    @RequestMapping("/query")
    public User query(Long id) {
        String apple = null;
        return userService.query(id);
    }

    /**
     * 通过名称批量查询用户接口
     *
     * @param name 用户名
     * @return java.util.List<com.alandevise.GeneralServer.entity.User>
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 09:47
     */
    @RequestMapping("/queryByName")
    public List<User> queryByName(String name) {
        return userService.queryByName(name);
    }

    /**
     * 根据用户ID更新用户
     *
     * @param user 用户对象实体类
     * @return java.lang.String
     * @author Alan Zhang [initiator@alandevise.com]
     * @date 2024/2/19 09:53
     */
    @RequestMapping("/updateUser")
    public String updateUser(User user) {
        Boolean result = userService.updateUser(user);
        if (result) {
            return "更新成功";
        }
        return "更新失败";
    }


    @RequestMapping("/createJob")
    @ResponseBody
    public String createJob(QuartzBean quartzBean) {
        try {
            // 进行测试所以写死
            quartzBean.setJobClass("com.alandevise.Task.MyTask1");
            quartzBean.setJobName("test1");
            quartzBean.setCronExpression("*/10 * * * * ?");
            QuartzUtils.createScheduleJob(scheduler, quartzBean);
        } catch (Exception e) {
            return "创建失败";
        }
        return "创建成功";
    }

    @RequestMapping("/pauseJob")
    @ResponseBody
    public String pauseJob() {
        try {
            QuartzUtils.pauseScheduleJob(scheduler, "test1");
        } catch (Exception e) {
            return "暂停失败";
        }
        return "暂停成功";
    }

    @RequestMapping("/runOnce")
    @ResponseBody
    public String runOnce() {
        try {
            QuartzUtils.runOnce(scheduler, "test1");
        } catch (Exception e) {
            return "运行一次失败";
        }
        return "运行一次成功";
    }

    @RequestMapping("/resume")
    @ResponseBody
    public String resume() {
        try {

            QuartzUtils.resumeScheduleJob(scheduler, "test1");
        } catch (Exception e) {
            return "启动失败";
        }
        return "启动成功";
    }

    @RequestMapping("/update")
    @ResponseBody
    public String update(QuartzBean quartzBean) {
        try {
            // 进行测试所以写死
            quartzBean.setJobClass("com.alandevise.Task.MyTask1");
            quartzBean.setJobName("test1");
            quartzBean.setCronExpression("10 * * * * ?");
            QuartzUtils.updateScheduleJob(scheduler, quartzBean);
        } catch (Exception e) {
            return "启动失败";
        }
        return "启动成功";
    }

    @RequestMapping("/delete")
    @ResponseBody
    public String delete(QuartzBean quartzBean) {
        try {
            QuartzUtils.deleteScheduleJob(scheduler, "test1");
        } catch (Exception e) {
            return "删除定时任务失败";
        }
        return "删除定时任务成功";
    }

    @RequestMapping("/testID")
    @ResponseBody
    public String testID() {
        try {
            for (int i = 0; i < 200; i++) {
                new Thread(() -> {
                    for (int j = 0; j < 2000; j++) {
                        String snowFlakeID = IdUtil.getSnowflake().nextIdStr();
                        log.info(snowFlakeID);
                        studentMapper.insertId(snowFlakeID);
                    }
                }, "MyThread - " + i).start();
            }
        } catch (Exception e) {
            return "执行失败";
        }
        return "执行成功";
    }


    // @GetMapping("/FolderTree")
    // @ApiOperation("遍历文件夹测试-GET")
    // public void FolderTree() {
    //     // 根目录文件
    //     String rootPath = "D:/test";
    //     File rootFile = new File(rootPath);
    //     TraverseAllFileInFolder(rootFile);
    // }

    // private void TraverseAllFileInFolder(File file) {
    //     File[] files = file.listFiles();
    //     if (files != null) {
    //         for (File f : files) {
    //             if (f.isDirectory()) {   // 是目录
    //                 String id = IdUtil.fastSimpleUUID();
    //                 String treeId = IdUtil.fastSimpleUUID();
    //                 String treeParentId;
    //                 if (!parentId.isEmpty()) {
    //                     treeParentId = parentId.peek();
    //                 } else {
    //                     treeParentId = "";
    //                 }
    //                 String name = f.getName();
    //                 Integer type = 0;
    //                 folderTree.insertAllInfo(id, treeId, treeParentId, name, type, ""); // 更新插入到数据库
    //                 parentId.push(treeId);  // 将当前树形节点压入栈
    //                 TraverseAllFileInFolder(f);
    //             }
    //             if (f.isFile()) {    // 是文件
    //                 String id = IdUtil.fastSimpleUUID();
    //                 String treeId = IdUtil.fastSimpleUUID();
    //                 String treeParentId;
    //                 if (!parentId.isEmpty()) {
    //                     treeParentId = parentId.peek(); // 取最上层的数据
    //                 } else {
    //                     treeParentId = "";
    //                 }
    //                 String name = f.getName();
    //                 Integer type = 1;
    //                 folderTree.insertAllInfo(id, treeId, treeParentId, name, type, ((f.getPath()).replaceAll("\\\\","/")).replaceAll("D:/","")); // 更新插入到数据库
    //             }
    //         }
    //         if (!parentId.isEmpty()) {
    //             parentId.pop();     // 一整个文件夹遍历完及离开出栈
    //         }
    //     }
    // }

    @PostMapping("/send")
    public void sendMessage(@RequestParam(value = "channelName") String channelName,
                            @RequestParam(value = "content") String content) {
        stringRedisTemplate.convertAndSend(channelName, content);
    }

    // @GetMapping("/invokeScheduledTask")
    // public void invokeScheduledTask() {
    //     System.out.println("[INFO] 手动触发一次");
    //     ScheduledTask.test();
    //     System.out.println("[INFO] 手动触发结束");
    // }

    @GetMapping("mybatisOnce")
    public void testBatchInsertUser() throws IOException {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            System.out.println("===== 开始插入数据 =====");
            long startTime = System.currentTimeMillis();
            List<t_test_user> userList = new ArrayList<>();
            for (int i = 1; i <= 300000; i++) {
                // User user = new User();
                t_test_user tTestUser = new t_test_user();
                tTestUser.setId(i);
                tTestUser.setUsername("共饮一杯无 " + i);
                tTestUser.setAge((int) (Math.random() * 100));
                userList.add(tTestUser);
            }
            session.insert("batchInsertUser", userList); // 最后插入剩余的数据
            session.commit();

            long spendTime = System.currentTimeMillis() - startTime;
            System.out.println("成功插入 30 万条数据,耗时：" + spendTime + "毫秒");
        }
    }

    @GetMapping("mybatisLoopInsert")
    public void testCirculateInsertUser() throws IOException {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        System.out.println("===== 开始插入数据 =====");
        long startTime = System.currentTimeMillis();
        try {
            for (int i = 1; i <= 300000; i++) {
                t_test_user user = new t_test_user();
                user.setId(i);
                user.setUsername("共饮一杯无 " + i);
                user.setAge((int) (Math.random() * 100));
                // 一条一条新增
                session.insert("insertUser", user);
                session.commit();
            }

            long spendTime = System.currentTimeMillis() - startTime;
            System.out.println("成功插入 30 万条数据,耗时：" + spendTime + "毫秒");
        } finally {
            session.close();
        }
    }

    @GetMapping("mybatisBatchInsert")
    public void mybatisBatchInsert() throws IOException {
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            System.out.println("===== 开始插入数据 =====");
            long startTime = System.currentTimeMillis();
            List<t_test_user> userList = new ArrayList<>();
            for (int i = 1; i <= 300000; i++) {
                t_test_user user = new t_test_user();
                user.setId(i);
                user.setUsername("共饮一杯无 " + i);
                user.setAge((int) (Math.random() * 100));
                userList.add(user);
                if (i % 5000 == 0) {
                    session.insert("batchInsertUser", userList);
                    // 每 1000 条数据提交一次事务
                    session.commit();
                    userList.clear();
                }
            }
            // 最后插入剩余的数据
            if (!CollectionUtils.isEmpty(userList)) {
                session.insert("batchInsertUser", userList);
                session.commit();
            }

            long spendTime = System.currentTimeMillis() - startTime;
            System.out.println("成功插入 30 万条数据,耗时：" + spendTime + "毫秒");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * JDBC分批次批量插入
     */
    @GetMapping("testJDBCBatchInsertUser")
    public void testJDBCBatchInsertUser() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        String databaseURL = "jdbc:mysql://localhost:3306/AlanDeviseDatabase?useUnicode=true&characterEncoding=utf8&useUnicode=true&characterEncoding=utf-8&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true";
        String user = "root";
        String password = "AlanDevise2020";

        int count = 3000000;
        int batchSize = 10000;

        try {
            connection = DriverManager.getConnection(databaseURL, user, password);
            // 关闭自动提交事务，改为手动提交
            connection.setAutoCommit(false);
            System.out.println("===== 开始插入数据 =====");
            long startTime = System.currentTimeMillis();
            String sqlInsert = "INSERT INTO t_user ( username, age) VALUES ( ?, ?)";
            preparedStatement = connection.prepareStatement(sqlInsert);

            Random random = new Random();
            for (int i = 1; i <= count; i++) {
                preparedStatement.setString(1, "共饮一杯无 " + i);
                preparedStatement.setInt(2, random.nextInt(100));
                // 添加到批处理中
                preparedStatement.addBatch();

                if (i % batchSize == 0) {
                    // 每1000条数据提交一次
                    preparedStatement.executeBatch();
                    connection.commit();
                    System.out.println("成功插入第 " + i + " 条数据");
                }

            }
            // 处理剩余的数据
            preparedStatement.executeBatch();
            connection.commit();
            long spendTime = System.currentTimeMillis() - startTime;
            System.out.println("成功插入 " + count / 10000 + " 万条数据,耗时：" + spendTime + "毫秒");
            System.out.println("批大小：" + batchSize);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Test Files API test1
     */
    @GetMapping("TestFilesAPI_1")
    public void TestFilesAPI_1() {
        String dirName = "C:\\Users\\Alan\\Downloads";
        // 深度优先 the maximum number of directory levels to visit，如1表示指定路径的第一个层级，不深入第一个层级的文件夹内容
        try (Stream<Path> paths = Files.walk(Paths.get(dirName), 2)) {
            paths.filter(Files::isRegularFile)
                    .forEach(System.out::println);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("testSearchFromPgsql")
    public PgUser testSearchFromPgsql(@RequestParam(value = "id") String id) {
        return userService.testSearchFromPgsql(id);
    }

    public static void main(String[] args) {
        ThreadLocal<String> local = new ThreadLocal<>();

        IntStream.range(0, 10).forEach(i -> new Thread(() -> {
            local.set(Thread.currentThread().getName() + ":" + i);
            System.out.println("线程：" + Thread.currentThread().getName() + ",local:" + local.get());
        }).start());
    }

}
