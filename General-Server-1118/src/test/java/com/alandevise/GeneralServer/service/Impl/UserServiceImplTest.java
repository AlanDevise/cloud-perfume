package com.alandevise.GeneralServer.service.Impl;

import com.alandevise.GeneralServer.dao.StudentMapper;
import com.alandevise.GeneralServer.entity.PgUser;
import com.alandevise.GeneralServer.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private StudentMapper mockStudentMapper;

    private UserServiceImpl userServiceImplUnderTest;

    @BeforeEach
    void setUp() {
        userServiceImplUnderTest = new UserServiceImpl();
        userServiceImplUnderTest.studentMapper = mockStudentMapper;
    }

    @Test
    void testCreate() {
        // Setup
        final User user = User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build();
        when(mockStudentMapper.create(User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build())).thenReturn(0);

        // Run the test
        final Boolean result = userServiceImplUnderTest.create(user);

        // Verify the results
        assertFalse(result);
    }

    @Test
    void testBatchCreate() {
        // Setup
        final User user = User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build();
        when(mockStudentMapper.batchCreateUser(Arrays.asList(User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build()))).thenReturn(0);

        // Run the test
        final Boolean result = userServiceImplUnderTest.batchCreate(user);

        // Verify the results
        assertFalse(result);
    }

    @Test
    void testUpdateUser() {
        // Setup
        final User user = User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build();
        when(mockStudentMapper.updateUser(User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build())).thenReturn(0);

        // Run the test
        final Boolean result = userServiceImplUnderTest.updateUser(user);

        // Verify the results
        assertFalse(result);
    }

    @Test
    void testQuery() {
        // Setup
        final User expectedResult = User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build();

        // Configure StudentMapper.selectUser(...).
        final User user = User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build();
        when(mockStudentMapper.selectUser(0L)).thenReturn(user);

        // Run the test
        final User result = userServiceImplUnderTest.query(0L);

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testQueryByName() {
        // Setup
        final List<User> expectedResult = Arrays.asList(User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build());

        // Configure StudentMapper.selectUserList(...).
        final List<User> users = Arrays.asList(User.builder()
                .name("name")
                .balance(0)
                .password("password")
                .build());
        when(mockStudentMapper.selectUserList("name")).thenReturn(users);

        // Run the test
        final List<User> result = userServiceImplUnderTest.queryByName("name");

        // Verify the results
        assertEquals(expectedResult, result);
    }

    @Test
    void testQueryByName_StudentMapperReturnsNoItems() {
        // Setup
        when(mockStudentMapper.selectUserList("name")).thenReturn(Collections.emptyList());

        // Run the test
        final List<User> result = userServiceImplUnderTest.queryByName("name");

        // Verify the results
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    void testTestSearchFromPgsql() {
        // Setup
        final PgUser expectedResult = new PgUser();
        expectedResult.setUserId("userId");
        expectedResult.setUsername("username");
        expectedResult.setUserAddress("userAddress");

        // Configure StudentMapper.testSearchFromPgsql(...).
        final PgUser pgUser = new PgUser();
        pgUser.setUserId("userId");
        pgUser.setUsername("username");
        pgUser.setUserAddress("userAddress");
        when(mockStudentMapper.testSearchFromPgsql("userId")).thenReturn(pgUser);

        // Run the test
        final PgUser result = userServiceImplUnderTest.testSearchFromPgsql("userId");

        // Verify the results
        assertEquals(expectedResult, result);
    }
}
