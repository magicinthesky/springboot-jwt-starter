package com.bfwg.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by fanjin on 2017-09-01.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    /**
    * Sets up the MockMvc instance for testing.
    * 
    * This method is annotated with @Before, indicating it runs before each test.
    * It initializes the MockMvc object using MockMvcBuilders, sets up the web
    * application context, applies Spring Security, and builds the MockMvc instance.
    * 
    * @return void
    */
    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    /**
    * Tests if unauthorized access is properly handled for the /user endpoint.
    * 
    * This method performs a GET request to the /user endpoint without any user role
    * and expects an HTTP 401 Unauthorized response.
    * 
    * @throws Exception if the mvc perform operation fails
    */
    @Test
    @WithAnonymousUser
    public void shouldGetUnauthorizedWithoutRole() throws Exception {

        this.mvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }

    /**
    * Performs a test to verify that a user with the "USER" role can successfully access the "/api/whoami" endpoint.
    * 
    * @throws Exception if an error occurs during the test execution
    */
    @Test
    @WithMockUser(roles = "USER")
    public void getPersonsSuccessfullyWithUserRole() throws Exception {
        this.mvc.perform(get("/api/whoami"))
                .andExpect(status().is2xxSuccessful());
    }

    /**
     * Performs a test to verify that an anonymous user cannot access the "/api/whoami" endpoint.
     * This test ensures that the endpoint returns a 4xx client error status when accessed without authentication.
     * 
     * @throws Exception if an error occurs during the execution of the test
     */
    @Test
    @WithAnonymousUser
    public void getPersonsFailWithAnonymousUser() throws Exception {
        this.mvc.perform(get("/api/whoami"))
                .andExpect(status().is4xxClientError());
    }

    /**
    * Performs a test to verify that an admin user can successfully retrieve all users.
    * This test method simulates an HTTP GET request to "/api/user/all" endpoint
    * with an authenticated user having ADMIN role, and expects a successful response.
    *
    * @throws Exception if the mvc perform request throws an exception
    */
    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllUserSuccessWithAdminRole() throws Exception {
        this.mvc.perform(get("/api/user/all"))
                .andExpect(status().is2xxSuccessful());
    }

    /**
    * Tests the behavior of accessing all users with USER role.
    * This method verifies that a user with the USER role cannot access
    * the endpoint to retrieve all users, expecting a 4xx client error.
    * 
    * @throws Exception if the mvc perform operation fails
    */
    @Test
    @WithMockUser(roles = "USER")
    public void getAllUserFailWithUserRole() throws Exception {
        this.mvc.perform(get("/api/user/all"))
                .andExpect(status().is4xxClientError());
    }
}

