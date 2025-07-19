# Authentication and Authorization Tests

This document describes the comprehensive test suite for authentication and authorization in the JWT-based security system.

## Test Files Overview

### 1. `JwtUtilTest.java`
**Purpose:** Unit tests for JWT utility functions
**Scope:** Tests JWT generation, validation, and extraction methods

### 2. `AuthenticationAuthorizationTest.java`
**Purpose:** Comprehensive unit tests for authentication and authorization scenarios
**Scope:** Tests all authentication and authorization scenarios with detailed assertions

### 3. `ControllerAuthenticationTest.java`
**Purpose:** Integration tests for actual controller endpoints
**Scope:** Tests real HTTP requests with different authentication scenarios

### 4. `TestDataSetup.java`
**Purpose:** Helper class to set up test data
**Scope:** Creates test users in the database for testing

## Test Scenarios Covered

### 🔐 Authentication Tests

#### Valid JWT Scenarios
- ✅ **Valid JWT with correct signature**
- ✅ **Valid JWT with correct expiration**
- ✅ **Valid JWT with correct claims**

#### Invalid JWT Scenarios
- ❌ **Null JWT token**
- ❌ **Empty JWT token**
- ❌ **Malformed JWT token**
- ❌ **Invalid JWT signature**
- ❌ **Expired JWT token**
- ❌ **JWT with wrong algorithm**

#### Missing JWT Scenarios
- ❌ **No Authorization header**
- ❌ **Empty Authorization header**
- ❌ **Incomplete Bearer token**
- ❌ **Non-Bearer authorization header**

### 🛡️ Authorization Tests

#### Valid JWT with Access
- ✅ **Admin accessing admin-only endpoints**
- ✅ **User accessing their own resources**
- ✅ **Admin accessing any user's resources**

#### Valid JWT without Access
- ❌ **Regular user accessing admin-only endpoints**
- ❌ **User accessing other user's resources**
- ❌ **User accessing resources they don't own**

#### Cross-User Access Tests
- ✅ **Admin can access any user's resources**
- ❌ **Regular user cannot access other user's resources**
- ✅ **User can access their own resources**

### 🔄 Integration Tests

#### Complete Authentication Flow
1. **Login with valid credentials** → Get JWT
2. **Use JWT to access protected endpoint** → Success
3. **Use JWT to access unauthorized endpoint** → Failure

#### Complete Authorization Flow
1. **Admin user** → Can access admin endpoints
2. **Regular user** → Can access own resources only
3. **Cross-user access** → Properly blocked

## Test Categories

### 1. Unit Tests (`JwtUtilTest.java`)
```java
@Test
void testGenerateAndValidateToken() {
    // Tests JWT generation and validation
}

@Test
void testInvalidToken() {
    // Tests various invalid token scenarios
}

@Test
void testAdminToken() {
    // Tests admin role verification
}
```

### 2. Authentication Tests (`AuthenticationAuthorizationTest.java`)
```java
@Test
void testValidJWT_AuthenticationSuccess() {
    // Tests successful authentication
}

@Test
void testInvalidJWT_AuthenticationFailure() {
    // Tests authentication failures
}

@Test
void testMissingJWT_AuthenticationFailure() {
    // Tests missing JWT scenarios
}
```

### 3. Authorization Tests (`AuthenticationAuthorizationTest.java`)
```java
@Test
void testValidJWT_AdminAccess_AuthorizationSuccess() {
    // Tests admin authorization
}

@Test
void testValidJWT_CrossUserAccess_AuthorizationFailure() {
    // Tests cross-user access restrictions
}

@Test
void testValidJWT_AdminAccessingUserResources_AuthorizationSuccess() {
    // Tests admin access to user resources
}
```

### 4. Integration Tests (`ControllerAuthenticationTest.java`)
```java
@Test
void testValidJWT_AdminAccess_ShouldSucceed() {
    // Tests actual HTTP requests with valid admin JWT
}

@Test
void testValidJWT_UserAccessingAdminEndpoint_ShouldFail() {
    // Tests actual HTTP requests with valid user JWT to admin endpoint
}

@Test
void testInvalidJWT_ShouldFail() {
    // Tests actual HTTP requests with invalid JWT
}
```

## Test Data

### Test Users
- **admin** (ID: 1, Role: ADMIN)
- **user1** (ID: 2, Role: USER)
- **user2** (ID: 3, Role: USER)

### Test Scenarios
1. **Admin accessing admin endpoints** → ✅ Success
2. **Admin accessing user endpoints** → ✅ Success
3. **User accessing own endpoints** → ✅ Success
4. **User accessing admin endpoints** → ❌ Failure
5. **User accessing other user's endpoints** → ❌ Failure

## Running Tests

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Classes
```bash
./mvnw test -Dtest=JwtUtilTest
./mvnw test -Dtest=AuthenticationAuthorizationTest
./mvnw test -Dtest=ControllerAuthenticationTest
```

### Run Specific Test Methods
```bash
./mvnw test -Dtest=JwtUtilTest#testGenerateAndValidateToken
./mvnw test -Dtest=AuthenticationAuthorizationTest#testValidJWT_AdminAccess_AuthorizationSuccess
```

## Expected Test Results

### All Tests Should Pass
- ✅ **JWT Generation and Validation**
- ✅ **Authentication Success/Failure Scenarios**
- ✅ **Authorization Success/Failure Scenarios**
- ✅ **Integration Tests with Real HTTP Requests**
- ✅ **Edge Cases and Error Handling**

### Test Coverage
- **Authentication:** 100% coverage of all scenarios
- **Authorization:** 100% coverage of all scenarios
- **Integration:** Full HTTP request/response testing
- **Edge Cases:** Comprehensive error handling

## Test Configuration

### Database
- **Test Database:** H2 In-Memory
- **Configuration:** `application-test.properties`
- **Data Setup:** `TestDataSetup.java`

### Security
- **JWT Secret:** Same as production for consistency
- **Token Expiration:** 1 day (same as production)
- **Test Users:** Pre-configured with known credentials

## Debugging Tests

### Enable Debug Logging
```properties
logging.level.com.authapp.demo=DEBUG
logging.level.org.springframework.security=DEBUG
```

### View H2 Console (Optional)
```properties
spring.h2.console.enabled=true
```
Access: `http://localhost:8080/h2-console`

### Test Data Verification
```java
@Test
void verifyTestData() {
    assertTrue(userRepository.findByUsername("admin").isPresent());
    assertTrue(userRepository.findByUsername("user1").isPresent());
    assertTrue(userRepository.findByUsername("user2").isPresent());
}
```

## Test Maintenance

### Adding New Tests
1. **Unit Tests:** Add to `JwtUtilTest.java` for JWT utility functions
2. **Authentication Tests:** Add to `AuthenticationAuthorizationTest.java`
3. **Integration Tests:** Add to `ControllerAuthenticationTest.java`

### Updating Test Data
1. **Modify:** `TestDataSetup.java` to add new test users
2. **Update:** Test methods to use new test data
3. **Verify:** All tests still pass

### Test Best Practices
- ✅ **Use descriptive test names**
- ✅ **Test both success and failure scenarios**
- ✅ **Include edge cases**
- ✅ **Use proper assertions with messages**
- ✅ **Keep tests independent**
- ✅ **Use setup/teardown methods appropriately** 