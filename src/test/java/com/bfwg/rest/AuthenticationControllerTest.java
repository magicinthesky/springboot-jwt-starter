package com.bfwg.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.assertj.core.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mobile.device.Device;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.bfwg.common.DeviceProvider;
import com.bfwg.common.TimeProvider;
import com.bfwg.model.Authority;
import com.bfwg.model.User;
import com.bfwg.model.UserRoleName;
import com.bfwg.security.DeviceDummy;
import com.bfwg.security.TokenHelper;
import com.bfwg.service.impl.CustomUserDetailsService;

/**
 * Created by fanjin on 2017-09-01.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthenticationControllerTest {

	private MockMvc mvc;

	@MockBean
	private TimeProvider timeProviderMock;

	private static final String TEST_USERNAME = "testUser";

	@Autowired
	private TokenHelper tokenHelper;

	@MockBean
	private CustomUserDetailsService userDetailsService;

	@InjectMocks
	private AuthenticationController authenticationController;

	@Autowired
	private WebApplicationContext context;

	@MockBean
	private DeviceProvider deviceProvider;

	@Autowired
	private DeviceDummy device;

	/**
	* Sets up the test environment for Spring MVC and security tests.
	* 
	* This method initializes the MockMvc instance, configures a mock user,
	* sets up UserDetailsService mock, and initializes test properties for
	* the TokenHelper. It also configures a device for testing different
	* client types.
	* 
	* @throws Exception if an error occurs during setup
	*/	@Before
	public void setup() {

		mvc = MockMvcBuilders
				.webAppContextSetup(context)
				.apply(springSecurity())
				.build();

		User user = new User();
		user.setUsername("username");
		Authority authority = new Authority();
		authority.setId(0L);
		authority.setName(UserRoleName.ROLE_USER);
		List<Authority> authorities = Arrays.asList(authority);
		user.setAuthorities(authorities);
		user.setLastPasswordResetDate(new Timestamp(DateUtil.yesterday().getTime()));
		when(this.userDetailsService.loadUserByUsername(eq("testUser"))).thenReturn(user);
		MockitoAnnotations.initMocks(this);

		ReflectionTestUtils.setField(tokenHelper, "EXPIRES_IN", 100); // 100 sec
		ReflectionTestUtils.setField(tokenHelper, "MOBILE_EXPIRES_IN", 200); // 200 sec
		ReflectionTestUtils.setField(tokenHelper, "SECRET", "queenvictoria");

		device.setMobile(false);
		device.setNormal(false);
		device.setTablet(false);
	}

	/**
	* Performs a test to verify that an empty token state is returned when given a valid old token.
	* This test simulates a POST request to the "/auth/refresh" endpoint with a bearer token,
	* expecting a response with null values for access_token and expires_in.
	* 
	* @throws Exception if an error occurs during the execution of the test
	*/	@Test
	public void shouldGetEmptyTokenStateWhenGivenValidOldToken() throws Exception {
		when(timeProviderMock.now())
				.thenReturn(DateUtil.yesterday());
		this.mvc.perform(post("/auth/refresh")
				.header("Authorization", "Bearer 123"))
				.andExpect(content().json("{access_token:null,expires_in:null}"));
	}

	/**
	* Performs a test to verify the refresh functionality for a non-expired web token.
	* This test simulates a user with the "USER" role attempting to refresh their token.
	* It sets up mock behavior for time and device, creates a token, refreshes it,
	* and then sends a POST request to the "/auth/refresh" endpoint to verify the response.
	* 
	* @throws Exception if an error occurs during the test execution
	*/
	@Test
	@WithMockUser(roles = "USER")
	public void shouldRefreshNotExpiredWebToken() throws Exception {

		given(timeProviderMock.now())
				.willReturn(new Date(30L));

		device.setNormal(true);
		String token = createToken(device);
		String refreshedToken = tokenHelper.refreshToken(token, device);
		given(deviceProvider.getCurrentDevice(any()))
				.willReturn(device);
		this.mvc.perform(post("/auth/refresh")
				.header("Authorization", "Bearer " + token))
				.andExpect(content().json("{access_token:" + refreshedToken + ",expires_in:100}"));
	}

	/**
	 * Performs a test to verify the refresh functionality for a non-expired mobile token.
	 * This test mocks the current time, sets up a mobile device, creates a token,
	 * refreshes it, and then sends a POST request to the "/auth/refresh" endpoint
	 * to validate the token refresh process.
	 * 
	 * @throws Exception if an error occurs during the test execution
	 */
	@Test
	@WithMockUser(roles = "USER")
	public void shouldRefreshNotExpiredMobileToken() throws Exception {
		given(timeProviderMock.now())
				.willReturn(new Date(30L));
		device.setMobile(true);
		String token = createToken(device);
		String refreshedToken = tokenHelper.refreshToken(token, device);
		given(deviceProvider.getCurrentDevice(any()))
				.willReturn(device);
		this.mvc.perform(post("/auth/refresh")
				.header("Authorization", "Bearer " + token))
				.andExpect(content().json("{access_token:" + refreshedToken + ",expires_in:200}"));
	}

	/**
	* Verifies that an expired web token is not refreshed.
	* 
	* This test method sets up a scenario where a web token has expired and attempts to refresh it.
	* It expects that the refresh attempt will fail, returning null values for the access token and expiration time.
	* 
	* @throws Exception if an error occurs during the test execution
	*/
	@Test
	public void shouldNotRefreshExpiredWebToken() throws Exception {
		Date beforeSomeTime = new Date(DateUtil.now().getTime() - 15 * 1000);
		when(timeProviderMock.now())
				.thenReturn(beforeSomeTime);
		device.setNormal(true);
		String token = createToken(device);
		this.mvc.perform(post("/auth/refresh")
				.header("Authorization", "Bearer " + token))
				.andExpect(content().json("{access_token:null,expires_in:null}"));
	}

	/**
	* Performs a test to verify the behavior when attempting to refresh an expired mobile token.
	* 
	* This test method simulates a scenario where a mobile token has expired and attempts to refresh it.
	* It sets up a mock time provider to create a token that is already expired, then sends a POST
	* request to the "/auth/refresh" endpoint with the expired token in the Authorization header.
	* The expected result is a JSON response indicating that no new access token is provided.
	* 
	* @throws Exception if an error occurs during the execution of the test
	*/
	@Test
	public void shouldRefreshExpiredMobileToken() throws Exception {
		Date beforeSomeTime = new Date(DateUtil.now().getTime() - 15 * 1000);
		when(timeProviderMock.now())
				.thenReturn(beforeSomeTime);
		device.setNormal(true);
		String token = createToken(device);
		this.mvc.perform(post("/auth/refresh").header("Authorization", "Bearer " + token))
				.andExpect(content().json("{access_token:null,expires_in:null}"));
	}

	/**
	 * Creates a token for the given device using a test username.
	 * 
	 * @param device The device for which to generate the token
	 * @return A String representing the generated token
	 */
	private String createToken(Device device) {
		return tokenHelper.generateToken(TEST_USERNAME, device);
	}
}
