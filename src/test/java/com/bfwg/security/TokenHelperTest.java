package com.bfwg.security;


import com.bfwg.common.TimeProvider;
import com.bfwg.model.User;
import org.assertj.core.util.DateUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mobile.device.Device;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by fan.jin on 2017-01-08.
 */
public class TokenHelperTest {

    private static final String TEST_USERNAME = "testUser";

    @InjectMocks
    private TokenHelper tokenHelper;

    @Mock
    private TimeProvider timeProviderMock;

    @InjectMocks
    DeviceDummy device;

    /**
    * Initializes the test environment with mock objects and sets up the tokenHelper with specific test values.
    * 
    * This method is annotated with @Before, indicating it runs before each test method.
    * It uses MockitoAnnotations to initialize mock objects and ReflectionTestUtils to set
    * private fields in the tokenHelper object for testing purposes.
    *
    * @throws Exception if there's an error during initialization
    */
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(tokenHelper, "EXPIRES_IN", 10); // 10 sec
        ReflectionTestUtils.setField(tokenHelper, "MOBILE_EXPIRES_IN", 20); // 20 sec
        ReflectionTestUtils.setField(tokenHelper, "SECRET", "mySecret");
    }

    /**
     * Tests if the token generation method produces different tokens for different creation dates.
     * 
     * This test method verifies that the token generation process takes into account
     * the creation date, ensuring that tokens created at different times are unique.
     * It uses a mock time provider to simulate different creation dates.
     * 
     * @throws Exception if an error occurs during token creation or assertion
     */
    @Test
    public void testGenerateTokenGeneratesDifferentTokensForDifferentCreationDates() throws Exception {
        when(timeProviderMock.now())
                .thenReturn(DateUtil.yesterday())
                .thenReturn(DateUtil.now());

        final String token = createToken(device);
        final String laterToken = createToken(device);

        assertThat(token).isNotEqualTo(laterToken);
    }

    /**
    * Tests if a mobile token has a longer lifespan than a regular token.
    * 
    * This method mocks necessary dependencies, creates a mobile token,
    * and verifies its validity after a short time period.
    * 
    * @throws <UNKNOWN> if token creation or validation fails
    */
    @Test
    public void mobileTokenShouldLiveLonger() {
        Date beforeSomeTime = new Date(DateUtil.now().getTime() - 15 * 1000);

        UserDetails userDetails = mock(User.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        when(timeProviderMock.now())
                .thenReturn(beforeSomeTime);
        device.setMobile(true);
        final String mobileToken = createToken(device);
        assertThat(tokenHelper.validateToken(mobileToken, userDetails)).isTrue();
    }

    /**
    * Verifies that a mobile token expires after a certain time period.
    * 
    * This test method checks if a mobile token becomes invalid after its expiration time.
    * It mocks the time provider to simulate a time in the past, creates a mobile token,
    * and then attempts to validate it, expecting the validation to fail.
    * 
    * @param <UNKNOWN> This method doesn't have any parameters
    * @return void This method doesn't return a value
    *
    * @throws <UNKNOWN> This method doesn't explicitly throw any exceptions
    */
    @Test
    public void mobileTokenShouldExpire() {
        Date beforeSomeTime = new Date(DateUtil.now().getTime() - 20 * 1000);

        when(timeProviderMock.now())
                .thenReturn(beforeSomeTime);

        UserDetails userDetails = mock(User.class);
        when(userDetails.getUsername()).thenReturn(TEST_USERNAME);

        device.setMobile(true);
        final String mobileToken = createToken(device);
        assertThat(tokenHelper.validateToken(mobileToken, userDetails)).isFalse();
    }

    /**
     * Retrieves the username from a given authentication token.
     * 
     * This method tests the functionality of extracting a username from a token.
     * It mocks the current time, creates a token, and then verifies that the
     * correct username is extracted from the token.
     * 
     * @param None
     * @return None
     * @throws Exception if there's an error during token creation or username extraction
     */
    @Test
    public void getUsernameFromToken() throws Exception {
        when(timeProviderMock.now()).thenReturn(DateUtil.now());

        final String token = createToken(device);

        assertThat(tokenHelper.getUsernameFromToken(token)).isEqualTo(TEST_USERNAME);
    }

    /**
    * Verifies that the creation date extracted from a token matches the current time.
    * 
    * This test method generates a token for a device, then checks if the issued-at date
    * extracted from the token is within the same minute window as the current time.
    * It uses mocking to control the current time for consistent testing.
    *
    * @throws Exception if token creation or validation fails
    */
    @Test
    public void getCreatedDateFromToken() {
        final Date now = DateUtil.now();
        when(timeProviderMock.now()).thenReturn(now);

        final String token = createToken(device);

        assertThat(tokenHelper.getIssuedAtDateFromToken(token)).isInSameMinuteWindowAs(now);
    }

    /**
    * Tests that an expired token cannot be refreshed.
    * 
    * This method simulates a scenario where a token has expired and attempts to refresh it.
    * It mocks the time provider to return yesterday's date, creates a token, and then
    * tries to refresh it using the tokenHelper.
    * 
    * @param <UNKNOWN> This method doesn't have any parameters
    * @return void This method doesn't return a value
    */
    @Test
    public void expiredTokenCannotBeRefreshed() {
        when(timeProviderMock.now())
                .thenReturn(DateUtil.yesterday());

        String token = createToken(device);
        tokenHelper.refreshToken(token, device);
    }

    /**
    * Tests the getAudienceFromToken method of the tokenHelper class.
    * 
    * This method verifies that the getAudienceFromToken method correctly
    * extracts the audience from a token generated for a web device.
    * 
    * @throws Exception if an error occurs during token creation or processing
    * @return void
    */
    @Test
    public void getAudienceFromToken() throws Exception {
        when(timeProviderMock.now()).thenReturn(DateUtil.now());
        device.setNormal(true);
        final String token = createToken(this.device);

        assertThat(tokenHelper.getAudienceFromToken(token)).isEqualTo(tokenHelper.AUDIENCE_WEB);
    }

    /**
    * Performs a test to verify the audience retrieval from a mobile token.
    * 
    * This test method mocks the time provider, sets the device as mobile,
    * creates a token, and then asserts that the audience retrieved from
    * the token matches the expected mobile audience.
    * 
    * @throws Exception if an error occurs during token creation or audience retrieval
    */
    @Test
    public void getAudienceFromMobileToken() throws Exception {
        when(timeProviderMock.now()).thenReturn(DateUtil.now());
        device.setMobile(true);
        final String token = createToken(this.device);
        assertThat(tokenHelper.getAudienceFromToken(token)).isEqualTo(tokenHelper.AUDIENCE_MOBILE);
    }

    /**
    * Verifies that a token becomes invalid when the user's password has been changed.
    * 
    * This test method checks if a token is invalidated after a user's password has been reset.
    * It mocks the current time, creates a user with a future password reset date,
    * generates a token, and then asserts that the token is no longer valid for this user.
    * 
    * @throws Exception if any error occurs during the test execution
    */    @Test
    public void changedPasswordCannotBeRefreshed() throws Exception {
        when(timeProviderMock.now())
                .thenReturn(DateUtil.now());

        User user = mock(User.class);
        when(user.getLastPasswordResetDate()).thenReturn(new Timestamp(DateUtil.tomorrow().getTime()));
        String token = createToken(device);
        assertThat(tokenHelper.validateToken(token, user)).isFalse();
    }

    /**
    * Tests the ability to refresh an authentication token.
    * 
    * This method verifies that:
    * 1. A new token can be created
    * 2. The created token can be refreshed
    * 3. The refreshed token has a later issue date than the original token
    * 
    * @throws Exception If there's an error during token creation or refresh
    */
    @Test
    public void canRefreshToken() throws Exception {
        when(timeProviderMock.now())
                .thenReturn(DateUtil.now())
                .thenReturn(DateUtil.tomorrow());
        String firstToken = createToken(device);
        String refreshedToken = tokenHelper.refreshToken(firstToken, device);
        Date firstTokenDate = tokenHelper.getIssuedAtDateFromToken(firstToken);
        Date refreshedTokenDate = tokenHelper.getIssuedAtDateFromToken(refreshedToken);
        assertThat(firstTokenDate).isBefore(refreshedTokenDate);
    }

    /**
     * Creates a token for the given device using a test username.
     * 
     * @param device The device for which the token is being generated
     * @return A string representing the generated token
     */
    private String createToken(Device device) {
        return tokenHelper.generateToken(TEST_USERNAME, device);
    }

}
